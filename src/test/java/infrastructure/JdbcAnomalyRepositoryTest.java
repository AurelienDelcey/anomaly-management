package infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import application.query.QueryContext;
import application.query.SortingSelection;
import domain.anomaly.Anomaly;
import domain.exception.IllegalAttachment;
import domain.exception.IllegalTraceErasureTentative;
import domain.exception.IllegalTransition;
import domain.exception.InconsistentAnomalyStateException;
import domain.traceability.EventTrace;
import domain.valueobject.BusinessId;
import domain.valueobject.Description;
import domain.valueobject.ImpactedQuantity;
import domain.valueobject.Machine;
import domain.valueobject.ProductionOrder;
import domain.valueobject.ProlongationContext;
import domain.valueobject.QualityDecision;
import domain.valueobject.Sector;
import infrastructure.exception.AnomalyNotFoundException;
import infrastructure.exception.BusinessIdColisionException;
import infrastructure.repository.ConnectionConfig;
import infrastructure.repository.JdbcAnomalyRepository;

class JdbcAnomalyRepositoryTest {
	
	private static final Instant FIXED_INSTANT = Instant.parse("2026-02-16T00:00:00Z");
	private static final Instant FIXED_INSTANT_LATER = Instant.parse("2026-02-17T00:00:00Z");
	private static final String DESCRIPTION = "anomalyTest";
	private static final String VALID_DOC_ID = "XXX-000-091991";
	private static final String VALID_ACTOR_ID = "0000";
	private static final String TABLE = "anomaly.anomalies_test";
	private static final String VALID_ACTOR_NAME = "Dupont";
	private static final QueryContext CONTEXT = new QueryContext(true, SortingSelection.DATE, 1);
	private static final int FIXED_YEAR = 2026;
	private static final int FIXED_SEQUENCE = 1;
	private static final int QUANTITY = 50;
	private static final int ORDER = 99999;
	private static JdbcAnomalyRepository repo;
	private static ConnectionConfig config;
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		config = new ConnectionConfig("jdbc:mysql://localhost:3306/anomaly", "anomaly_user", "anomaly2026");
		try {
			repo = new JdbcAnomalyRepository(config, TABLE);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	void saveAndFindById_shouldReturnTheSameAnomaly_whenAnomalyStateIsPending() {
		Anomaly anomaly = createPendingAnomaly();
		repo.save(anomaly);
		Anomaly newAnomaly = assertDoesNotThrow(()->repo.findById(anomaly.getId()));
		
		assertEquals(anomaly.getId().toString(), newAnomaly.getId().toString());
		assertEquals(anomaly.getBusinessId().year(), newAnomaly.getBusinessId().year());
		assertEquals( anomaly.getBusinessId().sequence(), newAnomaly.getBusinessId().sequence());
		assertNull(newAnomaly.getChildId());
		assertNull(newAnomaly.getProlongationContext());
		assertEquals(anomaly.getTraceability().getCreation().actorId(), newAnomaly.getTraceability().getCreation().actorId());
		assertEquals(anomaly.getTraceability().getCreation().instant(), newAnomaly.getTraceability().getCreation().instant());
		assertNull(newAnomaly.getTraceability().getToCorrected());
		assertNull(newAnomaly.getTraceability().getToResolved());
		assertNull(newAnomaly.getTraceability().getToArchived());
		assertEquals(anomaly.getAnomalyState(), newAnomaly.getAnomalyState());
		assertNull(newAnomaly.getCorrectiveAction());
		assertNull(newAnomaly.getEvidence());
		assertEquals(anomaly.getQualityDecision(), newAnomaly.getQualityDecision());
		assertEquals(anomaly.getDescription().description(), newAnomaly.getDescription().description());
	}
	
	@Test
	void saveAndFindById_shouldReturnTheSameAnomaly_whenAnomalyStateIsCorrected() {
		Anomaly anomaly = assertDoesNotThrow(()->createCorrectedAnomaly());
		repo.save(anomaly);
		Anomaly newAnomaly = assertDoesNotThrow(()->repo.findById(anomaly.getId()));
		
		assertEquals(anomaly.getId().toString(), newAnomaly.getId().toString());
		assertEquals(anomaly.getBusinessId().year(), newAnomaly.getBusinessId().year());
		assertEquals( anomaly.getBusinessId().sequence(), newAnomaly.getBusinessId().sequence());
		assertNull(newAnomaly.getChildId());
		assertNull(newAnomaly.getProlongationContext());
		assertEquals(anomaly.getTraceability().getCreation().actorId(), newAnomaly.getTraceability().getCreation().actorId());
		assertEquals(anomaly.getTraceability().getCreation().instant(), newAnomaly.getTraceability().getCreation().instant());
		assertEquals(anomaly.getTraceability().getToCorrected().actorId(), newAnomaly.getTraceability().getToCorrected().actorId());
		assertEquals(anomaly.getTraceability().getToCorrected().instant(), newAnomaly.getTraceability().getToCorrected().instant());
		assertNull(newAnomaly.getTraceability().getToResolved());
		assertNull(newAnomaly.getTraceability().getToArchived());
		assertEquals(anomaly.getAnomalyState(), newAnomaly.getAnomalyState());
		assertEquals(anomaly.getCorrectiveAction().documentId(), newAnomaly.getCorrectiveAction().documentId());
		assertNull(newAnomaly.getEvidence());
		assertEquals(anomaly.getQualityDecision(), newAnomaly.getQualityDecision());
		assertEquals(anomaly.getDescription().description(), newAnomaly.getDescription().description());
	}
	
	@Test
	void saveAndFindById_shouldReturnTheSameAnomaly_whenAnomalyStateIsResolved() {
		Anomaly anomaly = assertDoesNotThrow(()->createResolvedAnomaly());
		repo.save(anomaly);
		Anomaly newAnomaly = assertDoesNotThrow(()->repo.findById(anomaly.getId()));
		
		assertEquals(anomaly.getId().toString(), newAnomaly.getId().toString());
		assertEquals(anomaly.getBusinessId().year(), newAnomaly.getBusinessId().year());
		assertEquals( anomaly.getBusinessId().sequence(), newAnomaly.getBusinessId().sequence());
		assertNull(newAnomaly.getChildId());
		assertNull(newAnomaly.getProlongationContext());
		assertEquals(anomaly.getTraceability().getCreation().actorId(), newAnomaly.getTraceability().getCreation().actorId());
		assertEquals(anomaly.getTraceability().getCreation().instant(), newAnomaly.getTraceability().getCreation().instant());
		assertEquals(anomaly.getTraceability().getToCorrected().actorId(), newAnomaly.getTraceability().getToCorrected().actorId());
		assertEquals(anomaly.getTraceability().getToCorrected().instant(), newAnomaly.getTraceability().getToCorrected().instant());
		assertEquals(anomaly.getTraceability().getToResolved().actorId(), newAnomaly.getTraceability().getToResolved().actorId());
		assertEquals(anomaly.getTraceability().getToResolved().instant(), newAnomaly.getTraceability().getToResolved().instant());
		assertNull(newAnomaly.getTraceability().getToArchived());
		assertEquals(anomaly.getAnomalyState(), newAnomaly.getAnomalyState());
		assertEquals(anomaly.getCorrectiveAction().documentId(), newAnomaly.getCorrectiveAction().documentId());
		assertEquals(anomaly.getEvidence().documentId(), newAnomaly.getEvidence().documentId());
		assertEquals(anomaly.getQualityDecision(), newAnomaly.getQualityDecision());
		assertEquals(anomaly.getDescription().description(), newAnomaly.getDescription().description());
	}
	
	@Test
	void saveAndFindById_shouldReturnTheSameAnomaly_whenAnomalyStateIsArchived() {
		Anomaly anomaly = assertDoesNotThrow(()->createArchivedAnomaly());
		repo.save(anomaly);
		Anomaly newAnomaly = assertDoesNotThrow(()->repo.findById(anomaly.getId()));
		
		assertEquals(anomaly.getId().toString(), newAnomaly.getId().toString());
		assertEquals(anomaly.getBusinessId().year(), newAnomaly.getBusinessId().year());
		assertEquals( anomaly.getBusinessId().sequence(), newAnomaly.getBusinessId().sequence());
		assertNull(newAnomaly.getChildId());
		assertNull(newAnomaly.getProlongationContext());
		assertEquals(anomaly.getTraceability().getCreation().actorId(), newAnomaly.getTraceability().getCreation().actorId());
		assertEquals(anomaly.getTraceability().getCreation().instant(), newAnomaly.getTraceability().getCreation().instant());
		assertEquals(anomaly.getTraceability().getToCorrected().actorId(), newAnomaly.getTraceability().getToCorrected().actorId());
		assertEquals(anomaly.getTraceability().getToCorrected().instant(), newAnomaly.getTraceability().getToCorrected().instant());
		assertEquals(anomaly.getTraceability().getToResolved().actorId(), newAnomaly.getTraceability().getToResolved().actorId());
		assertEquals(anomaly.getTraceability().getToResolved().instant(), newAnomaly.getTraceability().getToResolved().instant());
		assertEquals(anomaly.getTraceability().getToArchived().actorId(), newAnomaly.getTraceability().getToArchived().actorId());
		assertEquals(anomaly.getTraceability().getToArchived().instant(), newAnomaly.getTraceability().getToArchived().instant());
		assertEquals(anomaly.getAnomalyState(), newAnomaly.getAnomalyState());
		assertEquals(anomaly.getCorrectiveAction().documentId(), newAnomaly.getCorrectiveAction().documentId());
		assertEquals(anomaly.getEvidence().documentId(), newAnomaly.getEvidence().documentId());
		assertEquals(anomaly.getQualityDecision(), newAnomaly.getQualityDecision());
		assertEquals(anomaly.getDescription().description(), newAnomaly.getDescription().description());
	}
	
	@Test
	void saveAtomicAndFindAll_shouldReturnSameAnomalies() {
		Anomaly parentAnomaly = assertDoesNotThrow(()->createArchivedAnomaly());
		repo.save(parentAnomaly);
		Anomaly childAnomaly = createPendingProlongationAnomaly(parentAnomaly.getId(), DESCRIPTION);
		Anomaly parentAnomalyWithProlongationId = assertDoesNotThrow(()->parentAnomaly.linkProlongation(childAnomaly.getId()));
		
		repo.saveAtomic(parentAnomalyWithProlongationId, childAnomaly);
		List<Anomaly> anomalies = assertDoesNotThrow(()->repo.findByContext(CONTEXT));
		
		assertEquals(2,anomalies.size());
		
		assertEquals(parentAnomalyWithProlongationId.getId().toString(), anomalies.get(1).getId().toString());
		assertEquals(parentAnomalyWithProlongationId.getBusinessId().year(), anomalies.get(1).getBusinessId().year());
		assertEquals( parentAnomalyWithProlongationId.getBusinessId().sequence(), anomalies.get(1).getBusinessId().sequence());
		assertEquals(childAnomaly.getId(),anomalies.get(1).getChildId());
		assertNull(anomalies.get(1).getProlongationContext());
		assertEquals(parentAnomalyWithProlongationId.getTraceability().getCreation().actorId(), anomalies.get(1).getTraceability().getCreation().actorId());
		assertEquals(parentAnomalyWithProlongationId.getTraceability().getCreation().instant(), anomalies.get(1).getTraceability().getCreation().instant());
		assertEquals(parentAnomalyWithProlongationId.getTraceability().getToCorrected().actorId(), anomalies.get(1).getTraceability().getToCorrected().actorId());
		assertEquals(parentAnomalyWithProlongationId.getTraceability().getToCorrected().instant(), anomalies.get(1).getTraceability().getToCorrected().instant());
		assertEquals(parentAnomalyWithProlongationId.getTraceability().getToResolved().actorId(), anomalies.get(1).getTraceability().getToResolved().actorId());
		assertEquals(parentAnomalyWithProlongationId.getTraceability().getToResolved().instant(), anomalies.get(1).getTraceability().getToResolved().instant());
		assertEquals(parentAnomalyWithProlongationId.getTraceability().getToArchived().actorId(), anomalies.get(1).getTraceability().getToArchived().actorId());
		assertEquals(parentAnomalyWithProlongationId.getTraceability().getToArchived().instant(), anomalies.get(1).getTraceability().getToArchived().instant());
		assertEquals(parentAnomalyWithProlongationId.getAnomalyState(), anomalies.get(1).getAnomalyState());
		assertEquals(parentAnomalyWithProlongationId.getCorrectiveAction().documentId(), anomalies.get(1).getCorrectiveAction().documentId());
		assertEquals(parentAnomalyWithProlongationId.getEvidence().documentId(), anomalies.get(1).getEvidence().documentId());
		assertEquals(parentAnomalyWithProlongationId.getQualityDecision(), anomalies.get(1).getQualityDecision());
		assertEquals(parentAnomalyWithProlongationId.getDescription().description(), anomalies.get(1).getDescription().description());
		
		assertEquals(childAnomaly.getId().toString(), anomalies.get(0).getId().toString());
		assertEquals(childAnomaly.getBusinessId().year(), anomalies.get(0).getBusinessId().year());
		assertEquals( childAnomaly.getBusinessId().sequence(), anomalies.get(0).getBusinessId().sequence());
		assertNull(anomalies.get(0).getChildId());
		assertEquals(parentAnomalyWithProlongationId.getId(),anomalies.get(0).getProlongationContext().parentId());
		assertEquals(childAnomaly.getTraceability().getCreation().actorId(), anomalies.get(0).getTraceability().getCreation().actorId());
		assertEquals(childAnomaly.getTraceability().getCreation().instant(), anomalies.get(0).getTraceability().getCreation().instant());
		assertNull(anomalies.get(0).getTraceability().getToCorrected());
		assertNull(anomalies.get(0).getTraceability().getToResolved());
		assertNull(anomalies.get(0).getTraceability().getToArchived());
		assertEquals(childAnomaly.getAnomalyState(), anomalies.get(0).getAnomalyState());
		assertNull(anomalies.get(0).getCorrectiveAction());
		assertNull(anomalies.get(0).getEvidence());
		assertEquals(childAnomaly.getQualityDecision(), anomalies.get(0).getQualityDecision());
		assertEquals(childAnomaly.getDescription().description(), anomalies.get(0).getDescription().description());
		
	}
	
	@Test
	void findById_ShouldThrowException_WhenAnomalyIsNotFound() {
		assertThrows(AnomalyNotFoundException.class, ()->repo.findById(UUID.randomUUID()));
	}
	
	@Test
	void save_ShouldThrowBusinessIdColisonException_WhenTheSameYearAndSequenceAlreadyExist() {
		Anomaly anomaly = assertDoesNotThrow(()->createArchivedAnomaly());
		repo.save(anomaly);
		Anomaly anomaly2 = assertDoesNotThrow(()->createArchivedAnomaly());
		assertThrows(BusinessIdColisionException.class, ()->repo.save(anomaly2));
	}
	
	private Anomaly createPendingAnomaly() {
		EventTrace creationTrace = new EventTrace(VALID_ACTOR_ID, VALID_ACTOR_NAME, FIXED_INSTANT);
		return new Anomaly(getValidBusinessId(), getValidDescription(), Sector.FORGING, getValidQuantity(), getValideProductionOrder(), Machine.MACHINE_1, creationTrace);
	}
	
	private Anomaly createPendingProlongationAnomaly(UUID parentId, String comment) {
		EventTrace creationTrace = new EventTrace(VALID_ACTOR_ID, VALID_ACTOR_NAME, FIXED_INSTANT_LATER);
		ProlongationContext prolongationContext = new ProlongationContext(parentId, comment);
		return new Anomaly(QualityDecision.NA, getValidBusinessIdForProlongation(), getValidDescription(), Sector.FORGING, getValidQuantity(), getValideProductionOrder(), Machine.MACHINE_1, creationTrace, prolongationContext);
	}
	
	private Anomaly createCorrectedAnomaly() throws IllegalAttachment, IllegalTransition, IllegalTraceErasureTentative, InconsistentAnomalyStateException {
		EventTrace correctedTrace = new EventTrace(VALID_ACTOR_ID, VALID_ACTOR_NAME, FIXED_INSTANT);
		Anomaly anomaly = createPendingAnomaly().attachCorrectiveAction(VALID_DOC_ID);
		anomaly = anomaly.attachQualityDecision(QualityDecision.NA);
		return anomaly.transitionToCorrected(correctedTrace);
	}
	
	private Anomaly createResolvedAnomaly() throws IllegalAttachment, IllegalTransition, IllegalTraceErasureTentative, InconsistentAnomalyStateException {
		EventTrace resolvedTrace = new EventTrace(VALID_ACTOR_ID, VALID_ACTOR_NAME, FIXED_INSTANT);
		Anomaly anomaly = createCorrectedAnomaly().attachEvidence(VALID_DOC_ID);
		return anomaly.transitionToResolved(resolvedTrace);
	}
	
	private Anomaly createArchivedAnomaly() throws IllegalTransition, IllegalTraceErasureTentative, IllegalAttachment, InconsistentAnomalyStateException {
		EventTrace archivedTrace = new EventTrace(VALID_ACTOR_ID, VALID_ACTOR_NAME, FIXED_INSTANT);
		return createResolvedAnomaly().transitionToArchived(archivedTrace);
	}
	
	private BusinessId getValidBusinessId() {
		return new BusinessId(FIXED_YEAR, FIXED_SEQUENCE);
	}
	
	private BusinessId getValidBusinessIdForProlongation() {
		return new BusinessId(FIXED_YEAR, FIXED_SEQUENCE+1);
	}
	
	private Description getValidDescription() {
		return new Description(DESCRIPTION);
	}
	
	private ImpactedQuantity getValidQuantity() {
		return new ImpactedQuantity(QUANTITY);
	}
	
	private ProductionOrder getValideProductionOrder() {
		return new ProductionOrder(ORDER);
	}
}
