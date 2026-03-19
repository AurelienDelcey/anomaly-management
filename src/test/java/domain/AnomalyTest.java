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
import domain.valueobject.BusinessId;
import domain.valueobject.ProlongationContext;
import domain.valueobject.QualityDecision;
import domain.valueobject.Sector;

class AnomalyTest {
	
	private final static Instant FIXED_INSTANT = Instant.parse("2026-02-16T00:00:00Z");
	private final static String FIXED_UUID = "3f6b8a4c-9e21-4c7f-b8d2-1a5e0f6c2d9b";
	private final static String DESCRIPTION = "anomalyTest";
	private final static String OTHER_DESCRIPTION = "otherAnomalyTest";
	private final static String VALID_DOC_ID = "XXX-000-091991";
	private final static String VALID_ACTOR_ID = "0000";
	private final static int FIXED_YEAR = 2026;
	private final static int FIXED_SEQUENCE = 1;
	
	@Test
	void constructor_ShouldReturnValidAnomaly() {
		Anomaly anomaly = createPendingAnomaly();
		
		assertNotNull(anomaly.getId());
		assertNull(anomaly.getChildId());
		assertNull(anomaly.getProlongationContext());
		assertEquals(Sector.FORGING, anomaly.getSector());
		assertEquals(VALID_ACTOR_ID, anomaly.getTraceability().getCreation().actorId());
		assertEquals(FIXED_INSTANT, anomaly.getTraceability().getCreation().instant());
		assertEquals(AnomalyState.PENDING, anomaly.getAnomalyState());
		assertNull(anomaly.getCorrectiveAction());
		assertNull(anomaly.getEvidence());
		assertEquals(QualityDecision.EMPTY, anomaly.getQualityDecision());
		assertEquals(DESCRIPTION, anomaly.getDescription().description());
	}
	

	@Test
	void constructor_ShouldThrowException_WhenSectorIsNull() {
		EventTrace creationTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		assertThrows(IllegalArgumentException.class, ()-> new Anomaly(getValidBusinessId(), DESCRIPTION, null, creationTrace));
	}
	
	@Test
	void constructor_ShouldThrowException_WhenBusinessIdIsNull() {
		EventTrace creationTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		assertThrows(IllegalArgumentException.class, ()-> new Anomaly(null, DESCRIPTION, Sector.FORGING, creationTrace));
	}
	
	@Test
	void prolongationConstructor_ShouldReturnValidAnomaly() {
		ProlongationContext prolongationContext = getValidProlongationContext();
		EventTrace creationTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		Anomaly anomaly = new Anomaly(getValidBusinessId(), DESCRIPTION, Sector.FORGING, creationTrace, prolongationContext);
		
		assertNotNull(anomaly.getId());
		assertNull(anomaly.getChildId());
		assertNotNull(anomaly.getProlongationContext());
		assertEquals(FIXED_UUID, anomaly.getProlongationContext().parentId().toString());
		assertEquals(DESCRIPTION, anomaly.getProlongationContext().prolongationComment());
		assertEquals(Sector.FORGING, anomaly.getSector());
		assertEquals(VALID_ACTOR_ID, anomaly.getTraceability().getCreation().actorId());
		assertEquals(FIXED_INSTANT, anomaly.getTraceability().getCreation().instant());
		assertEquals(AnomalyState.PENDING, anomaly.getAnomalyState());
		assertNull(anomaly.getCorrectiveAction());
		assertNull(anomaly.getEvidence());
		assertEquals(QualityDecision.EMPTY, anomaly.getQualityDecision());
		assertEquals(DESCRIPTION, anomaly.getDescription().description());
	}
	
	@Test
	void prolongationConstructor_ShouldThrowException_WhenSectorIsNull() {
		EventTrace creationTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		assertThrows(IllegalArgumentException.class, ()-> new Anomaly(getValidBusinessId(), DESCRIPTION, null, creationTrace, getValidProlongationContext()));
	}
	
	@Test
	void prolongationConstructor_ShouldThrowException_WhenBusinessIdIsNull() {
		EventTrace creationTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		assertThrows(IllegalArgumentException.class, ()-> new Anomaly(null, DESCRIPTION, Sector.FORGING, creationTrace, getValidProlongationContext()));
	}
	
	@Test
	void prolongationConstructor_ShouldThrowException_WhenProlongationContextIsNull() {
		EventTrace creationTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		assertThrows(IllegalArgumentException.class, ()-> new Anomaly(getValidBusinessId(), DESCRIPTION, Sector.FORGING, creationTrace, null));
	}
	
