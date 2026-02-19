package domain.anomaly;

import java.util.UUID;

import domain.exception.IllegalAttachment;
import domain.exception.IllegalTraceErasureTentative;
import domain.exception.IllegalTransition;
import domain.exception.InconsistentAnomalyStateException;
import domain.traceability.EventTrace;
import domain.traceability.Traceability;
import domain.valueobject.CorrectiveAction;
import domain.valueobject.Description;
import domain.valueobject.ProvingDocument;
import domain.valueobject.QualityDecision;

public class Anomaly {
	
	private final UUID id;
	private final UUID parentId;
	private final UUID childId;
	private final CorrectiveAction correctiveAction;
	private final ProvingDocument provingDocument;
	private final Traceability traceability;
	private final QualityDecision qualityDecision;
	private final AnomalyState anomalyState;
	private final Description description;
	
	
	public Anomaly(String description, EventTrace creatingTrace){
		this.id = UUID.randomUUID();
		this.childId = null;
		this.parentId = null;
		this.traceability = new Traceability(creatingTrace);
		this.anomalyState = AnomalyState.PENDING;
		this.correctiveAction = null;
		this.provingDocument = null;
		this.qualityDecision = QualityDecision.EMPTY;
		this.description = new Description(description);
	}
	
	public Anomaly(String description, EventTrace creatingTrace, UUID parentId){
		this.id = UUID.randomUUID();
		this.childId = null;
		this.parentId = parentId;
		this.traceability = new Traceability(creatingTrace);
		this.anomalyState = AnomalyState.PENDING;
		this.correctiveAction = null;
		this.provingDocument = null;
		this.qualityDecision = QualityDecision.EMPTY;
		this.description = new Description(description);
	}
	
	
	
	Anomaly(UUID id, UUID parentId, UUID childId, CorrectiveAction correctiveAction,
			ProvingDocument provingDocument, Traceability traceability, QualityDecision qualityDecision,
			AnomalyState anomalyState, Description description) {
		verifyStructuralConsistency(id, anomalyState, correctiveAction, provingDocument, qualityDecision, description);
		this.id = id;
		this.parentId = parentId;
		this.childId = childId;
		this.correctiveAction = correctiveAction;
		this.provingDocument = provingDocument;
		this.traceability = traceability;
		this.qualityDecision = qualityDecision;
		this.anomalyState = anomalyState;
		this.description = description;
	}

	public Anomaly transitionToCorrected(EventTrace toCorrectedTrace) throws IllegalTransition,IllegalTraceErasureTentative{
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
		
		return new Anomaly(id, parentId, childId, correctiveAction, provingDocument, trace, qualityDecision, AnomalyState.CORRECTED, description);
	}

	public Anomaly transitionToResolved(EventTrace toResolvedTrace) throws IllegalTransition,IllegalTraceErasureTentative{
		if(this.anomalyState != AnomalyState.CORRECTED) {
			throw new IllegalTransition("Anomaly must be in CORRECTED state.");
		}
		if(this.provingDocument == null) {
			throw new IllegalTransition("A proving document must be attached to this anomaly to validate the transition.");
		}
		Traceability trace = this.traceability.addToResolvedTrace(toResolvedTrace);
		
		return new Anomaly(id, parentId, childId, correctiveAction, provingDocument, trace, qualityDecision, AnomalyState.RESOLVED, description);
	}
	
	public Anomaly transitionToArchived(EventTrace toArchivedTrace)throws IllegalTransition,IllegalTraceErasureTentative{
		if(this.anomalyState != AnomalyState.RESOLVED) {
			throw new IllegalTransition("Anomaly must be in RESOLVED state.");
		}
		Traceability trace = this.traceability.addToArchivedTrace(toArchivedTrace);
		
		return new Anomaly(id, parentId, childId, correctiveAction, provingDocument, trace, qualityDecision, AnomalyState.ARCHIVED, description);
	}
	
