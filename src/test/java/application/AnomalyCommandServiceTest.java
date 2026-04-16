package application;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import application.actor.Actor;
import application.actor.Role;
import application.command.AnomalyCommandService;
import application.command.CommandFailure;
import application.command.CommandResult;
import application.command.CommandSuccess;
import application.dto.AnomalyDto;
import application.query.AnomalyQueryService;
import application.query.QueryContext;
import application.query.QueryFailure;
import application.query.QueryNotFound;
import application.query.QueryResult;
import application.query.QuerySuccess;
import application.query.SortingSelection;
import application.repository.AnomalyRepository;
import domain.anomaly.Anomaly;
import domain.exception.InconsistentAnomalyStateException;
import domain.valueobject.QualityDecision;
import infrastructure.exception.AnomalyNotFoundException;
import infrastructure.exception.BusinessIdColisionException;
import infrastructure.repository.ConnectionConfig;
import infrastructure.repository.JdbcAnomalyRepository;

class AnomalyCommandServiceTest {
	
	private final static String DESCRIPTION = "anomalyTest";
	private final static String VALID_DOC_ID = "XXX-000-091991";
	private final static String VALID_ACTOR_ID = "0000";
	private final static String SECTOR = "FORGING";
	private final static String MACHINE = "FORGE_PRESS_1";
	private final static String ACTOR_NAME = "Dupont";
	private final static Role PRIVILEGE = Role.SUPERVISOR;
	private final static int QUANTITY = 50;
	private final static int ORDER = 99999;
	private final static String TABLE = "anomaly.anomalies_test";
	private final static QueryContext CONTEXT = new QueryContext(true, SortingSelection.DATE, 1);
	private AnomalyCommandService command;
	private AnomalyQueryService query;
	private JdbcAnomalyRepository repo;
	private ConnectionConfig config ;
	private Actor actor;

	@BeforeEach
	void setUp() throws Exception {
		this.actor = new Actor(VALID_ACTOR_ID, ACTOR_NAME, PRIVILEGE);
		this.config = new ConnectionConfig("jdbc:mysql://localhost:3307/anomaly", "anomaly_user", "anomaly_pass");
		this.repo= new JdbcAnomalyRepository(config,TABLE);
		this.command = new AnomalyCommandService(repo, actor);
		this.query = new AnomalyQueryService(repo);
	}

	@AfterEach
	void tearDown() throws Exception {
		try (Connection connection =
	             DriverManager.getConnection(config.url(), config.user(), config.password())) {

	        connection.createStatement()
	                  .execute("TRUNCATE TABLE " + TABLE);
	    }
	}

	@Test
	void shouldCompleteAnomalyLifecycle() {
		assertSuccess(command.createAnomaly(DESCRIPTION, SECTOR, QUANTITY, ORDER, MACHINE));
		QueryResult<List<AnomalyDto>> anomalies = assertDoesNotThrow(()->query.findByContext(CONTEXT));
			List<AnomalyDto> list = switch (anomalies) {
		    case QuerySuccess<List<AnomalyDto>> ls -> ls.payload();
		    case QueryNotFound<List<AnomalyDto>> nf -> fail("Created anomaly not found.");
		    case QueryFailure<List<AnomalyDto>> fl -> fail("Query failed on created anomaly.");
		};
		UUID anomalyId = UUID.fromString(list.getFirst().id());
		assertSuccess(command.attachCorrectiveAction(anomalyId, VALID_DOC_ID));
		assertSuccess(command.attachQualityDecision(anomalyId, QualityDecision.NA));
		assertSuccess(command.transitionToCorrected(anomalyId));
		assertSuccess(command.attachEvidence(anomalyId, VALID_DOC_ID));
		assertSuccess(command.transitionToResolved(anomalyId));
		assertSuccess(command.transitionToArchived(anomalyId));
		
		QueryResult<AnomalyDto> queryResult = assertDoesNotThrow(()->query.findById(anomalyId));
		AnomalyDto anomalyDto = switch(queryResult) {
		case QuerySuccess<AnomalyDto> success -> success.payload();
		case QueryNotFound<AnomalyDto> notFound ->fail("Archived anomaly not found.");
		case QueryFailure<AnomalyDto> failure ->fail("Query failed on archived anomaly.");
		};
		
		assertEquals(anomalyId.toString(), anomalyDto.id());
		assertNull(anomalyDto.parentId());
		assertNull(anomalyDto.childId());
		assertEquals("ARCHIVED", anomalyDto.anomalyState());
		assertEquals(VALID_DOC_ID, anomalyDto.correctiveActionId());
		assertEquals("NA", anomalyDto.qualityDecision());
		assertEquals(VALID_DOC_ID, anomalyDto.evidenceId());
		assertEquals(DESCRIPTION, anomalyDto.description());
	}
	
