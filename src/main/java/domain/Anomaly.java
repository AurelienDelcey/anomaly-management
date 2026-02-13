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

	public Anomaly transitionToCorrected(EventTrace toCorrectedTrace) throws IllegalDomainMachintruc{
		if(this.anomalyState != AnomalyState.PENDING) {
			throw new IllegalDomainMachintruc();
		}
		if(this.correctiveAction == null) {
			throw new IllegalDomainMachintruc();
		}
		if(this.qualityDecision == QualityDecision.EMPTY) {
			throw new IllegalDomainMachintruc();
		}
		Traceability trace = this.traceability.addToCorrectedTrace(toCorrectedTrace);
		
		return new Anomaly(id, parentId, childId, correctiveAction, provingDocument, trace, qualityDecision, AnomalyState.CORRECTED);
	}

	public Anomaly transitionToResolved(EventTrace toResolvedTrace) throws IllegalDomainMachintruc{
		if(this.anomalyState != AnomalyState.CORRECTED) {
			throw new IllegalDomainMachintruc();
		}
		if(this.provingDocument == null) {
			throw new IllegalDomainMachintruc();
		}
		Traceability trace = this.traceability.addToResolvedTrace(toResolvedTrace);
		
		return new Anomaly(id, parentId, childId, correctiveAction, provingDocument, trace, qualityDecision, AnomalyState.RESOLVED);
	}
	
	public Anomaly transitionToArchived(EventTrace toArchivedTrace)throws IllegalDomainMachintruc{
		if(this.anomalyState != AnomalyState.RESOLVED) {
			throw new IllegalDomainMachintruc();
		}
		Traceability trace = this.traceability.addToArchivedTrace(toArchivedTrace);
		
		return new Anomaly(id, parentId, childId, correctiveAction, provingDocument, trace, qualityDecision, AnomalyState.ARCHIVED);
	}

	public Anomaly attachCorrectiveAction (String newCorrectiveAction) throws IllegalDomainMachintruc{
		if(this.anomalyState != AnomalyState.PENDING) {
			throw new IllegalDomainMachintruc();
		}
		
		CorrectiveAction document = new CorrectiveAction(newCorrectiveAction);
		
		return new Anomaly(id, parentId, childId, document, provingDocument, traceability, qualityDecision, anomalyState);
	}
	
	public Anomaly attachQualityDecision (QualityDecision newQualityDecision)throws IllegalDomainMachintruc{
		if(this.anomalyState != AnomalyState.PENDING) {
			throw new IllegalDomainMachintruc();
		}
		if(newQualityDecision==QualityDecision.EMPTY) {
			throw new IllegalDomainMachintruc();
		}
		return new Anomaly(id, parentId, childId, correctiveAction, provingDocument, traceability, newQualityDecision, anomalyState);
	}
	
	public Anomaly attachProvingDocument(String newProvingDocument)throws IllegalDomainMachintruc{
		if(this.anomalyState != AnomalyState.CORRECTED) {
			throw new IllegalDomainMachintruc();
		}
		
		ProvingDocument document = new ProvingDocument(newProvingDocument);
		
		return new Anomaly(id, parentId, childId, correctiveAction, document, traceability, qualityDecision, anomalyState);
	}
	
	public Anomaly attachProlongationId(UUID prolongationId)throws IllegalDomainMachintruc{
		if(this.anomalyState != AnomalyState.ARCHIVED) {
			throw new IllegalDomainMachintruc();
		}
		if(this.childId != null) {
			throw new IllegalDomainMachintruc();
		}
		return new Anomaly(id, parentId, prolongationId, correctiveAction, provingDocument, traceability, qualityDecision, anomalyState);
	}
	
	public UUID getId() {
		return this.id;
	}
}
