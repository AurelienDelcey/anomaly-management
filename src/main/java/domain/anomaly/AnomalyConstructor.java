package domain.anomaly;

import java.util.UUID;

import domain.exception.InconsistentAnomalyStateException;
import domain.traceability.Traceability;
import domain.valueobject.CorrectiveAction;
import domain.valueobject.Description;
import domain.valueobject.Evidence;
import domain.valueobject.QualityDecision;

public final class AnomalyConstructor {
	
	private AnomalyConstructor() {}
	
	public static Anomaly rehydrate(UUID id, UUID parentId, UUID childId, Sector sector, CorrectiveAction correctiveAction,
			Evidence evidence, Traceability traceability, QualityDecision qualityDecision,
			AnomalyState anomalyState, Description description) throws InconsistentAnomalyStateException {
		
		return new Anomaly(id, parentId, childId, sector,
				correctiveAction, evidence, traceability,
				qualityDecision, anomalyState, description);
	}
}