	@Test
	void transitionToCorrected_ShouldReturnValidAnomaly(){
		Anomaly anomaly = createPendingAnomaly();
		EventTrace toCorrectedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		
		Anomaly anomalyWithCorrectiveAction = assertDoesNotThrow(()-> anomaly.attachCorrectiveAction(VALID_DOC_ID));
		Anomaly anomalyWithQualityDecision = assertDoesNotThrow(()-> anomalyWithCorrectiveAction.attachQualityDecision(QualityDecision.NA));
		Anomaly anomalyCorrected = assertDoesNotThrow(()-> anomalyWithQualityDecision.transitionToCorrected(toCorrectedTrace));
		
		
		assertNotNull(anomalyCorrected.getId());
		assertEquals(FIXED_YEAR, anomalyCorrected.getBusinessId().year());
		assertEquals(FIXED_SEQUENCE, anomalyCorrected.getBusinessId().sequence());
		assertNull(anomalyCorrected.getChildId());
		assertNull(anomalyCorrected.getProlongationContext());
		assertEquals(Sector.FORGING, anomalyCorrected.getSector());
		assertEquals(VALID_ACTOR_ID, anomalyCorrected.getTraceability().getCreation().actorId());
		assertEquals(FIXED_INSTANT, anomalyCorrected.getTraceability().getCreation().instant());
		assertEquals(VALID_ACTOR_ID, anomalyCorrected.getTraceability().getToCorrected().actorId());
		assertEquals(FIXED_INSTANT, anomalyCorrected.getTraceability().getToCorrected().instant());
		assertEquals(AnomalyState.CORRECTED, anomalyCorrected.getAnomalyState());
		assertEquals(VALID_DOC_ID, anomalyCorrected.getCorrectiveAction().documentId());
		assertNull(anomalyCorrected.getEvidence());
		assertEquals(QualityDecision.NA, anomalyCorrected.getQualityDecision());
		assertEquals(DESCRIPTION, anomalyCorrected.getDescription().description());
	}
	
	@Test
	void transitionToResolved_ShouldReturnValidAnomaly() {
		Anomaly anomalyCorrected = assertDoesNotThrow(()-> getValidAnomaly(AnomalyState.CORRECTED));
		EventTrace toResolvedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		
		Anomaly anomalyWithEvidences = assertDoesNotThrow(()-> anomalyCorrected.attachEvidence(VALID_DOC_ID));
		Anomaly anomalyResolved = assertDoesNotThrow(()-> anomalyWithEvidences.transitionToResolved(toResolvedTrace));
		
		
		assertNotNull(anomalyResolved.getId());
		assertEquals(FIXED_YEAR, anomalyResolved.getBusinessId().year());
		assertEquals(FIXED_SEQUENCE, anomalyResolved.getBusinessId().sequence());
		assertNull(anomalyResolved.getChildId());
		assertNull(anomalyResolved.getProlongationContext());
		assertEquals(Sector.FORGING, anomalyResolved.getSector());
		assertEquals(VALID_ACTOR_ID, anomalyResolved.getTraceability().getCreation().actorId());
		assertEquals(FIXED_INSTANT, anomalyResolved.getTraceability().getCreation().instant());
		assertEquals(VALID_ACTOR_ID, anomalyResolved.getTraceability().getToCorrected().actorId());
		assertEquals(FIXED_INSTANT, anomalyResolved.getTraceability().getToCorrected().instant());
		assertEquals(VALID_ACTOR_ID, anomalyResolved.getTraceability().getToResolved().actorId());
		assertEquals(FIXED_INSTANT, anomalyResolved.getTraceability().getToResolved().instant());
		assertEquals(AnomalyState.RESOLVED, anomalyResolved.getAnomalyState());
		assertEquals(VALID_DOC_ID, anomalyResolved.getCorrectiveAction().documentId());
		assertEquals(VALID_DOC_ID, anomalyResolved.getEvidence().documentId());
		assertEquals(QualityDecision.NA, anomalyResolved.getQualityDecision());
		assertEquals(DESCRIPTION, anomalyResolved.getDescription().description());
	}
	
	@Test
	void transitionToArchived_ShouldReturnValidAnomaly(){
		Anomaly anomalyResolved = assertDoesNotThrow(()->getValidAnomaly(AnomalyState.RESOLVED));
		EventTrace toArchivedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		Anomaly anomalyArchived = assertDoesNotThrow(()-> anomalyResolved.transitionToArchived(toArchivedTrace));
		
		
		assertNotNull(anomalyArchived.getId());
		assertEquals(FIXED_YEAR, anomalyArchived.getBusinessId().year());
		assertEquals(FIXED_SEQUENCE, anomalyArchived.getBusinessId().sequence());
		assertNull(anomalyArchived.getChildId());
		assertNull(anomalyArchived.getProlongationContext());
		assertEquals(Sector.FORGING, anomalyArchived.getSector());
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
		assertEquals(VALID_DOC_ID, anomalyArchived.getEvidence().documentId());
		assertEquals(QualityDecision.NA, anomalyArchived.getQualityDecision());
		assertEquals(DESCRIPTION, anomalyArchived.getDescription().description());
	}
	
