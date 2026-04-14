package infrastructure.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import domain.anomaly.Anomaly;
import domain.anomaly.AnomalyState;
import domain.exception.IllegalTraceErasureTentative;
import domain.exception.InconsistentAnomalyStateException;
import domain.traceability.EventTrace;
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
import infrastructure.exception.TechnicalException;
import static domain.anomaly.AnomalyConstructor.rehydrate;

public class AnomalyRepositoryMapper {
	

	private AnomalyRepositoryMapper() {
	}
	
	public static Anomaly mapAnomaly(ResultSet result) throws SQLException, IllegalTraceErasureTentative, InconsistentAnomalyStateException {
		String id = result.getString("id");
		String parentId = result.getString("parent_id");
		String prolongationComment = result.getString("prolongation_comment");
		String childId = result.getString("child_id");
		String state = result.getString("anomaly_state");
		String decision = result.getString("quality_decision");
		String sector = result.getString("sector");
		String machine = result.getString("machine");
	
		UUID anomalyId = uuidOrNullFromString(id);
		UUID anomalyChildId = uuidOrNullFromString(childId);
		UUID anomalyParentId = uuidOrNullFromString(parentId);
		
		Integer year = getNullableInt(result,"year");
		Integer sequence = getNullableInt(result,"sequence");
		BusinessId businessId = year == null || sequence == null ? null : new BusinessId(year, sequence);
		
		ProlongationContext context = prolongationContextOrNull(anomalyParentId, prolongationComment);
		
		String description = result.getString("description");
		Description anomalyDescription = description == null ? null : new Description(description);
		
		Integer quantity = getNullableInt(result,"impacted_quantity");
		ImpactedQuantity impactedQuantity = quantity == null ? null : new ImpactedQuantity(quantity);
		
		Integer order = getNullableInt(result,"production_order");
		ProductionOrder productionOrder = order == null ? null : new ProductionOrder(order);
		
		String correctiveAction = result.getString("corrective_action_id");
		CorrectiveAction anomalyCorrectiveAction = correctiveAction == null ? null : new CorrectiveAction(correctiveAction);
		
		String evidence = result.getString("proving_document_id");
		Evidence anomalyEvidence = evidence == null ? null : new Evidence(evidence);
		
		String createdBy = result.getString("created_by");
		String createdName = result.getString("created_name");
		Timestamp createdAt = result.getTimestamp("created_at");
		String correctedBy = result.getString("corrected_by");
		String correctedName = result.getString("corrected_name");
		Timestamp correctedAt = result.getTimestamp("corrected_at");
		String resolvedBy = result.getString("resolved_by");
		String resolvedName = result.getString("resolved_name");
		Timestamp resolvedAt = result.getTimestamp("resolved_at");
		String archivedBy = result.getString("archived_by");
		String archivedName = result.getString("archived_name");
		Timestamp archivedAt = result.getTimestamp("archived_at");
		
		Instant createdInstant = instantOrNullFromTimestamp(createdAt);
		Instant correctedInstant = instantOrNullFromTimestamp(correctedAt);
		Instant resolvedInstant = instantOrNullFromTimestamp(resolvedAt);
		Instant archivedInstant = instantOrNullFromTimestamp(archivedAt);
		
		EventTrace created = new EventTrace(createdBy, createdName, createdInstant);
		EventTrace corrected = eventTraceOrNull(correctedBy, correctedName, correctedInstant);
		EventTrace resolved = eventTraceOrNull(resolvedBy, resolvedName, resolvedInstant);
		EventTrace archived = eventTraceOrNull(archivedBy, archivedName, archivedInstant);
		
		Traceability traceability =  buildTraceability(created, corrected, resolved, archived);
		
		QualityDecision qualityDecision = null;

		if (decision != null) {
		    try {
		    	qualityDecision = QualityDecision.valueOf(decision);
		    } catch (IllegalArgumentException e) {
		        throw new TechnicalException("Unknown decision: " + decision, e);
		    }
		}
		
		AnomalyState anomalyState = null;

		if (state != null) {
		    try {
		    	anomalyState = AnomalyState.valueOf(state);
		    } catch (IllegalArgumentException e) {
		        throw new TechnicalException("Unknown state: " + state, e);
		    }
		}
		
		Sector anomalySector = null;

		if (sector != null) {
		    try {
		    	anomalySector = Sector.valueOf(sector);
		    } catch (IllegalArgumentException e) {
		        throw new TechnicalException("Unknown sector: " + sector, e);
		    }
		}
		
		Machine anomalyMachine = null;

		if (machine != null) {
		    try {
		        anomalyMachine = Machine.valueOf(machine);
		    } catch (IllegalArgumentException e) {
		        throw new TechnicalException("Unknown machine: " + machine, e);
		    }
		}
		
		Anomaly anomaly = rehydrate(
				anomalyId, businessId, context, anomalyChildId, anomalySector,
				anomalyCorrectiveAction, impactedQuantity, productionOrder, anomalyMachine, anomalyEvidence, 
				traceability, qualityDecision, anomalyState, anomalyDescription);
		
		return anomaly;
	}
	
	private static UUID uuidOrNullFromString(String id) {
		return id == null ? null : UUID.fromString(id);
	}
	
	private static Instant instantOrNullFromTimestamp(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toInstant();
	}
	
	private static EventTrace eventTraceOrNull(String actor, String name, Instant instant) {
		return instant == null || actor == null || name == null ? null : new EventTrace(actor, name, instant);
	}
	
	private static ProlongationContext prolongationContextOrNull(UUID parentId, String comment) {
		return parentId == null || comment == null ? null : new ProlongationContext(parentId, comment);
	}
	
	private static Traceability buildTraceability(EventTrace created, EventTrace corrected, EventTrace resolved, EventTrace archived) throws IllegalTraceErasureTentative {
		Traceability traceability = new Traceability(created);
		traceability = corrected == null?traceability:traceability.addToCorrectedTrace(corrected);
		traceability = resolved == null?traceability:traceability.addToResolvedTrace(resolved);
		traceability = archived == null?traceability:traceability.addToArchivedTrace(archived);
		return traceability;
	}
	
	private static Integer getNullableInt(ResultSet rs, String column) throws SQLException {
	    int value = rs.getInt(column);
	    return rs.wasNull() ? null : value;
	}
}