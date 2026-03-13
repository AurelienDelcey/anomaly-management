package application.dto;

import java.time.Instant;
import java.util.UUID;

import domain.anomaly.Anomaly;
import domain.anomaly.AnomalyState;
import domain.anomaly.Sector;
import domain.traceability.EventTrace;
import domain.traceability.Traceability;
import domain.valueobject.QualityDecision;

public final class AnomalyDtoMapper {
	
	
	
	private AnomalyDtoMapper() {
	}

	public static AnomalyDto mapToDto(Anomaly anomaly) {
		Traceability traceability = anomaly.getTraceability();
		
		String id = stringOrNullFromUuid(anomaly.getId());
		String parentId = stringOrNullFromUuid(anomaly.getParentId());
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
		return new AnomalyDto(id, parentId, childId, sector, correctiveActionId, evidenceId, qualityDecision, anomalyState, description, createBy, createAt, correctedBy, correctedAt, resolvedBy, resolvedAt, archivedBy, archivedAt);
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
}