	@Test
	void attachProlongationId_ShouldReturnValidAnomaly(){
		Anomaly anomaly = assertDoesNotThrow(()-> createArchivedAnomaly());
		Anomaly anomalyWithProlongationId = assertDoesNotThrow(()-> anomaly.linkProlongation(UUID.randomUUID()));
		
		assertNotNull(anomalyWithProlongationId.getId());
		assertEquals(FIXED_YEAR, anomalyWithProlongationId.getBusinessId().year());
		assertEquals(FIXED_SEQUENCE, anomalyWithProlongationId.getBusinessId().sequence());
		assertNotNull(anomalyWithProlongationId.getChildId());
		assertEquals(Sector.FORGING, anomalyWithProlongationId.getSector());
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
		assertEquals(VALID_DOC_ID, anomalyWithProlongationId.getEvidence().documentId());
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
	void attachDescription_ShouldReturnAnomalyWithCorrectDescription(){
		Anomaly anomaly = assertDoesNotThrow(()-> createPendingAnomaly());
		
		Anomaly anomalyWithNewDescription = assertDoesNotThrow(()->anomaly.attachDescription(OTHER_DESCRIPTION));
		assertEquals(OTHER_DESCRIPTION, anomalyWithNewDescription.getDescription().description());
	}
	
	@Test
	void attachSector_ShouldReturnAnomalyWithCorrectSector(){
		Anomaly anomaly = assertDoesNotThrow(()-> createPendingAnomaly());
		
		Anomaly anomalyWithSector = assertDoesNotThrow(()->anomaly.attachSector(Sector.FINISHING));
		assertEquals(Sector.FINISHING, anomalyWithSector.getSector());
	}
	
	@Test
	void attachSector_ShouldThrowException_WhenSectorIsNull() {
	    Anomaly anomaly = createPendingAnomaly();
	    assertThrows(IllegalArgumentException.class, () -> anomaly.attachSector(null));
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
	void attachEvidence_ShouldReturnAnomalyWithEvidence(){
		Anomaly anomaly = assertDoesNotThrow(()-> createCorrectedAnomaly());
		
		Anomaly anomalyWithEvidences = assertDoesNotThrow(()-> anomaly.attachEvidence(VALID_DOC_ID));
		assertEquals(VALID_DOC_ID, anomalyWithEvidences.getEvidence().documentId());
	}
	
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
	void transitionToResolved_ShouldThrowException_WhenEvidenceIsMissing() {
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
	void attachDescription_ShouldThrowException_WhenAnomalyStateIsNotPending(AnomalyState state) {
		Anomaly anomaly = assertDoesNotThrow(()-> getValidAnomaly(state));
		assertThrows(IllegalAttachment.class, ()->anomaly.attachDescription(OTHER_DESCRIPTION));
	}
	
	@ParameterizedTest
	@EnumSource(
			value = AnomalyState.class)
	void attachDescription_ShouldThrowException_WhenAnomalyIsProlongation(AnomalyState state) {
		Anomaly anomaly = assertDoesNotThrow(()-> getValidProlongationAtState(state));
		assertThrows(IllegalAttachment.class, ()->anomaly.attachDescription(OTHER_DESCRIPTION));
	}
	
	@ParameterizedTest
	@EnumSource(
			value = AnomalyState.class,
			mode = EnumSource.Mode.EXCLUDE,
			names = "PENDING")
	void attachSector_ShouldThrowException_WhenAnomalyStateIsNotPending(AnomalyState state) {
		Anomaly anomaly = assertDoesNotThrow(()-> getValidAnomaly(state));
		assertThrows(IllegalAttachment.class, ()->anomaly.attachSector(Sector.FINISHING));
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
	void attachEvidence_ShouldThrowException_WhenAnomalyStateIsNotCorrected(AnomalyState state) {
		Anomaly anomaly = assertDoesNotThrow(()-> getValidAnomaly(state));
		assertThrows(IllegalAttachment.class, ()->anomaly.attachEvidence(VALID_DOC_ID));
	}
	
	@Test
	void attachQualityDecision_ShouldThrowException_WhenStateReturnToEmpty() {
		Anomaly anomaly = assertDoesNotThrow(()->getValidAnomaly(AnomalyState.PENDING));
		Anomaly anomalyStateNA = assertDoesNotThrow(()->anomaly.attachQualityDecision(QualityDecision.NA));
		assertThrows(IllegalAttachment.class, ()->anomalyStateNA.attachQualityDecision(QualityDecision.EMPTY));
	}
	
	@Test
	void linkProlongation_ShouldThrowException_WhenAChildIdAlreadyExists() {
		Anomaly anomaly = assertDoesNotThrow(()->getValidAnomaly(AnomalyState.ARCHIVED));
		Anomaly anomalyWithChildId = assertDoesNotThrow(()->anomaly.linkProlongation(UUID.randomUUID()));
		assertThrows(IllegalAttachment.class, ()->anomalyWithChildId.linkProlongation(UUID.randomUUID()));
	}
	
	private Anomaly createProlongation() {
		EventTrace creationTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		return new Anomaly(getValidBusinessId(), DESCRIPTION, Sector.FORGING, creationTrace, getValidProlongationContext());
	}
	
	private Anomaly createPendingAnomaly() {
		EventTrace creationTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		return new Anomaly(getValidBusinessId(), DESCRIPTION, Sector.FORGING, creationTrace);
	}
	
	private Anomaly createCorrectedAnomaly() throws IllegalAttachment, IllegalTransition, IllegalTraceErasureTentative, InconsistentAnomalyStateException {
		EventTrace correctedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		Anomaly anomaly = createPendingAnomaly().attachCorrectiveAction(VALID_DOC_ID);
		anomaly = anomaly.attachQualityDecision(QualityDecision.NA);
		return anomaly.transitionToCorrected(correctedTrace);
	}
	
	private Anomaly createCorrectedProlongation() throws IllegalAttachment, IllegalTransition, IllegalTraceErasureTentative, InconsistentAnomalyStateException {
		EventTrace correctedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		Anomaly anomaly = createProlongation().attachCorrectiveAction(VALID_DOC_ID);
		anomaly = anomaly.attachQualityDecision(QualityDecision.NA);
		return anomaly.transitionToCorrected(correctedTrace);
	}
	
	private Anomaly createResolvedAnomaly() throws IllegalAttachment, IllegalTransition, IllegalTraceErasureTentative, InconsistentAnomalyStateException {
		EventTrace resolvedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		Anomaly anomaly = createCorrectedAnomaly().attachEvidence(VALID_DOC_ID);
		return anomaly.transitionToResolved(resolvedTrace);
	}
	
	private Anomaly createResolvedProlongation() throws IllegalAttachment, IllegalTransition, IllegalTraceErasureTentative, InconsistentAnomalyStateException {
		EventTrace resolvedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		Anomaly anomaly = createCorrectedProlongation().attachEvidence(VALID_DOC_ID);
		return anomaly.transitionToResolved(resolvedTrace);
	}
	
	private Anomaly createArchivedAnomaly() throws IllegalTransition, IllegalTraceErasureTentative, IllegalAttachment, InconsistentAnomalyStateException {
		EventTrace archivedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		return createResolvedAnomaly().transitionToArchived(archivedTrace);
	}
	
	private Anomaly createArchivedProlongation() throws IllegalTransition, IllegalTraceErasureTentative, IllegalAttachment, InconsistentAnomalyStateException {
		EventTrace archivedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		return createResolvedProlongation().transitionToArchived(archivedTrace);
	}
	
	private Anomaly getValidAnomaly(AnomalyState state) throws IllegalTransition, IllegalTraceErasureTentative, IllegalAttachment, InconsistentAnomalyStateException {
		return switch(state) {
		case PENDING -> createPendingAnomaly();
		case CORRECTED -> createCorrectedAnomaly();
		case RESOLVED -> createResolvedAnomaly();
		case ARCHIVED -> createArchivedAnomaly();
		};
	}
	
	private Anomaly getValidProlongationAtState(AnomalyState state) throws IllegalTransition, IllegalTraceErasureTentative, IllegalAttachment, InconsistentAnomalyStateException {
		return switch(state) {
		case PENDING -> createProlongation();
		case CORRECTED -> createCorrectedProlongation();
		case RESOLVED -> createResolvedProlongation();
		case ARCHIVED -> createArchivedProlongation();
		};
	}
	
	private ProlongationContext getValidProlongationContext() {
		return new ProlongationContext(UUID.fromString(FIXED_UUID), DESCRIPTION);
	}
	
	private BusinessId getValidBusinessId() {
		return new BusinessId(FIXED_YEAR, FIXED_SEQUENCE);
	}
}
