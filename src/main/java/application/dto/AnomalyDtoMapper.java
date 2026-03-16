package application.dto;

import java.time.Instant;
import java.util.UUID;

import domain.anomaly.Anomaly;
import domain.anomaly.AnomalyState;
import domain.traceability.EventTrace;
import domain.traceability.Traceability;
import domain.valueobject.ProlongationContext;
import domain.valueobject.QualityDecision;
import domain.valueobject.Sector;

public final class AnomalyDtoMapper {
	
	
	
	private AnomalyDtoMapper() {
	}

	public static AnomalyDto mapToDto(Anomaly anomaly) {
		Traceability traceability = anomaly.getTraceability();
		
		String id = stringOrNullFromUuid(anomaly.getId());
		String parentId = idStringOrNullFromProlongationContext(anomaly.getProlongationContext());
		String prolongationComment = commentStringOrNullFromProlongationContext(anomaly.getProlongationContext());
		String childId = stringOrNullFromUuid(anomaly.getChildId());
		Sector sector = anomaly.getSector();
		String correctiveActionId = anomaly.getCorrectiveAction() == null ? null : anomaly.getCorrectiveAction().documentId();
		String evidenceId = anomaly.getEvidence() == null ? null : anomaly.getEvidence().documentId();
		QualityDecision qualityDecision = anomaly.getQualityDecision();
		AnomalyState anomalyState = anomaly.getAnomalyState();
		String description = anomaly.getDescription().description();
		String createBy = actorOrNull(traceability.getCreation());
		Instant createAt = instantOrNull(traceability.getCreation());
		String correctedBy = actorOrNull(traceability.getToCorrected());
		Instant correctedAt = instantOrNull(traceability.getToCorrected());
		String resolvedBy = actorOrNull(traceability.getToResolved());
		Instant resolvedAt = instantOrNull(traceability.getToResolved());
		String archivedBy = actorOrNull(traceability.getToArchived());
		Instant archivedAt = instantOrNull(traceability.getToArchived());
		return new AnomalyDto(id, parentId, prolongationComment, childId, sector, correctiveActionId, evidenceId, qualityDecision, anomalyState, description, createBy, createAt, correctedBy, correctedAt, resolvedBy, resolvedAt, archivedBy, archivedAt);
	}
	
	private static String actorOrNull(EventTrace trace) {
	    return trace == null ? null : trace.actorId();
	}

	private static Instant instantOrNull(EventTrace trace) {
	    return trace == null ? null : trace.instant();
	}
	
	private static String stringOrNullFromUuid(UUID uuid) {
		return uuid == null ? null : uuid.toString();
	}
	
	private static String idStringOrNullFromProlongationContext(ProlongationContext context) {
		return context == null ? null : context.parentId().toString();
	}
	
	private static String commentStringOrNullFromProlongationContext(ProlongationContext context) {
		return context == null ? null : context.prolongationComment();
	}
}
