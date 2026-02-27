package domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import domain.anomaly.Anomaly;
import domain.anomaly.AnomalyState;
import domain.exception.IllegalAttachment;
import domain.exception.IllegalTraceErasureTentative;
import domain.exception.IllegalTransition;
import domain.exception.InconsistentAnomalyStateException;
import domain.traceability.EventTrace;
import domain.valueobject.QualityDecision;

class AnomalyTest {
	
	private final static Instant FIXED_INSTANT = Instant.parse("2026-02-16T00:00:00Z");
	private final static String DESCRIPTION = "anomalyTest";
	private final static String VALID_DOC_ID = "XXX-000-091991";
	private final static String VALID_ACTOR_ID = "0000";
	
	@Test
	void constructor_ShouldReturnValidAnomaly() {
		Anomaly anomaly = createPendingAnomaly();
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
		Anomaly anomaly = createPendingAnomaly();
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
		Anomaly anomaly = createPendingAnomaly();
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
		Anomaly anomaly = createPendingAnomaly();
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
		Anomaly anomaly = assertDoesNotThrow(()-> createArchivedAnomaly());
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
		Anomaly anomaly = assertDoesNotThrow(()-> createPendingAnomaly());
		
		Anomaly anomalyWithCorrectiveAction = assertDoesNotThrow(()->anomaly.attachCorrectiveAction(VALID_DOC_ID));
		assertEquals(VALID_DOC_ID, anomalyWithCorrectiveAction.getCorrectiveAction().documentId());
	}
	
	@Test
	void attachQualityDecision_ShouldReturnAnomalyWithQualityDecision(){
		Anomaly anomaly = assertDoesNotThrow(()-> createPendingAnomaly());
		
		Anomaly anomalyWithQualityDecisionNA = assertDoesNotThrow(()-> anomaly.attachQualityDecision(QualityDecision.NA));		
		assertEquals(QualityDecision.NA, anomalyWithQualityDecisionNA.getQualityDecision());
		Anomaly anomalyWithQualityDecisionREPAIR = assertDoesNotThrow(()-> anomaly.attachQualityDecision(QualityDecision.REPAIR));
		assertEquals(QualityDecision.REPAIR, anomalyWithQualityDecisionREPAIR.getQualityDecision());
		Anomaly anomalyWithQualityDecisionSCRAP = assertDoesNotThrow(()-> anomaly.attachQualityDecision(QualityDecision.SCRAP));
		assertEquals(QualityDecision.SCRAP, anomalyWithQualityDecisionSCRAP.getQualityDecision());
	}
	
	@Test
	void attachProvingDocument_ShouldReturnAnomalyWithProvingDocument(){
		Anomaly anomaly = assertDoesNotThrow(()-> createCorrectedAnomaly());
		
		Anomaly anomalyWithProvingDocuments = assertDoesNotThrow(()-> anomaly.attachProvingDocument(VALID_DOC_ID));
		assertEquals(VALID_DOC_ID, anomalyWithProvingDocuments.getProvingDocument().documentId());
	}
	
	/*@ParameterizedTest // EXTRACT IN DescrptionTest CLASS.
	@NullAndEmptySource
	@ValueSource(strings = {""," ","   ","\n"})
	void creatingAnomalyWithInvalidDescription_ShouldThrowException(String description) {
		EventTrace creatingTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		assertThrows(IllegalArgumentException.class, ()-> new Anomaly(description,creatingTrace));
	}*/
	
	@Test
	void transitionToCorrected_ShouldThrowException_WhenCorrectiveActionIsMissing() {
		Anomaly anomaly = createPendingAnomaly();
		Anomaly anomalyWithQualityDecision = assertDoesNotThrow(()->anomaly.attachQualityDecision(QualityDecision.NA));
		EventTrace correctedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		
		assertThrows(IllegalTransition.class, ()->anomalyWithQualityDecision.transitionToCorrected(correctedTrace));
	}
	
	@Test
	void transitionToCorrected_ShouldThrowException_WhenQualityDecisionIsMissing() {
		Anomaly anomaly = createPendingAnomaly();
		Anomaly anomalyWithCorrectiveAction = assertDoesNotThrow(()->anomaly.attachCorrectiveAction(VALID_DOC_ID));
		EventTrace correctedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		
		assertThrows(IllegalTransition.class, ()->anomalyWithCorrectiveAction.transitionToCorrected(correctedTrace));
	}
	
	@Test
	void transitionToResolved_ShouldThrowException_WhenProvingDocumentIsMissing() {
		Anomaly anomaly = assertDoesNotThrow(()->createCorrectedAnomaly());
		EventTrace resolvedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		
		assertThrows(IllegalTransition.class, ()->anomaly.transitionToResolved(resolvedTrace));
	}
	
	@ParameterizedTest
	@EnumSource(
			value = AnomalyState.class,
			mode = EnumSource.Mode.EXCLUDE,
			names = "PENDING")
	void transitionToCorrected_ShouldThrowException_WhenAnomalyStateIsNotPending(AnomalyState state) {
		Anomaly anomaly = assertDoesNotThrow(()-> getValidAnomaly(state));
		EventTrace correctedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		assertThrows(IllegalTransition.class, ()->anomaly.transitionToCorrected(correctedTrace));
	}
	
