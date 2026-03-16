package domain.anomaly;

import java.util.UUID;

import domain.exception.IllegalAttachment;
import domain.exception.IllegalTraceErasureTentative;
import domain.exception.IllegalTransition;
import domain.exception.InconsistentAnomalyStateException;
import domain.traceability.EventTrace;
import domain.traceability.Traceability;
import domain.valueobject.BusinessId;
import domain.valueobject.CorrectiveAction;
import domain.valueobject.Description;
import domain.valueobject.Evidence;
import domain.valueobject.ProlongationContext;
import domain.valueobject.QualityDecision;
import domain.valueobject.Sector;

public class Anomaly {
	
	private final UUID id;
	private final BusinessId businessId;
	private final ProlongationContext prolongationContext;
	private final UUID childId;
	private final Sector sector;
	private final CorrectiveAction correctiveAction;
	private final Evidence evidence;
	private final Traceability traceability;
	private final QualityDecision qualityDecision;
	private final AnomalyState anomalyState;
	private final Description description;
	
	
	public Anomaly(BusinessId businessId, String description, Sector sector, EventTrace creationTrace){
		if(businessId == null) {
			throw new IllegalArgumentException("businessId cannot be null.");
		}
		if(sector == null) {
			throw new IllegalArgumentException("Sector cannot be null.");
		}
		this.id = UUID.randomUUID();
		this.businessId = businessId;
		this.childId = null;
		this.prolongationContext = null;
		this.sector = sector;
		this.traceability = new Traceability(creationTrace);
		this.anomalyState = AnomalyState.PENDING;
		this.correctiveAction = null;
		this.evidence = null;
		this.qualityDecision = QualityDecision.EMPTY;
		this.description = new Description(description);
	}
	
	public Anomaly(BusinessId businessId, String description, Sector sector, EventTrace creationTrace, ProlongationContext prolongationContext){
		if(businessId == null) {
			throw new IllegalArgumentException("businessId cannot be null.");
		}
		if(sector == null) {
			throw new IllegalArgumentException("Sector cannot be null.");
		}
		if(prolongationContext == null) {
			throw new IllegalArgumentException("prolongationContext cannot be null.");
		}
		this.id = UUID.randomUUID();
		this.businessId = businessId;
		this.childId = null;
		this.prolongationContext = prolongationContext;
		this.sector = sector;
		this.traceability = new Traceability(creationTrace);
		this.anomalyState = AnomalyState.PENDING;
		this.correctiveAction = null;
		this.evidence = null;
		this.qualityDecision = QualityDecision.EMPTY;
		this.description = new Description(description);
	}
	
	
	
	Anomaly(UUID id,BusinessId businessId, ProlongationContext prolongationContext, UUID childId, Sector sector, CorrectiveAction correctiveAction,
			Evidence evidence, Traceability traceability, QualityDecision qualityDecision,
			AnomalyState anomalyState, Description description) throws InconsistentAnomalyStateException {
		verifyStructuralConsistency(id, businessId, anomalyState, correctiveAction, evidence, qualityDecision, description, traceability, sector);
		this.id = id;
		this.businessId = businessId;
		this.prolongationContext = prolongationContext;
		this.childId = childId;
		this.sector = sector;
		this.correctiveAction = correctiveAction;
		this.evidence = evidence;
		this.traceability = traceability;
		this.qualityDecision = qualityDecision;
		this.anomalyState = anomalyState;
		this.description = description;
	}

	public Anomaly transitionToCorrected(EventTrace toCorrectedTrace) throws IllegalTransition,IllegalTraceErasureTentative, InconsistentAnomalyStateException{
		if(this.anomalyState != AnomalyState.PENDING) {
			throw new IllegalTransition("Anomaly must be in PENDING state.");
		}
		if(this.correctiveAction == null) {
			throw new IllegalTransition("A corrective action must be attached to this anomaly to validate the transition.");
		}
		if(this.qualityDecision == QualityDecision.EMPTY) {
			throw new IllegalTransition("A quality decision must be attached to this anomaly to validate the transition.");
		}
		Traceability trace = this.traceability.addToCorrectedTrace(toCorrectedTrace);
		
		return new Anomaly(id, businessId, prolongationContext, childId, sector, correctiveAction, evidence, trace, qualityDecision, AnomalyState.CORRECTED, description);
	}