	@Test
	void shouldRejectInvalidTransition() {
		assertSuccess(command.createAnomaly(DESCRIPTION, SECTOR, QUANTITY, ORDER, MACHINE));
		QueryResult<List<AnomalyDto>> anomalies = assertDoesNotThrow(()->query.findByContext(CONTEXT));
			List<AnomalyDto> list = switch (anomalies) {
		    case QuerySuccess<List<AnomalyDto>> ls -> ls.payload();
		    case QueryNotFound<List<AnomalyDto>> nf -> fail("Created anomaly not found.");
		    case QueryFailure<List<AnomalyDto>> fl -> fail("Query failed on created anomaly.");
		};
		UUID anomalyId = UUID.fromString(list.getFirst().id());
		assertFail(command.transitionToCorrected(anomalyId));
		assertFail(command.transitionToResolved(anomalyId));
		assertFail(command.transitionToArchived(anomalyId));
	}
	
	@Test
	void transitionToArchivedWithProlongation() {
		assertSuccess(command.createAnomaly(DESCRIPTION, SECTOR, QUANTITY, ORDER, MACHINE));
		QueryResult<List<AnomalyDto>> anomalies = assertDoesNotThrow(()->query.findByContext(CONTEXT));
			List<AnomalyDto> list = switch (anomalies) {
		    case QuerySuccess<List<AnomalyDto>> ls -> ls.payload();
		    case QueryNotFound<List<AnomalyDto>> nf -> fail("Created anomaly not found.");
		    case QueryFailure<List<AnomalyDto>> fl -> fail("Query failed on created anomaly.");
		};
		
		UUID anomalyId = UUID.fromString(list.getFirst().id());
		assertSuccess(command.attachCorrectiveAction(anomalyId, VALID_DOC_ID));
		assertSuccess(command.attachQualityDecision(anomalyId, QualityDecision.NA));
		assertSuccess(command.transitionToCorrected(anomalyId));
		assertSuccess(command.attachEvidence(anomalyId, VALID_DOC_ID));
		assertSuccess(command.transitionToResolved(anomalyId));
		assertSuccess(command.transitionToArchivedWithProlongation(anomalyId, DESCRIPTION));
		
		QueryResult<List<AnomalyDto>> anomaliesAfterProlongation = assertDoesNotThrow(()->query.findByContext(CONTEXT));
		List<AnomalyDto> listAfterProlongation = switch ( anomaliesAfterProlongation) {
		    case QuerySuccess<List<AnomalyDto>> ls -> ls.payload();
		    case QueryNotFound<List<AnomalyDto>> nf -> fail("Created anomaly not found.");
		    case QueryFailure<List<AnomalyDto>> fl -> fail("Query failed on created anomaly.");
		};
		
		assertEquals(2,listAfterProlongation.size());
		
		AnomalyDto parent = listAfterProlongation.get(0).id().equals(anomalyId.toString())?listAfterProlongation.get(0):listAfterProlongation.get(1);
		AnomalyDto child = listAfterProlongation.get(0).id().equals(anomalyId.toString())?listAfterProlongation.get(1):listAfterProlongation.get(0);
		
		assertEquals(anomalyId.toString(), parent.id());
		assertNull(parent.parentId());
		assertEquals(child.id(), parent.childId());
		assertEquals(DESCRIPTION, child.prolongationComent());
		assertEquals("ARCHIVED", parent.anomalyState());
		assertEquals(VALID_DOC_ID, parent.correctiveActionId());
		assertEquals("NA", parent.qualityDecision());
		assertEquals(VALID_DOC_ID, parent.evidenceId());
		assertEquals(DESCRIPTION, parent.description());
		
		assertEquals(parent.id(), child.parentId());
		assertNull(child.childId());
		assertEquals("PENDING", child.anomalyState());
		assertNull(child.correctiveActionId());
		assertEquals("NA", child.qualityDecision());
		assertNull(child.evidenceId());
		assertEquals(DESCRIPTION, child.description());
	}
	
