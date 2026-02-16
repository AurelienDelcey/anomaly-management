package domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class AnomalyTest {
	
	private final static Instant FIXED_INSTANT = Instant.parse("2026-02-16T00:00:00Z");
	private final static String DESCRIPTION = "anomalyTest";
	private final static String VALID_DOC_ID = "XXX-000-091991";
	private final static String VALID_ACTOR_ID = "0000";
	
	@Test
	void constructor_ShouldReturnValidAnomaly() {
		Anomaly anomaly = creatPendingAnomaly();
		assertNotNull(anomaly.getId());
		assertNull(anomaly.getChildId());
		assertNull(anomaly.getParentId());
		assertEquals(VALID_ACTOR_ID, anomaly.getTraceability().getCreation().actorId());
		assertEquals(FIXED_INSTANT, anomaly.getTraceability().getCreation().instant());
		assertEquals(AnomalyState.PENDING, anomaly.getAnomalyState());
		assertNull(anomaly.getCorrectiveAction());
		assertNull(anomaly.getProvingDocument());
		assertEquals(QualityDecision.EMPTY, anomaly.getQualityDecision());
		assertEquals(DESCRIPTION, anomaly.getDescription().description());
	}
	
	@Test
	void prolongationConstructor_ShouldReturnValidAnomaly() {
		UUID parentId = UUID.randomUUID();
		EventTrace creatingTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		Anomaly anomaly = new Anomaly(DESCRIPTION, creatingTrace, parentId);
		assertNotNull(anomaly.getId());
		assertNull(anomaly.getChildId());
		assertNotNull(anomaly.getParentId());
		assertEquals(VALID_ACTOR_ID, anomaly.getTraceability().getCreation().actorId());
		assertEquals(FIXED_INSTANT, anomaly.getTraceability().getCreation().instant());
		assertEquals(AnomalyState.PENDING, anomaly.getAnomalyState());
		assertNull(anomaly.getCorrectiveAction());
		assertNull(anomaly.getProvingDocument());
		assertEquals(QualityDecision.EMPTY, anomaly.getQualityDecision());
		assertEquals(DESCRIPTION, anomaly.getDescription().description());
	}
	
	@Test
	void transitionToCorrected_ShouldReturnValidAnomaly(){
		Anomaly anomaly = creatPendingAnomaly();
		EventTrace toCorrectedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		
		Anomaly anomalyWithCorrectiveAction = assertDoesNotThrow(()-> anomaly.attachCorrectiveAction(VALID_DOC_ID));
		Anomaly anomalyWithQualityDecision = assertDoesNotThrow(()-> anomalyWithCorrectiveAction.attachQualityDecision(QualityDecision.NA));
		Anomaly anomalyCorrected = assertDoesNotThrow(()-> anomalyWithQualityDecision.transitionToCorrected(toCorrectedTrace));
		
		
		assertNotNull(anomalyCorrected.getId());
		assertNull(anomalyCorrected.getChildId());
		assertNull(anomalyCorrected.getParentId());
		assertEquals(VALID_ACTOR_ID, anomalyCorrected.getTraceability().getCreation().actorId());
		assertEquals(FIXED_INSTANT, anomalyCorrected.getTraceability().getCreation().instant());
		assertEquals(VALID_ACTOR_ID, anomalyCorrected.getTraceability().getToCorrected().actorId());
		assertEquals(FIXED_INSTANT, anomalyCorrected.getTraceability().getToCorrected().instant());
		assertEquals(AnomalyState.CORRECTED, anomalyCorrected.getAnomalyState());
		assertEquals(VALID_DOC_ID, anomalyCorrected.getCorrectiveAction().documentId());
		assertNull(anomalyCorrected.getProvingDocument());
		assertEquals(QualityDecision.NA, anomalyCorrected.getQualityDecision());
		assertEquals(DESCRIPTION, anomalyCorrected.getDescription().description());
	}
	
	@Test
	void transitionToResolved_ShouldReturnValidAnomaly() {
		Anomaly anomaly = creatPendingAnomaly();
		EventTrace toCorrectedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		EventTrace toResolvedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		
		Anomaly anomalyWithCorrectiveAction = assertDoesNotThrow(()-> anomaly.attachCorrectiveAction(VALID_DOC_ID));
		Anomaly anomalyWithQualityDecision = assertDoesNotThrow(()-> anomalyWithCorrectiveAction.attachQualityDecision(QualityDecision.NA));
		Anomaly anomalyCorrected = assertDoesNotThrow(()-> anomalyWithQualityDecision.transitionToCorrected(toCorrectedTrace));
		Anomaly anomalyWithProvingDocuments = assertDoesNotThrow(()-> anomalyCorrected.attachProvingDocument(VALID_DOC_ID));
		Anomaly anomalyResolved = assertDoesNotThrow(()-> anomalyWithProvingDocuments.transitionToResolved(toResolvedTrace));
		
		
		assertNotNull(anomalyResolved.getId());
		assertNull(anomalyResolved.getChildId());
		assertNull(anomalyResolved.getParentId());
		assertEquals(VALID_ACTOR_ID, anomalyResolved.getTraceability().getCreation().actorId());
		assertEquals(FIXED_INSTANT, anomalyResolved.getTraceability().getCreation().instant());
		assertEquals(VALID_ACTOR_ID, anomalyResolved.getTraceability().getToCorrected().actorId());
		assertEquals(FIXED_INSTANT, anomalyResolved.getTraceability().getToCorrected().instant());
		assertEquals(VALID_ACTOR_ID, anomalyResolved.getTraceability().getToResolved().actorId());
		assertEquals(FIXED_INSTANT, anomalyResolved.getTraceability().getToResolved().instant());
		assertEquals(AnomalyState.RESOLVED, anomalyResolved.getAnomalyState());
		assertEquals(VALID_DOC_ID, anomalyResolved.getCorrectiveAction().documentId());
		assertEquals(VALID_DOC_ID, anomalyResolved.getProvingDocument().documentId());
		assertEquals(QualityDecision.NA, anomalyResolved.getQualityDecision());
		assertEquals(DESCRIPTION, anomalyResolved.getDescription().description());
	}
	
	@Test
	void transitionToArchived_ShouldReturnValidAnomaly(){
		Anomaly anomaly = creatPendingAnomaly();
		EventTrace toCorrectedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		EventTrace toResolvedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		EventTrace toArchivedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		
		Anomaly anomalyWithCorrectiveAction = assertDoesNotThrow(()-> anomaly.attachCorrectiveAction(VALID_DOC_ID));
		Anomaly anomalyWithQualityDecision = assertDoesNotThrow(()-> anomalyWithCorrectiveAction.attachQualityDecision(QualityDecision.NA));
		Anomaly anomalyCorrected = assertDoesNotThrow(()-> anomalyWithQualityDecision.transitionToCorrected(toCorrectedTrace));
		Anomaly anomalyWithProvingDocuments = assertDoesNotThrow(()-> anomalyCorrected.attachProvingDocument(VALID_DOC_ID));
		Anomaly anomalyResolved = assertDoesNotThrow(()-> anomalyWithProvingDocuments.transitionToResolved(toResolvedTrace));
		Anomaly anomalyArchived = assertDoesNotThrow(()-> anomalyResolved.transitionToArchived(toArchivedTrace));
		
		
		assertNotNull(anomalyArchived.getId());
		assertNull(anomalyArchived.getChildId());
		assertNull(anomalyArchived.getParentId());
		assertEquals(VALID_ACTOR_ID, anomalyArchived.getTraceability().getCreation().actorId());
		assertEquals(FIXED_INSTANT, anomalyArchived.getTraceability().getCreation().instant());
		assertEquals(VALID_ACTOR_ID, anomalyArchived.getTraceability().getToCorrected().actorId());
		assertEquals(FIXED_INSTANT, anomalyArchived.getTraceability().getToCorrected().instant());
		assertEquals(VALID_ACTOR_ID, anomalyArchived.getTraceability().getToResolved().actorId());
		assertEquals(FIXED_INSTANT, anomalyArchived.getTraceability().getToResolved().instant());
		assertEquals(VALID_ACTOR_ID, anomalyArchived.getTraceability().getToArchived().actorId());
		assertEquals(FIXED_INSTANT, anomalyArchived.getTraceability().getToArchived().instant());
		assertEquals(AnomalyState.ARCHIVED, anomalyArchived.getAnomalyState());
		assertEquals(VALID_DOC_ID, anomalyArchived.getCorrectiveAction().documentId());
		assertEquals(VALID_DOC_ID, anomalyArchived.getProvingDocument().documentId());
		assertEquals(QualityDecision.NA, anomalyArchived.getQualityDecision());
		assertEquals(DESCRIPTION, anomalyArchived.getDescription().description());
	}
	
	@Test
	void attachProlongationId_ShouldReturnValidAnomaly(){
		Anomaly anomaly = assertDoesNotThrow(()-> creatArchivedAnomaly());
		Anomaly anomalyWithProlongationId = assertDoesNotThrow(()-> anomaly.attachProlongationId(UUID.randomUUID()));
		
		assertNotNull(anomalyWithProlongationId.getId());
		assertNotNull(anomalyWithProlongationId.getChildId());
		assertNull(anomalyWithProlongationId.getParentId());
		assertEquals(VALID_ACTOR_ID, anomalyWithProlongationId.getTraceability().getCreation().actorId());
		assertEquals(FIXED_INSTANT, anomalyWithProlongationId.getTraceability().getCreation().instant());
		assertEquals(VALID_ACTOR_ID, anomalyWithProlongationId.getTraceability().getToCorrected().actorId());
		assertEquals(FIXED_INSTANT, anomalyWithProlongationId.getTraceability().getToCorrected().instant());
		assertEquals(VALID_ACTOR_ID, anomalyWithProlongationId.getTraceability().getToResolved().actorId());
		assertEquals(FIXED_INSTANT, anomalyWithProlongationId.getTraceability().getToResolved().instant());
		assertEquals(VALID_ACTOR_ID, anomalyWithProlongationId.getTraceability().getToArchived().actorId());
		assertEquals(FIXED_INSTANT, anomalyWithProlongationId.getTraceability().getToArchived().instant());
		assertEquals(AnomalyState.ARCHIVED, anomalyWithProlongationId.getAnomalyState());
		assertEquals(VALID_DOC_ID, anomalyWithProlongationId.getCorrectiveAction().documentId());
		assertEquals(VALID_DOC_ID, anomalyWithProlongationId.getProvingDocument().documentId());
		assertEquals(QualityDecision.NA, anomalyWithProlongationId.getQualityDecision());
		assertEquals(DESCRIPTION, anomalyWithProlongationId.getDescription().description());
	}
	
	@Test
	void attachCorrectiveAction_ShouldReturnAnomalyWithCorrectiveAction(){
		Anomaly anomaly = assertDoesNotThrow(()-> creatPendingAnomaly());
		
		Anomaly anomalyWithCorrectiveAction = assertDoesNotThrow(()->anomaly.attachCorrectiveAction(VALID_DOC_ID));
		assertEquals(VALID_DOC_ID, anomalyWithCorrectiveAction.getCorrectiveAction().documentId());
	}
	
	@Test
	void attachQualityDecision_ShouldReturnAnomalyWithQualityDecision(){
		Anomaly anomaly = assertDoesNotThrow(()-> creatPendingAnomaly());
		
		Anomaly anomalyWithQualityDecisionNA = assertDoesNotThrow(()-> anomaly.attachQualityDecision(QualityDecision.NA));		
		assertEquals(QualityDecision.NA, anomalyWithQualityDecisionNA.getQualityDecision());
		Anomaly anomalyWithQualityDecisionREPAIR = assertDoesNotThrow(()-> anomaly.attachQualityDecision(QualityDecision.REPAIR));
		assertEquals(QualityDecision.REPAIR, anomalyWithQualityDecisionREPAIR.getQualityDecision());
		Anomaly anomalyWithQualityDecisionSCRAP = assertDoesNotThrow(()-> anomaly.attachQualityDecision(QualityDecision.SCRAP));
		assertEquals(QualityDecision.SCRAP, anomalyWithQualityDecisionSCRAP.getQualityDecision());
	}
	
	@Test
	void attachProvingDocument_ShouldReturnAnomalyWithProvingDocument(){
		Anomaly anomaly = assertDoesNotThrow(()-> creatCorrectedAnomaly());
		
		Anomaly anomalyWithProvingDocuments = assertDoesNotThrow(()-> anomaly.attachProvingDocument(VALID_DOC_ID));
		assertEquals(VALID_DOC_ID, anomalyWithProvingDocuments.getProvingDocument().documentId());
	}
	
	private Anomaly creatPendingAnomaly() {
		EventTrace creatingTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		return new Anomaly(DESCRIPTION, creatingTrace);
	}
	
	private Anomaly creatCorrectedAnomaly() throws IllegalAttachment, IllegalTransition, IllegalTraceErasureTentative {
		EventTrace correctedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		Anomaly anomaly = creatPendingAnomaly().attachCorrectiveAction(VALID_DOC_ID);
		anomaly = anomaly.attachQualityDecision(QualityDecision.NA);
		return anomaly.transitionToCorrected(correctedTrace);
	}
	
	private Anomaly creatResolvedAnomaly() throws IllegalAttachment, IllegalTransition, IllegalTraceErasureTentative {
		EventTrace resolvedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		Anomaly anomaly = creatCorrectedAnomaly().attachProvingDocument(VALID_DOC_ID);
		return anomaly.transitionToResolved(resolvedTrace);
	}
	
	private Anomaly creatArchivedAnomaly() throws IllegalTransition, IllegalTraceErasureTentative, IllegalAttachment {
		EventTrace archivedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		return creatResolvedAnomaly().transitionToArchived(archivedTrace);
	}
}