	@ParameterizedTest
	@EnumSource(
			value = AnomalyState.class,
			mode = EnumSource.Mode.EXCLUDE,
			names = "CORRECTED")
	void transitionToResolved_ShouldThrowException_WhenAnomalyStateIsNotCorrected(AnomalyState state) {
		Anomaly anomaly = assertDoesNotThrow(()-> getValidAnomaly(state));
		EventTrace resolvedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		assertThrows(IllegalTransition.class, ()->anomaly.transitionToResolved(resolvedTrace));
	}
	
	@ParameterizedTest
	@EnumSource(
			value = AnomalyState.class,
			mode = EnumSource.Mode.EXCLUDE,
			names = "RESOLVED")
	void transitionToArchived_ShouldThrowException_WhenAnomalyStateIsNotResolved(AnomalyState state) {
		Anomaly anomaly = assertDoesNotThrow(()-> getValidAnomaly(state));
		EventTrace archivedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		assertThrows(IllegalTransition.class, ()->anomaly.transitionToArchived(archivedTrace));
	}
	
	@ParameterizedTest
	@EnumSource(
			value = AnomalyState.class,
			mode = EnumSource.Mode.EXCLUDE,
			names = "PENDING")
	void attachCorrectiveAction_ShouldThrowException_WhenAnomalyStateIsNotPending(AnomalyState state) {
		Anomaly anomaly = assertDoesNotThrow(()-> getValidAnomaly(state));
		assertThrows(IllegalAttachment.class, ()->anomaly.attachCorrectiveAction(VALID_DOC_ID));
	}
	
	@ParameterizedTest
	@EnumSource(
			value = AnomalyState.class,
			mode = EnumSource.Mode.EXCLUDE,
			names = "PENDING")
	void attachQualityDecision_ShouldThrowException_WhenAnomalyStateIsNotPending(AnomalyState state) {
		Anomaly anomaly = assertDoesNotThrow(()-> getValidAnomaly(state));
		assertThrows(IllegalAttachment.class, ()->anomaly.attachQualityDecision(QualityDecision.REPAIR));
	}
	
	@ParameterizedTest
	@EnumSource(
			value = AnomalyState.class,
			mode = EnumSource.Mode.EXCLUDE,
			names = "CORRECTED")
	void attachProvingDocument_ShouldThrowException_WhenAnomalyStateIsNotCorrected(AnomalyState state) {
		Anomaly anomaly = assertDoesNotThrow(()-> getValidAnomaly(state));
		assertThrows(IllegalAttachment.class, ()->anomaly.attachProvingDocument(VALID_DOC_ID));
	}
	
	@Test
	void attachQualityDecision_ShouldThrowException_WhenStateReturnToEmpty() {
		Anomaly anomaly = assertDoesNotThrow(()->getValidAnomaly(AnomalyState.PENDING));
		Anomaly anomalyStateNA = assertDoesNotThrow(()->anomaly.attachQualityDecision(QualityDecision.NA));
		assertThrows(IllegalAttachment.class, ()->anomalyStateNA.attachQualityDecision(QualityDecision.EMPTY));
	}
	
	@Test
	void attachProlongationId_ShouldThrowException_WhenAChildIdAlreadyExists() {
		Anomaly anomaly = assertDoesNotThrow(()->getValidAnomaly(AnomalyState.ARCHIVED));
		Anomaly anomalyWithChildId = assertDoesNotThrow(()->anomaly.attachProlongationId(UUID.randomUUID()));
		assertThrows(IllegalAttachment.class, ()->anomalyWithChildId.attachProlongationId(UUID.randomUUID()));
	}
	
	private Anomaly createPendingAnomaly() {
		EventTrace creatingTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		return new Anomaly(DESCRIPTION, creatingTrace);
	}
	
	private Anomaly createCorrectedAnomaly() throws IllegalAttachment, IllegalTransition, IllegalTraceErasureTentative, InconsistentAnomalyStateException {
		EventTrace correctedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		Anomaly anomaly = createPendingAnomaly().attachCorrectiveAction(VALID_DOC_ID);
		anomaly = anomaly.attachQualityDecision(QualityDecision.NA);
		return anomaly.transitionToCorrected(correctedTrace);
	}
	
	private Anomaly createResolvedAnomaly() throws IllegalAttachment, IllegalTransition, IllegalTraceErasureTentative, InconsistentAnomalyStateException {
		EventTrace resolvedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		Anomaly anomaly = createCorrectedAnomaly().attachProvingDocument(VALID_DOC_ID);
		return anomaly.transitionToResolved(resolvedTrace);
	}
	
	private Anomaly createArchivedAnomaly() throws IllegalTransition, IllegalTraceErasureTentative, IllegalAttachment, InconsistentAnomalyStateException {
		EventTrace archivedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		return createResolvedAnomaly().transitionToArchived(archivedTrace);
	}
	
	private Anomaly getValidAnomaly(AnomalyState state) throws IllegalTransition, IllegalTraceErasureTentative, IllegalAttachment, InconsistentAnomalyStateException {
		return switch(state) {
		case PENDING -> createPendingAnomaly();
		case CORRECTED -> createCorrectedAnomaly();
		case RESOLVED -> createResolvedAnomaly();
		case ARCHIVED -> createArchivedAnomaly();
		};
	}
}
