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
import domain.exception.InvalidValueException;
import domain.traceability.EventTrace;
import domain.valueobject.BusinessId;
import domain.valueobject.Description;
import domain.valueobject.ImpactedQuantity;
import domain.valueobject.Machine;
import domain.valueobject.ProductionOrder;
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
	private final static String VALID_ACTOR_NAME = "Dupont";
	private final static int FIXED_YEAR = 2026;
	private final static int FIXED_SEQUENCE = 1;
	private final static int QUANTITY = 50;
	private final static int OTHER_QUANTITY = 100;
	private final static int ORDER = 99999;
	private final static int OTHER_ORDER = 11111;
	
	@Test
	void constructor_ShouldReturnValidAnomaly() {
		Anomaly anomaly = createPendingAnomaly();
		
		assertNotNull(anomaly.getId());
		assertNull(anomaly.getChildId());
		assertNull(anomaly.getProlongationContext());
		assertEquals(Machine.MACHINE_1, anomaly.getMachine());
		assertEquals(ORDER, anomaly.getProductionOrder().productionOrder());
		assertEquals(QUANTITY, anomaly.getQuantity().quantity());
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
		EventTrace creationTrace = getEventTrace();
		assertThrows(InvalidValueException.class, ()-> new Anomaly(getValidBusinessId(), getValidDescription(), null, 
				getValidQuantity(), getValideProductionOrder(), Machine.MACHINE_1, creationTrace));
	}
	
	@Test
	void constructor_ShouldThrowException_WhenBusinessIdIsNull() {
		EventTrace creationTrace = getEventTrace();
		assertThrows(InvalidValueException.class, ()-> new Anomaly(null, getValidDescription(), Sector.FORGING, 
				getValidQuantity(), getValideProductionOrder(), Machine.MACHINE_1, creationTrace));
	}
	
	@Test
	void constructor_ShouldThrowException_WhenImpactedQuantityIsNull() {
		EventTrace creationTrace = getEventTrace();
		assertThrows(InvalidValueException.class, ()-> new Anomaly(getValidBusinessId(), getValidDescription(), Sector.FORGING, 
				null, getValideProductionOrder(), Machine.MACHINE_1, creationTrace));
	}
	
	@Test
	void constructor_ShouldThrowException_WhenProductionOrderIsNull() {
		EventTrace creationTrace = getEventTrace();
		assertThrows(InvalidValueException.class, ()-> new Anomaly(getValidBusinessId(), getValidDescription(), Sector.FORGING, 
				getValidQuantity(), null, Machine.MACHINE_1, creationTrace));
	}
	
	@Test
	void constructor_ShouldThrowException_WhenMachineIsNull() {
		EventTrace creationTrace = getEventTrace();
		assertThrows(InvalidValueException.class, ()-> new Anomaly(getValidBusinessId(), getValidDescription(), Sector.FORGING, 
				getValidQuantity(), getValideProductionOrder(), null, creationTrace));
	}
	
	@Test
	void prolongationConstructor_ShouldReturnValidAnomaly() {
		ProlongationContext prolongationContext = getValidProlongationContext();
		EventTrace creationTrace = getEventTrace();
		Anomaly anomaly = new Anomaly(QualityDecision.NA, getValidBusinessId(),getValidDescription(), Sector.FORGING, 
				getValidQuantity(), getValideProductionOrder(), Machine.MACHINE_1, creationTrace, prolongationContext);
		
		assertNotNull(anomaly.getId());
		assertNull(anomaly.getChildId());
		assertNotNull(anomaly.getProlongationContext());
		assertEquals(Machine.MACHINE_1, anomaly.getMachine());
		assertEquals(ORDER, anomaly.getProductionOrder().productionOrder());
		assertEquals(QUANTITY, anomaly.getQuantity().quantity());
		assertEquals(FIXED_UUID, anomaly.getProlongationContext().parentId().toString());
		assertEquals(DESCRIPTION, anomaly.getProlongationContext().prolongationComment());
		assertEquals(Sector.FORGING, anomaly.getSector());
		assertEquals(VALID_ACTOR_ID, anomaly.getTraceability().getCreation().actorId());
		assertEquals(FIXED_INSTANT, anomaly.getTraceability().getCreation().instant());
		assertEquals(AnomalyState.PENDING, anomaly.getAnomalyState());
		assertNull(anomaly.getCorrectiveAction());
		assertNull(anomaly.getEvidence());
		assertEquals(QualityDecision.NA, anomaly.getQualityDecision());
		assertEquals(DESCRIPTION, anomaly.getDescription().description());
	}
	
	@Test
	void prolongationConstructor_ShouldThrowException_WhenSectorIsNull() {
		EventTrace creationTrace = getEventTrace();
		assertThrows(InvalidValueException.class, ()-> new Anomaly(QualityDecision.NA, getValidBusinessId(), getValidDescription(), null, 
				getValidQuantity(), getValideProductionOrder(), Machine.MACHINE_1, creationTrace, getValidProlongationContext()));
	}
	
	@Test
	void prolongationConstructor_ShouldThrowException_WhenBusinessIdIsNull() {
		EventTrace creationTrace = getEventTrace();
		assertThrows(InvalidValueException.class, ()-> new Anomaly(QualityDecision.NA, null, getValidDescription(), Sector.FORGING, 
				getValidQuantity(), getValideProductionOrder(), Machine.MACHINE_1, creationTrace, getValidProlongationContext()));
	}
	
	@Test
	void prolongationConstructor_ShouldThrowException_WhenImpactedQuantityIsNull() {
		EventTrace creationTrace = getEventTrace();
		assertThrows(InvalidValueException.class, ()-> new Anomaly(QualityDecision.NA, getValidBusinessId(), getValidDescription(), Sector.FORGING, 
				null, getValideProductionOrder(), Machine.MACHINE_1, creationTrace, getValidProlongationContext()));
	}
	
	@Test
	void prolongationConstructor_ShouldThrowException_WhenProductionOrderIsNull() {
		EventTrace creationTrace = getEventTrace();
		assertThrows(InvalidValueException.class, ()-> new Anomaly(QualityDecision.NA, getValidBusinessId(), getValidDescription(), Sector.FORGING, 
				getValidQuantity(), null, Machine.MACHINE_1, creationTrace, getValidProlongationContext()));
	}
	
	@Test
	void prolongationConstructor_ShouldThrowException_WhenMachineIsNull() {
		EventTrace creationTrace = getEventTrace();
		assertThrows(InvalidValueException.class, ()-> new Anomaly(QualityDecision.NA, getValidBusinessId(), getValidDescription(), Sector.FORGING, 
				getValidQuantity(), getValideProductionOrder(), null, creationTrace, getValidProlongationContext()));
	}
	
	@Test
	void prolongationConstructor_ShouldThrowException_WhenProlongationContextIsNull() {
		EventTrace creationTrace = getEventTrace();
		assertThrows(InvalidValueException.class, ()-> new Anomaly(QualityDecision.NA, getValidBusinessId(), getValidDescription(), Sector.FORGING, 
				getValidQuantity(), getValideProductionOrder(), Machine.MACHINE_1, creationTrace, null));
	}
	
	@Test
	void transitionToCorrected_ShouldReturnValidAnomaly(){
		Anomaly anomaly = createPendingAnomaly();
		EventTrace toCorrectedTrace = getEventTrace();
		
		Anomaly anomalyWithCorrectiveAction = assertDoesNotThrow(()-> anomaly.attachCorrectiveAction(VALID_DOC_ID));
		Anomaly anomalyWithQualityDecision = assertDoesNotThrow(()-> anomalyWithCorrectiveAction.attachQualityDecision(QualityDecision.NA));
		Anomaly anomalyCorrected = assertDoesNotThrow(()-> anomalyWithQualityDecision.transitionToCorrected(toCorrectedTrace));
		
		
		assertNotNull(anomalyCorrected.getId());
		assertEquals(FIXED_YEAR, anomalyCorrected.getBusinessId().year());
		assertEquals(FIXED_SEQUENCE, anomalyCorrected.getBusinessId().sequence());
		assertEquals(Machine.MACHINE_1, anomaly.getMachine());
		assertEquals(ORDER, anomaly.getProductionOrder().productionOrder());
		assertEquals(QUANTITY, anomaly.getQuantity().quantity());
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
		EventTrace toResolvedTrace = getEventTrace();
		
		Anomaly anomalyWithEvidences = assertDoesNotThrow(()-> anomalyCorrected.attachEvidence(VALID_DOC_ID));
		Anomaly anomalyResolved = assertDoesNotThrow(()-> anomalyWithEvidences.transitionToResolved(toResolvedTrace));
		
		
		assertNotNull(anomalyResolved.getId());
		assertEquals(FIXED_YEAR, anomalyResolved.getBusinessId().year());
		assertEquals(FIXED_SEQUENCE, anomalyResolved.getBusinessId().sequence());
		assertEquals(Machine.MACHINE_1, anomalyResolved.getMachine());
		assertEquals(ORDER, anomalyResolved.getProductionOrder().productionOrder());
		assertEquals(QUANTITY, anomalyResolved.getQuantity().quantity());
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
		EventTrace toArchivedTrace = getEventTrace();
		Anomaly anomalyArchived = assertDoesNotThrow(()-> anomalyResolved.transitionToArchived(toArchivedTrace));
		
		
		assertNotNull(anomalyArchived.getId());
		assertEquals(FIXED_YEAR, anomalyArchived.getBusinessId().year());
		assertEquals(FIXED_SEQUENCE, anomalyArchived.getBusinessId().sequence());
		assertEquals(Machine.MACHINE_1, anomalyArchived.getMachine());
		assertEquals(ORDER, anomalyArchived.getProductionOrder().productionOrder());
		assertEquals(QUANTITY, anomalyArchived.getQuantity().quantity());
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
		assertEquals(Machine.MACHINE_1, anomaly.getMachine());
		assertEquals(ORDER, anomaly.getProductionOrder().productionOrder());
		assertEquals(QUANTITY, anomaly.getQuantity().quantity());
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
		Description otherDescription = new Description(OTHER_DESCRIPTION);
		Anomaly anomalyWithNewDescription = assertDoesNotThrow(()->anomaly.attachDescription(otherDescription));
		assertEquals(OTHER_DESCRIPTION, anomalyWithNewDescription.getDescription().description());
	}
	
	@Test
	void attachImpactedQuantity_ShouldReturnAnomalyWithCorrectDescription(){
		Anomaly anomaly = assertDoesNotThrow(()-> createPendingAnomaly());
		ImpactedQuantity newQuantity = new ImpactedQuantity(OTHER_QUANTITY);
		Anomaly anomalyWithNewQuantity = assertDoesNotThrow(()->anomaly.attachImpactedQuantity(newQuantity));
		assertEquals(OTHER_QUANTITY, anomalyWithNewQuantity.getQuantity().quantity());
	}
	
	@Test
	void attachProductionOrder_ShouldReturnAnomalyWithCorrectDescription(){
		Anomaly anomaly = assertDoesNotThrow(()-> createPendingAnomaly());
		ProductionOrder newProductionOrder = new ProductionOrder(OTHER_ORDER);
		Anomaly anomalyWithNewProductionOrder = assertDoesNotThrow(()->anomaly.attachProductionOrder(newProductionOrder));
		assertEquals(OTHER_ORDER, anomalyWithNewProductionOrder.getProductionOrder().productionOrder());
	}
	
	@Test
	void attachMachine_ShouldReturnAnomalyWithCorrectDescription(){
		Anomaly anomaly = assertDoesNotThrow(()-> createPendingAnomaly());
		Anomaly anomalyWithNewMachine = assertDoesNotThrow(()->anomaly.attachMachine(Machine.MACHINE_2));
		assertEquals(Machine.MACHINE_2, anomalyWithNewMachine.getMachine());
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
	    assertThrows(InvalidValueException.class, () -> anomaly.attachSector(null));
	}
	
	@Test
	void attachDescription_ShouldThrowException_WhenDescriptionIsNull() {
	    Anomaly anomaly = createPendingAnomaly();
	    assertThrows(InvalidValueException.class, () -> anomaly.attachDescription(null));
	}
	
	@Test
	void attachImpactedQuantity_ShouldThrowException_WhenImpactedQuantityIsNull() {
	    Anomaly anomaly = createPendingAnomaly();
	    assertThrows(InvalidValueException.class, () -> anomaly.attachImpactedQuantity(null));
	}
	
	@Test
	void attachProductionOrder_ShouldThrowException_WhenProductionOrderIsNull() {
	    Anomaly anomaly = createPendingAnomaly();
	    assertThrows(InvalidValueException.class, () -> anomaly.attachProductionOrder(null));
	}
	
	@Test
	void attachMachine_ShouldThrowException_WhenMachineIsNull() {
	    Anomaly anomaly = createPendingAnomaly();
	    assertThrows(InvalidValueException.class, () -> anomaly.attachMachine(null));
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
		EventTrace correctedTrace = getEventTrace();
		
		assertThrows(IllegalTransition.class, ()->anomalyWithQualityDecision.transitionToCorrected(correctedTrace));
	}
	
	@Test
	void transitionToCorrected_ShouldThrowException_WhenQualityDecisionIsMissing() {
		Anomaly anomaly = createPendingAnomaly();
		Anomaly anomalyWithCorrectiveAction = assertDoesNotThrow(()->anomaly.attachCorrectiveAction(VALID_DOC_ID));
		EventTrace correctedTrace = getEventTrace();
		
		assertThrows(IllegalTransition.class, ()->anomalyWithCorrectiveAction.transitionToCorrected(correctedTrace));
	}
	
	@Test
	void transitionToResolved_ShouldThrowException_WhenEvidenceIsMissing() {
		Anomaly anomaly = assertDoesNotThrow(()->createCorrectedAnomaly());
		EventTrace resolvedTrace = getEventTrace();
		
		assertThrows(IllegalTransition.class, ()->anomaly.transitionToResolved(resolvedTrace));
	}
	
	@ParameterizedTest
	@EnumSource(
			value = AnomalyState.class,
			mode = EnumSource.Mode.EXCLUDE,
			names = "PENDING")
	void transitionToCorrected_ShouldThrowException_WhenAnomalyStateIsNotPending(AnomalyState state) {
		Anomaly anomaly = assertDoesNotThrow(()-> getValidAnomaly(state));
		EventTrace correctedTrace = getEventTrace();
		assertThrows(IllegalTransition.class, ()->anomaly.transitionToCorrected(correctedTrace));
	}
	
	@ParameterizedTest
	@EnumSource(
			value = AnomalyState.class,
			mode = EnumSource.Mode.EXCLUDE,
			names = "CORRECTED")
	void transitionToResolved_ShouldThrowException_WhenAnomalyStateIsNotCorrected(AnomalyState state) {
		Anomaly anomaly = assertDoesNotThrow(()-> getValidAnomaly(state));
		EventTrace resolvedTrace = getEventTrace();
		assertThrows(IllegalTransition.class, ()->anomaly.transitionToResolved(resolvedTrace));
	}
	
	@ParameterizedTest
	@EnumSource(
			value = AnomalyState.class,
			mode = EnumSource.Mode.EXCLUDE,
			names = "RESOLVED")
	void transitionToArchived_ShouldThrowException_WhenAnomalyStateIsNotResolved(AnomalyState state) {
		Anomaly anomaly = assertDoesNotThrow(()-> getValidAnomaly(state));
		EventTrace archivedTrace = getEventTrace();
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
		Description otherDescription = new Description(OTHER_DESCRIPTION);
		assertThrows(IllegalAttachment.class, ()->anomaly.attachDescription(otherDescription));
	}
	
	@ParameterizedTest
	@EnumSource(
			value = AnomalyState.class)
	void attachDescription_ShouldThrowException_WhenAnomalyIsProlongation(AnomalyState state) {
		Anomaly anomaly = assertDoesNotThrow(()-> getValidProlongationAtState(state));
		Description otherDescription = new Description(OTHER_DESCRIPTION);
		assertThrows(IllegalAttachment.class, ()->anomaly.attachDescription(otherDescription));
	}
	
	@ParameterizedTest
	@EnumSource(
			value = AnomalyState.class,
			mode = EnumSource.Mode.EXCLUDE,
			names = "PENDING")
	void attachImpactedQuantity_ShouldThrowException_WhenAnomalyStateIsNotPending(AnomalyState state) {
		Anomaly anomaly = assertDoesNotThrow(()-> getValidAnomaly(state));
		ImpactedQuantity newQuantity = new ImpactedQuantity(OTHER_QUANTITY);
		assertThrows(IllegalAttachment.class, ()->anomaly.attachImpactedQuantity(newQuantity));
	}
	
	@ParameterizedTest
	@EnumSource(
			value = AnomalyState.class)
	void attachImpactedQuantity_ShouldThrowException_WhenAnomalyIsProlongation(AnomalyState state) {
		Anomaly anomaly = assertDoesNotThrow(()-> getValidProlongationAtState(state));
		ImpactedQuantity newQuantity = new ImpactedQuantity(OTHER_QUANTITY);
		assertThrows(IllegalAttachment.class, ()->anomaly.attachImpactedQuantity(newQuantity));
	}
	
	@ParameterizedTest
	@EnumSource(
			value = AnomalyState.class,
			mode = EnumSource.Mode.EXCLUDE,
			names = "PENDING")
	void attachProductionOrder_ShouldThrowException_WhenAnomalyStateIsNotPending(AnomalyState state) {
		Anomaly anomaly = assertDoesNotThrow(()-> getValidAnomaly(state));
		ProductionOrder newProductionOrder = new ProductionOrder(OTHER_ORDER);
		assertThrows(IllegalAttachment.class, ()->anomaly.attachProductionOrder(newProductionOrder));
	}
	
	@ParameterizedTest
	@EnumSource(
			value = AnomalyState.class)
	void attachProductionOrder_ShouldThrowException_WhenAnomalyIsProlongation(AnomalyState state) {
		Anomaly anomaly = assertDoesNotThrow(()-> getValidProlongationAtState(state));
		ProductionOrder newProductionOrder = new ProductionOrder(OTHER_ORDER);
		assertThrows(IllegalAttachment.class, ()->anomaly.attachProductionOrder(newProductionOrder));
	}
	
	@ParameterizedTest
	@EnumSource(
			value = AnomalyState.class,
			mode = EnumSource.Mode.EXCLUDE,
			names = "PENDING")
	void attachMachine_ShouldThrowException_WhenAnomalyStateIsNotPending(AnomalyState state) {
		Anomaly anomaly = assertDoesNotThrow(()-> getValidAnomaly(state));
		assertThrows(IllegalAttachment.class, ()->anomaly.attachMachine(Machine.OTHER_MACHINE));
	}
	
	@ParameterizedTest
	@EnumSource(
			value = AnomalyState.class)
	void attachMachine_ShouldThrowException_WhenAnomalyIsProlongation(AnomalyState state) {
		Anomaly anomaly = assertDoesNotThrow(()-> getValidProlongationAtState(state));
		assertThrows(IllegalAttachment.class, ()->anomaly.attachMachine(Machine.OTHER_MACHINE));
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
			value = AnomalyState.class)
	void attachSector_ShouldThrowException_WhenAnomalyIsProlongation(AnomalyState state) {
		Anomaly anomaly = assertDoesNotThrow(()-> getValidProlongationAtState(state));
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
		EventTrace creationTrace = getEventTrace();
		return new Anomaly(QualityDecision.NA, getValidBusinessId(), getValidDescription(), Sector.FORGING, getValidQuantity(), getValideProductionOrder(), Machine.MACHINE_1, creationTrace, getValidProlongationContext());
	}
	
	private Anomaly createPendingAnomaly() {
		EventTrace creationTrace = getEventTrace();
		return new Anomaly(getValidBusinessId(), getValidDescription(), Sector.FORGING, getValidQuantity(), getValideProductionOrder(), Machine.MACHINE_1, creationTrace);
	}
	
	private Anomaly createCorrectedAnomaly() throws IllegalAttachment, IllegalTransition, IllegalTraceErasureTentative, InconsistentAnomalyStateException {
		EventTrace correctedTrace = getEventTrace();
		Anomaly anomaly = createPendingAnomaly().attachCorrectiveAction(VALID_DOC_ID);
		anomaly = anomaly.attachQualityDecision(QualityDecision.NA);
		return anomaly.transitionToCorrected(correctedTrace);
	}
	
	private Anomaly createCorrectedProlongation() throws IllegalAttachment, IllegalTransition, IllegalTraceErasureTentative, InconsistentAnomalyStateException {
		EventTrace correctedTrace = getEventTrace();
		Anomaly anomaly = createProlongation().attachCorrectiveAction(VALID_DOC_ID);
		return anomaly.transitionToCorrected(correctedTrace);
	}
	
	private Anomaly createResolvedAnomaly() throws IllegalAttachment, IllegalTransition, IllegalTraceErasureTentative, InconsistentAnomalyStateException {
		EventTrace resolvedTrace = getEventTrace();
		Anomaly anomaly = createCorrectedAnomaly().attachEvidence(VALID_DOC_ID);
		return anomaly.transitionToResolved(resolvedTrace);
	}
	
	private Anomaly createResolvedProlongation() throws IllegalAttachment, IllegalTransition, IllegalTraceErasureTentative, InconsistentAnomalyStateException {
		EventTrace resolvedTrace = getEventTrace();
		Anomaly anomaly = createCorrectedProlongation().attachEvidence(VALID_DOC_ID);
		return anomaly.transitionToResolved(resolvedTrace);
	}
	
	private Anomaly createArchivedAnomaly() throws IllegalTransition, IllegalTraceErasureTentative, IllegalAttachment, InconsistentAnomalyStateException {
		EventTrace archivedTrace = getEventTrace();
		return createResolvedAnomaly().transitionToArchived(archivedTrace);
	}
	
	private Anomaly createArchivedProlongation() throws IllegalTransition, IllegalTraceErasureTentative, IllegalAttachment, InconsistentAnomalyStateException {
		EventTrace archivedTrace = getEventTrace();
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
	
	private Description getValidDescription() {
		return new Description(DESCRIPTION);
	}
	
	private ImpactedQuantity getValidQuantity() {
		return new ImpactedQuantity(QUANTITY);
	}
	
	private ProductionOrder getValideProductionOrder() {
		return new ProductionOrder(ORDER);
	}
	
	private EventTrace getEventTrace() {
		return new EventTrace(VALID_ACTOR_ID, VALID_ACTOR_NAME, FIXED_INSTANT);
	}
}