	public Anomaly transitionToResolved(EventTrace toResolvedTrace) throws IllegalTransition,IllegalTraceErasureTentative, InconsistentAnomalyStateException{
		if(this.anomalyState != AnomalyState.CORRECTED) {
			throw new IllegalTransition("Anomaly must be in CORRECTED state.");
		}
		if(this.evidence == null) {
			throw new IllegalTransition("Evidence must be attached to validate the transition.");
		}
		Traceability trace = this.traceability.addToResolvedTrace(toResolvedTrace);
		
		return new Anomaly(id, businessId, prolongationContext, childId, sector, correctiveAction, evidence, trace, qualityDecision, AnomalyState.RESOLVED, description);
	}
	
	public Anomaly transitionToArchived(EventTrace toArchivedTrace)throws IllegalTransition,IllegalTraceErasureTentative, InconsistentAnomalyStateException{
		if(this.anomalyState != AnomalyState.RESOLVED) {
			throw new IllegalTransition("Anomaly must be in RESOLVED state.");
		}
		Traceability trace = this.traceability.addToArchivedTrace(toArchivedTrace);
		
		return new Anomaly(id, businessId, prolongationContext, childId, sector, correctiveAction, evidence, trace, qualityDecision, AnomalyState.ARCHIVED, description);
	}
	
	public Anomaly attachDescription(String description) throws IllegalAttachment, InconsistentAnomalyStateException{
		if(this.anomalyState != AnomalyState.PENDING) {
			throw new IllegalAttachment("Editing the description is only permitted during the PENDING state.");
		}
		Description newDescription = new Description(description);
		return new Anomaly(id, businessId, prolongationContext, childId, sector, correctiveAction, evidence, traceability, qualityDecision, anomalyState, newDescription);
	}
	
	public Anomaly attachSector(Sector sector) throws IllegalAttachment, InconsistentAnomalyStateException{
		if(sector == null) {
			throw new IllegalArgumentException("Sector cannot be null.");
		}
		if(this.anomalyState != AnomalyState.PENDING) {
			throw new IllegalAttachment("Editing sector is only permitted during the PENDING state.");
		}
		
		return new Anomaly(id, businessId, prolongationContext, childId, sector, correctiveAction, evidence, traceability, qualityDecision, anomalyState, description);
	}

	public Anomaly attachCorrectiveAction (String correctiveActionId) throws IllegalAttachment, InconsistentAnomalyStateException{
		if(this.anomalyState != AnomalyState.PENDING) {
			throw new IllegalAttachment("The state of anomaly must be PENDING to attach corrective action.");
		}
		
		CorrectiveAction action = new CorrectiveAction(correctiveActionId);
		
		return new Anomaly(id, businessId, prolongationContext, childId, sector, action, evidence, traceability, qualityDecision, anomalyState, description);
	}
	
	public Anomaly attachQualityDecision(QualityDecision newQualityDecision)throws IllegalAttachment, InconsistentAnomalyStateException{
		if(this.anomalyState != AnomalyState.PENDING) {
			throw new IllegalAttachment("The state of anomaly must be PENDING to attach quality decision.");
		}
		if(newQualityDecision==QualityDecision.EMPTY) {
			throw new IllegalAttachment("Quality decision can't be EMPTY.");
		}
		return new Anomaly(id, businessId, prolongationContext, childId, sector, correctiveAction, evidence, traceability, newQualityDecision, anomalyState, description);
	}
	
	public Anomaly attachEvidence(String evidenceId)throws IllegalAttachment, InconsistentAnomalyStateException{
		if(this.anomalyState != AnomalyState.CORRECTED) {
			throw new IllegalAttachment("Evidence can only be attached in CORRECTED state.");
		}
		
		Evidence document = new Evidence(evidenceId);
		
		return new Anomaly(id, businessId, prolongationContext, childId, sector, correctiveAction, document, traceability, qualityDecision, anomalyState, description);
	}
	
	public Anomaly linkProlongation(UUID prolongationId)throws IllegalAttachment, InconsistentAnomalyStateException{
		if(this.anomalyState != AnomalyState.ARCHIVED) {
			throw new IllegalAttachment("The state of anomaly must be ARCHIVED to attach a prolongation ID.");
		}
		if(this.childId != null) {
			throw new IllegalAttachment("A prolongation ID is already attached to this anomaly.");
		}
		return new Anomaly(id, businessId, prolongationContext, prolongationId, sector, correctiveAction, evidence, traceability, qualityDecision, anomalyState, description);
	}
	
