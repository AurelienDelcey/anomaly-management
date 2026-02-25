package domain.anomaly;

import java.util.UUID;

import domain.traceability.Traceability;
import domain.valueobject.CorrectiveAction;
import domain.valueobject.Description;
import domain.valueobject.ProvingDocument;
import domain.valueobject.QualityDecision;

public final class AnomalyConstructor {
	
	private AnomalyConstructor() {}
	
	public static Anomaly rehydrate(UUID id, UUID parentId, UUID childId, CorrectiveAction correctiveAction,
			ProvingDocument provingDocument, Traceability traceability, QualityDecision qualityDecision,
			AnomalyState anomalyState, Description description) {
		
		return new Anomaly(id, parentId, childId,
				correctiveAction, provingDocument, traceability,
				qualityDecision, anomalyState, description);
	}
}
