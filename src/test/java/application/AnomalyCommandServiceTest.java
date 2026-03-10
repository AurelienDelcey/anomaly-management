package application;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;


import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import application.command.AnomalyCommandService;
import application.command.CommandFailure;
import application.command.CommandResult;
import application.command.CommandSuccess;
import application.dto.AnomalyDto;
import application.query.AnomalyQueryService;
import application.query.QueryFailure;
import application.query.QueryNotFound;
import application.query.QueryResult;
import application.query.QuerySuccess;
import domain.actor.Actor;
import domain.anomaly.AnomalyState;
import domain.valueobject.QualityDecision;
import infrastructure.repository.ConnectionConfig;
import infrastructure.repository.JdbcAnomalyRepository;

class AnomalyCommandServiceTest {
	
	private final static String DESCRIPTION = "anomalyTest";
	private final static String VALID_DOC_ID = "XXX-000-091991";
	private final static String VALID_ACTOR_ID = "0000";
	private final static int PRIVILEGE = 0;
	private static final String TABLE = "anomaly.anomalies_test";
	private AnomalyCommandService command;
	private AnomalyQueryService query;
	private JdbcAnomalyRepository repo;
	private ConnectionConfig config ;
	private Actor actor;

	@BeforeEach
	void setUp() throws Exception {
		this.actor = new Actor(VALID_ACTOR_ID, PRIVILEGE);
		this.config = new ConnectionConfig("jdbc:mysql://localhost:3306/anomaly", "anomaly_user", "anomaly2026");
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
	void ShouldCompleteAnomalyLifecycle() {
		assertSuccess(command.createAnomaly(DESCRIPTION));
		QueryResult<List<AnomalyDto>> anomalies = assertDoesNotThrow(()->query.getAll(1));
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
		
		QueryResult<AnomalyDto> queryResult = assertDoesNotThrow(()->query.getById(anomalyId));
		AnomalyDto anomalyDto = switch(queryResult) {
		case QuerySuccess<AnomalyDto> success -> success.payload();
		case QueryNotFound<AnomalyDto> notFound ->fail("Archived anomaly not found.");
		case QueryFailure<AnomalyDto> failure ->fail("Query failed on archived anomaly.");
		};
		
		assertEquals(anomalyId.toString(), anomalyDto.id());
		assertNull(anomalyDto.parentId());
		assertNull(anomalyDto.childId());
		assertEquals(AnomalyState.ARCHIVED, anomalyDto.anomalyState());
		assertEquals(VALID_DOC_ID, anomalyDto.correctiveActionId());
		assertEquals(QualityDecision.NA, anomalyDto.qualityDecision());
		assertEquals(VALID_DOC_ID, anomalyDto.evidenceId());
		assertEquals(DESCRIPTION, anomalyDto.description());
	}
	
	@Test
	void shouldRejectInvalidTransition() {
		assertSuccess(command.createAnomaly(DESCRIPTION));
		QueryResult<List<AnomalyDto>> anomalies = assertDoesNotThrow(()->query.getAll(1));
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
		assertSuccess(command.createAnomaly(DESCRIPTION));
		QueryResult<List<AnomalyDto>> anomalies = assertDoesNotThrow(()->query.getAll(1));
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
		assertSuccess(command.transitionToArchivedWithProlongation(anomalyId));
		
		QueryResult<List<AnomalyDto>> anomaliesAfterProlongation = assertDoesNotThrow(()->query.getAll(1));
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
		assertEquals(AnomalyState.ARCHIVED, parent.anomalyState());
		assertEquals(VALID_DOC_ID, parent.correctiveActionId());
		assertEquals(QualityDecision.NA, parent.qualityDecision());
		assertEquals(VALID_DOC_ID, parent.evidenceId());
		assertEquals(DESCRIPTION, parent.description());
		
		assertEquals(parent.id(), child.parentId());
		assertNull(child.childId());
		assertEquals(AnomalyState.PENDING, child.anomalyState());
		assertNull(child.correctiveActionId());
		assertEquals(QualityDecision.EMPTY, child.qualityDecision());
		assertNull(child.evidenceId());
		assertEquals(DESCRIPTION, child.description());
	}
	
	@Test
	void shouldReturnResultNotFound() {
		QueryResult<AnomalyDto> queryResult = assertDoesNotThrow(()->query.getById(UUID.randomUUID()));
		switch(queryResult) {
			case QuerySuccess<AnomalyDto> success -> fail("Fail anomaly not found expected.");
			case QueryNotFound<AnomalyDto> notFound -> {}
			case QueryFailure<AnomalyDto> failure ->fail("Query failed on archived anomaly.");
		};
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
}