	public UUID getId() {
		return this.id;
	}
	
	public ProlongationContext getProlongationContext() {
		return prolongationContext;
	}

	public UUID getChildId() {
		return childId;
	}
	
	public Sector getSector() {
		return sector;
	}

	public CorrectiveAction getCorrectiveAction() {
		return correctiveAction;
	}

	public Evidence getEvidence() {
		return evidence;
	}

	public Traceability getTraceability() {
		return traceability;
	}

	public QualityDecision getQualityDecision() {
		return qualityDecision;
	}

	public AnomalyState getAnomalyState() {
		return anomalyState;
	}

	public Description getDescription() {
		return description;
	}

	private void verifyStructuralConsistency(UUID id, BusinessId businessId, AnomalyState state, CorrectiveAction correctiveAction,
			Evidence evidence, QualityDecision qualityDecision, Description description, 
			Traceability traceability, Sector sector) throws InconsistentAnomalyStateException {
		if(state == null) {
			throw new InconsistentAnomalyStateException("Cannot create anomaly without state.");
		}
		if(traceability == null) {
			throw new InconsistentAnomalyStateException("Cannot create anomaly without traceability.");
		}
		if(businessId == null) {
			throw new InconsistentAnomalyStateException("Cannot create anomaly without businessId.");
		}
		switch(state) {
			case PENDING -> verifyPendingStructure(id, evidence, description, traceability, sector);
			case CORRECTED -> verifyCorrectedStructure(id, correctiveAction, qualityDecision, description, traceability, sector);
			case RESOLVED -> verifyResolvedStructure(id, correctiveAction, evidence, qualityDecision, description, traceability, sector);
			case ARCHIVED -> verifyArchivedStructure(id, correctiveAction, evidence, qualityDecision, description, traceability, sector);
		}
	}

	private void verifyArchivedStructure(UUID id, CorrectiveAction correctiveAction, Evidence evidence,
			QualityDecision qualityDecision, Description description, Traceability traceability, Sector sector)
			throws InconsistentAnomalyStateException {
		if(description == null || sector == null|| id == null || qualityDecision == null ||qualityDecision == QualityDecision.EMPTY || 
				correctiveAction == null || evidence == null || traceability.getCreation() == null || 
				traceability.getToCorrected() == null || traceability.getToResolved() == null ||
				traceability.getToArchived() == null) {
			throw new InconsistentAnomalyStateException("Cannot create anomaly in ARCHIVED state without description, ID, corrective action, quality decision, or evidence.");
		}
	}

	private void verifyResolvedStructure(UUID id, CorrectiveAction correctiveAction, Evidence evidence,
			QualityDecision qualityDecision, Description description, Traceability traceability, Sector sector)
			throws InconsistentAnomalyStateException {
		if(description == null || sector == null|| id == null || qualityDecision == null || qualityDecision == QualityDecision.EMPTY ||
				correctiveAction == null || evidence == null || traceability.getCreation() == null || 
				traceability.getToCorrected() == null || traceability.getToResolved() == null ||
				traceability.getToArchived() != null) {
			throw new InconsistentAnomalyStateException("Cannot create anomaly in RESOLVED state without description, ID, corrective action, quality decision, or evidence.");
		}
	}

	private void verifyCorrectedStructure(UUID id, CorrectiveAction correctiveAction, QualityDecision qualityDecision,
			Description description, Traceability traceability, Sector sector) throws InconsistentAnomalyStateException {
		if(description == null || sector == null|| id == null || qualityDecision == null || qualityDecision == QualityDecision.EMPTY ||
				correctiveAction == null || traceability.getCreation() == null || 
				traceability.getToCorrected() == null || traceability.getToResolved() != null ||
				traceability.getToArchived() != null) {
			throw new InconsistentAnomalyStateException("Cannot create anomaly in CORRECTED state without description, ID, corrective action, or quality decision.");
		}
	}

	private void verifyPendingStructure(UUID id, Evidence evidence, Description description, Traceability traceability, Sector sector)
			throws InconsistentAnomalyStateException {
		if(description == null || sector == null || id == null || traceability.getCreation() == null || 
				traceability.getToCorrected() != null || traceability.getToResolved() != null ||
				traceability.getToArchived() != null || evidence != null ) {
			throw new InconsistentAnomalyStateException("Cannot create anomaly in PENDING state without description, or ID.");
		}
	}
}
