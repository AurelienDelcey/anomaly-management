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
import domain.valueobject.CorrectiveAction;
import domain.valueobject.Description;
import domain.valueobject.Evidence;
import domain.valueobject.QualityDecision;
import infrastructure.exception.TechnicalException;
import static domain.anomaly.AnomalyConstructor.rehydrate;

public class AnomalyRepositoryMapper {
	

	private AnomalyRepositoryMapper() {
	}
	
	public static Anomaly mapAnomaly(ResultSet result) throws SQLException, IllegalTraceErasureTentative, InconsistentAnomalyStateException {
		String id = result.getString("id");
		String parentId = result.getString("parent_id");
		String childId = result.getString("child_id");
		String state = result.getString("anomaly_state");
		String decision = result.getString("quality_decision");
	
		UUID anomalyId = uuidOrNullFromString(id);
		UUID anomalyParentId = uuidOrNullFromString(parentId);
		UUID anomalyChildId = uuidOrNullFromString(childId);
		
		String description = result.getString("description");
		Description anomalyDescription = description == null?null:new Description(description);
		
		String correctiveAction = result.getString("corrective_action_id");
		CorrectiveAction anomalyCorrectiveAction = correctiveAction == null?null:new CorrectiveAction(correctiveAction);
		
		String evidence = result.getString("proving_document_id");
		Evidence anomalyEvidence = evidence == null?null:new Evidence(evidence);
		
		String createdBy = result.getString("created_by");
		Timestamp createdAt = result.getTimestamp("created_at");
		String correctedBy = result.getString("corrected_by");
		Timestamp correctedAt = result.getTimestamp("corrected_at");
		String resolvedBy = result.getString("resolved_by");
		Timestamp resolvedAt = result.getTimestamp("resolved_at");
		String archivedBy = result.getString("archived_by");
		Timestamp archivedAt = result.getTimestamp("archived_at");
		
		Instant createdInstant = instantOrNullFromTimestamp(createdAt);
		Instant correctedInstant = instantOrNullFromTimestamp(correctedAt);
		Instant resolvedInstant = instantOrNullFromTimestamp(resolvedAt);
		Instant archivedInstant = instantOrNullFromTimestamp(archivedAt);
		
		EventTrace created = new EventTrace(createdBy, createdInstant);
		EventTrace corrected = eventTraceOrNull(correctedBy, correctedInstant);
		EventTrace resolved = eventTraceOrNull(resolvedBy, resolvedInstant);
		EventTrace archived = eventTraceOrNull(archivedBy, archivedInstant);
		
		Traceability traceability =  buildTraceability(created, corrected, resolved, archived);
		
		QualityDecision qualityDecision = switch(decision) {
		case "EMPTY" -> QualityDecision.EMPTY;
		case "NA" -> QualityDecision.NA;
		case "REPAIR" -> QualityDecision.REPAIR;
		case "SCRAP" -> QualityDecision.SCRAP;
		default -> throw new TechnicalException("Unknown decision: " + decision);
		};
		
		AnomalyState anomalyState = switch(state) {
		case "PENDING" -> AnomalyState.PENDING;
		case "CORRECTED" -> AnomalyState.CORRECTED; 
		case "RESOLVED" -> AnomalyState.RESOLVED;
		case "ARCHIVED" -> AnomalyState.ARCHIVED;
		default -> throw new TechnicalException("Unknown state: " + state);
		};
		
		Anomaly anomaly = rehydrate(
				anomalyId, anomalyParentId, anomalyChildId, null, //add sector mapping
				anomalyCorrectiveAction, anomalyEvidence, 
				traceability, qualityDecision, anomalyState, anomalyDescription);
		
		return anomaly;
	}
	
	private static UUID uuidOrNullFromString(String id) {
		return id == null ? null : UUID.fromString(id);
	}
	
	private static Instant instantOrNullFromTimestamp(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toInstant();
	}
	
	private static EventTrace eventTraceOrNull(String actor, Instant instant) {
		return instant == null || actor == null ? null : new EventTrace(actor, instant);
	}
	
	private static Traceability buildTraceability(EventTrace created, EventTrace corrected, EventTrace resolved, EventTrace archived) throws IllegalTraceErasureTentative {
		Traceability traceability = new Traceability(created);
		traceability = corrected == null?traceability:traceability.addToCorrectedTrace(corrected);
		traceability = resolved == null?traceability:traceability.addToResolvedTrace(resolved);
		traceability = archived == null?traceability:traceability.addToArchivedTrace(archived);
		return traceability;
	}
}