	@Test
	void shouldReturnResultNotFound() {
		QueryResult<AnomalyDto> queryResult = assertDoesNotThrow(()->query.findById(UUID.randomUUID()));
		switch(queryResult) {
			case QuerySuccess<AnomalyDto> success -> fail("Fail anomaly not found expected.");
			case QueryNotFound<AnomalyDto> notFound -> {}
			case QueryFailure<AnomalyDto> failure ->fail("Query failed on archived anomaly.");
		};
	}
	
	@Test
	void createAnomaly_ShouldFail_WhenAllRetriesFail() {
	    AnomalyRepository repo = new AlwaysFailingRepository();
	    AnomalyCommandService service = new AnomalyCommandService(repo, actor);

	    CommandResult result = service.createAnomaly(DESCRIPTION, SECTOR, QUANTITY, ORDER, MACHINE);

	    assertTrue(result instanceof CommandFailure);
	}
	
	@Test
	void createAnomaly_ShouldRetryAndSucceed_WhenCollisionOccursOnce() {
	    FlakyRepository repo = new FlakyRepository();
	    AnomalyCommandService service = new AnomalyCommandService(repo, actor);

	    CommandResult result = service.createAnomaly(DESCRIPTION, SECTOR, QUANTITY, ORDER, MACHINE);

	    assertTrue(result instanceof CommandSuccess);
	    assertEquals(2, repo.getSaveCalls());
	}
	
	private void assertSuccess(CommandResult result) {
		switch(result) {
		case CommandSuccess rs -> {}
		case CommandFailure rs -> fail("Command failed");
		};
	}
	
	private void assertFail(CommandResult result) {
		switch(result) {
		case CommandSuccess rs -> fail("Failure expected.");
		case CommandFailure rs -> {}
		};
	}
	
	class AlwaysFailingRepository implements AnomalyRepository {

	    @Override
	    public void save(Anomaly anomaly) {
	        throw new BusinessIdColisionException();
	    }

	    @Override
	    public int getMaxSequenceByYear(int year) {
	        return 1;
	    }

		@Override
		public void saveAtomic(Anomaly anomaly1, Anomaly anomaly2) {}
		@Override
		public Anomaly findById(UUID id) throws AnomalyNotFoundException, InconsistentAnomalyStateException {return null;}
		@Override
		public List<Anomaly> findByContext(QueryContext context)  {return null;}

	    
	}
	
	class FlakyRepository implements AnomalyRepository {

	    private int saveCalls = 0;

	    @Override
	    public void save(Anomaly anomaly) {
	        saveCalls++;
	        if (saveCalls == 1) {
	            throw new BusinessIdColisionException();
	        }
	    }

	    @Override
	    public int getMaxSequenceByYear(int year) {
	        return saveCalls == 0 ? 1 : 2;
	    }

	    public int getSaveCalls() {
	        return saveCalls;
	    }

	    @Override
		public void saveAtomic(Anomaly anomaly1, Anomaly anomaly2) {}
		@Override
		public Anomaly findById(UUID id) throws AnomalyNotFoundException, InconsistentAnomalyStateException {return null;}
		@Override
		public List<Anomaly> findByContext(QueryContext context)  {return null;}
	}
}
