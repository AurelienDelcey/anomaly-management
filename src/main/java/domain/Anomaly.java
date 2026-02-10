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
	
	
	public Anomaly() {
		this.id = UUID.randomUUID();
		this.childId = null;
		this.parentId = null;
		this.traceability = new Traceability();
		this.anomalyState = AnomalyState.PENDING;
		this.correctiveAction = new CorrectiveAction();
		this.provingDocument = new ProvingDocument();
		this.qualityDecision = QualityDecision.EMPTY;
	}
	
	public Anomaly(UUID parentId) {
		this.id = UUID.randomUUID();
		this.childId = null;
		this.parentId = parentId;
		this.traceability = new Traceability();
		this.anomalyState = AnomalyState.PENDING;
		this.correctiveAction = new CorrectiveAction();
		this.provingDocument = new ProvingDocument();
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

	public Anomaly transitionToCorrected() throws IllegalDomainMachintruc{
		if(anomalyState != AnomalyState.PENDING) {
			throw new IllegalDomainMachintruc();
		}
		if(!this.correctiveAction.isExist()) {
			throw new IllegalDomainMachintruc();
		}
		if(qualityDecision == QualityDecision.EMPTY) {
			throw new IllegalDomainMachintruc();
		}
		return new Anomaly(id, parentId, childId, correctiveAction, provingDocument, traceability, qualityDecision, AnomalyState.CORRECTED);
	}

	public Anomaly transitionToResolved() throws IllegalDomainMachintruc{
		if(anomalyState != AnomalyState.CORRECTED) {
			throw new IllegalDomainMachintruc();
		}
		if(!this.provingDocument.isExist()) {
			throw new IllegalDomainMachintruc();
		}
		return new Anomaly(id, parentId, childId, correctiveAction, provingDocument, traceability, qualityDecision, AnomalyState.RESOLVED);
	}

	public Anomaly attachCorrectiveAction (CorrectiveAction newCorrectiveAction) throws IllegalDomainMachintruc{
		if(anomalyState != AnomalyState.PENDING) {
			throw new IllegalDomainMachintruc();
		}
		if(this.correctiveAction.isExist()) {
			throw new IllegalDomainMachintruc();
		}
		if(!newCorrectiveAction.isExist()) {
			throw new IllegalDomainMachintruc();
		}
		return new Anomaly(id, parentId, childId, newCorrectiveAction, provingDocument, traceability, qualityDecision, anomalyState);
	}
	
	public Anomaly attachQualityDecision (QualityDecision newQualityDecision)throws IllegalDomainMachintruc{
		if(anomalyState != AnomalyState.PENDING) {
			throw new IllegalDomainMachintruc();
		}
		if(newQualityDecision==QualityDecision.EMPTY) {
			throw new IllegalDomainMachintruc();
		}
		return new Anomaly(id, parentId, childId, correctiveAction, provingDocument, traceability, newQualityDecision, anomalyState);
	}
	
	public Anomaly attachProvingDocument(ProvingDocument newProvingDocument)throws IllegalDomainMachintruc{
		if(anomalyState != AnomalyState.CORRECTED) {
			throw new IllegalDomainMachintruc();
		}
		if(this.provingDocument.isExist()) {
			throw new IllegalDomainMachintruc();
		}
		if(!newProvingDocument.isExist()) {
			throw new IllegalDomainMachintruc();
		}
		return new Anomaly(id, parentId, childId, correctiveAction, newProvingDocument, traceability, qualityDecision, anomalyState);
	}
	
	
}
