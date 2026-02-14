package domain;

import java.util.UUID;

public class Anomaly {
	
	private final UUID id;
	private final UUID parentId;
	private final UUID childId;
	private final CorrectiveAction correctiveAction;
	private final ProvingDocument provingDocument;
	private final Traceability traceability;
	private final QualityDecision qualityDecision;
	private final AnomalyState anomalyState;
	
	
	public Anomaly(EventTrace creatingTrace) {
		this.id = UUID.randomUUID();
		this.childId = null;
		this.parentId = null;
		this.traceability = new Traceability(creatingTrace);
		this.anomalyState = AnomalyState.PENDING;
		this.correctiveAction = null;
		this.provingDocument = null;
		this.qualityDecision = QualityDecision.EMPTY;
	}
	
	public Anomaly(EventTrace creatingTrace, UUID parentId) {
		this.id = UUID.randomUUID();
		this.childId = null;
		this.parentId = parentId;
		this.traceability = new Traceability(creatingTrace);
		this.anomalyState = AnomalyState.PENDING;
		this.correctiveAction = null;
		this.provingDocument = null;
		this.qualityDecision = QualityDecision.EMPTY;
	}
	
	
	
	private Anomaly(UUID id, UUID parentId, UUID childId, CorrectiveAction correctiveAction,
			ProvingDocument provingDocument, Traceability traceability, QualityDecision qualityDecision,
			AnomalyState anomalyState) {
		this.id = id;
		this.parentId = parentId;
		this.childId = childId;
		this.correctiveAction = correctiveAction;
		this.provingDocument = provingDocument;
		this.traceability = traceability;
		this.qualityDecision = qualityDecision;
		this.anomalyState = anomalyState;
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
		
		return new Anomaly(id, parentId, childId, correctiveAction, provingDocument, trace, qualityDecision, AnomalyState.CORRECTED);
	}

	public Anomaly transitionToResolved(EventTrace toResolvedTrace) throws IllegalTransition,IllegalTraceErasureTentative{
		if(this.anomalyState != AnomalyState.CORRECTED) {
			throw new IllegalTransition("Anomaly must be in CORRECTED state.");
		}
		if(this.provingDocument == null) {
			throw new IllegalTransition("A proving document must be attached to this anomaly to validate the transition.");
		}
		Traceability trace = this.traceability.addToResolvedTrace(toResolvedTrace);
		
		return new Anomaly(id, parentId, childId, correctiveAction, provingDocument, trace, qualityDecision, AnomalyState.RESOLVED);
	}
	
	public Anomaly transitionToArchived(EventTrace toArchivedTrace)throws IllegalTransition,IllegalTraceErasureTentative{
		if(this.anomalyState != AnomalyState.RESOLVED) {
			throw new IllegalTransition("Anomaly must be in RESOLVED state.");
		}
		Traceability trace = this.traceability.addToArchivedTrace(toArchivedTrace);
		
		return new Anomaly(id, parentId, childId, correctiveAction, provingDocument, trace, qualityDecision, AnomalyState.ARCHIVED);
	}

	public Anomaly attachCorrectiveAction (String newCorrectiveAction) throws IllegalAttachment{
		if(this.anomalyState != AnomalyState.PENDING) {
			throw new IllegalAttachment("The state of anomaly must be PENDING to attach corrective action.");
		}
		
		CorrectiveAction document = new CorrectiveAction(newCorrectiveAction);
		
		return new Anomaly(id, parentId, childId, document, provingDocument, traceability, qualityDecision, anomalyState);
	}
	
	public Anomaly attachQualityDecision (QualityDecision newQualityDecision)throws IllegalAttachment{
		if(this.anomalyState != AnomalyState.PENDING) {
			throw new IllegalAttachment("The state of anomaly must be PENDING to attach quality decision.");
		}
		if(newQualityDecision==QualityDecision.EMPTY) {
			throw new IllegalAttachment("Quality decision can't be EMPTY.");
		}
		return new Anomaly(id, parentId, childId, correctiveAction, provingDocument, traceability, newQualityDecision, anomalyState);
	}
	
	public Anomaly attachProvingDocument(String newProvingDocument)throws IllegalAttachment{
		if(this.anomalyState != AnomalyState.CORRECTED) {
			throw new IllegalAttachment("The state of anomaly must be CORRECTED to attach a proving document.");
		}
		
		ProvingDocument document = new ProvingDocument(newProvingDocument);
		
		return new Anomaly(id, parentId, childId, correctiveAction, document, traceability, qualityDecision, anomalyState);
	}
	
	public Anomaly attachProlongationId(UUID prolongationId)throws IllegalAttachment{
		if(this.anomalyState != AnomalyState.ARCHIVED) {
			throw new IllegalAttachment("The state of anomaly must be ARCHIVED to attach a prolongation ID.");
		}
		if(this.childId != null) {
			throw new IllegalAttachment("A prolongation ID is already attached to this anomaly.");
		}
		return new Anomaly(id, parentId, prolongationId, correctiveAction, provingDocument, traceability, qualityDecision, anomalyState);
	}
	
	public UUID getId() {
		return this.id;
	}
}