	public Anomaly attachDescription(String description) throws IllegalAttachment{
		if(this.anomalyState != AnomalyState.PENDING) {
			throw new IllegalAttachment("Editing the description is only permitted during the PENDING state.");
		}
		Description newDescription = new Description(description);
		return new Anomaly(id, parentId, childId, correctiveAction, provingDocument, traceability, qualityDecision, anomalyState, newDescription);
	}

	public Anomaly attachCorrectiveAction (String correctiveActionId) throws IllegalAttachment{
		if(this.anomalyState != AnomalyState.PENDING) {
			throw new IllegalAttachment("The state of anomaly must be PENDING to attach corrective action.");
		}
		
		CorrectiveAction document = new CorrectiveAction(correctiveActionId);
		
		return new Anomaly(id, parentId, childId, document, provingDocument, traceability, qualityDecision, anomalyState, description);
	}
	
	public Anomaly attachQualityDecision(QualityDecision newQualityDecision)throws IllegalAttachment{
		if(this.anomalyState != AnomalyState.PENDING) {
			throw new IllegalAttachment("The state of anomaly must be PENDING to attach quality decision.");
		}
		if(newQualityDecision==QualityDecision.EMPTY) {
			throw new IllegalAttachment("Quality decision can't be EMPTY.");
		}
		return new Anomaly(id, parentId, childId, correctiveAction, provingDocument, traceability, newQualityDecision, anomalyState, description);
	}
	
	public Anomaly attachProvingDocument(String provingDocumentId)throws IllegalAttachment{
		if(this.anomalyState != AnomalyState.CORRECTED) {
			throw new IllegalAttachment("The state of anomaly must be CORRECTED to attach a proving document.");
		}
		
		ProvingDocument document = new ProvingDocument(provingDocumentId);
		
		return new Anomaly(id, parentId, childId, correctiveAction, document, traceability, qualityDecision, anomalyState, description);
	}
	
	public Anomaly attachProlongationId(UUID prolongationId)throws IllegalAttachment{
		if(this.anomalyState != AnomalyState.ARCHIVED) {
			throw new IllegalAttachment("The state of anomaly must be ARCHIVED to attach a prolongation ID.");
		}
		if(this.childId != null) {
			throw new IllegalAttachment("A prolongation ID is already attached to this anomaly.");
		}
		return new Anomaly(id, parentId, prolongationId, correctiveAction, provingDocument, traceability, qualityDecision, anomalyState, description);
	}
	
	public UUID getId() {
		return this.id;
	}
	
	public UUID getParentId() {
		return parentId;
	}

	public UUID getChildId() {
		return childId;
	}

	public CorrectiveAction getCorrectiveAction() {
		return correctiveAction;
	}

	public ProvingDocument getProvingDocument() {
		return provingDocument;
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

	private void verifyStructuralConsistency(UUID id, AnomalyState state, CorrectiveAction correctiveAction,
			ProvingDocument provingDocument, QualityDecision qualityDecision, Description description) {
		if(state == null) {
			throw new InconsistentAnomalyStateException("Cannot create anomaly without state.");
		}
		switch(state) {
			case PENDING -> {
				if(description == null || id == null) {
					throw new InconsistentAnomalyStateException("Cannot create anomaly in PENDING state without description, or ID.");
				}
			}
			case CORRECTED -> {
				if(description == null || id == null || qualityDecision == QualityDecision.EMPTY || 
						correctiveAction == null) {
					throw new InconsistentAnomalyStateException("Cannot create anomaly in CORRECTED state without description, ID, corrective action, or quality decision.");
				}
			}
			case RESOLVED -> {
				if(description == null || id == null || qualityDecision == QualityDecision.EMPTY || 
						correctiveAction == null || provingDocument == null) {
					throw new InconsistentAnomalyStateException("Cannot create anomaly in RESOLVED state without description, ID, corrective action, quality decision, or proving document.");
				}
			}
			case ARCHIVED -> {
				if(description == null || id == null || qualityDecision == QualityDecision.EMPTY || 
						correctiveAction == null || provingDocument == null) {
					throw new InconsistentAnomalyStateException("Cannot create anomaly in ARCHIVED state without description, ID, corrective action, quality decision, or proving document.");
				}
			}
		}
	}
}
