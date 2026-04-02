package domain.anomaly;

import java.util.UUID;

import domain.traceability.Traceability;
import domain.valueobject.BusinessId;
import domain.valueobject.CorrectiveAction;
import domain.valueobject.Description;
import domain.valueobject.Evidence;
import domain.valueobject.ImpactedQuantity;
import domain.valueobject.Machine;
import domain.valueobject.ProductionOrder;
import domain.valueobject.ProlongationContext;
import domain.valueobject.QualityDecision;
import domain.valueobject.Sector;

public final class AnomalyConstructor {
	
	private AnomalyConstructor() {}
	
	public static Anomaly rehydrate(UUID id, BusinessId businessId, ProlongationContext prolongationContext, UUID childId, Sector sector, CorrectiveAction correctiveAction,
			ImpactedQuantity quantity, ProductionOrder productionOrder, Machine machine, 
			Evidence evidence, Traceability traceability, QualityDecision qualityDecision,
			AnomalyState anomalyState, Description description)  {
		
		return new Anomaly(id, businessId, prolongationContext, childId, sector,
				quantity, productionOrder, machine, correctiveAction, evidence, traceability,
				qualityDecision, anomalyState, description);
	}
}
