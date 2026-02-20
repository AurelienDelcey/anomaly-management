package application;

import java.time.Instant;

import domain.anomaly.Anomaly;
import domain.anomaly.AnomalyState;
import domain.traceability.EventTrace;
import domain.traceability.Traceability;
import domain.valueobject.QualityDecision;

public final class AnomalyDtoMapper {
	
	
	
	private AnomalyDtoMapper() {
	}

	public static AnomalyDto mapToDto(Anomaly anomaly) {
		Traceability traceability = anomaly.getTraceability();
		
		String id = anomaly.getId().toString();
		String correctiveActionId = anomaly.getCorrectiveAction() == null ? null : anomaly.getCorrectiveAction().documentId();
		String provingDocumentId = anomaly.getProvingDocument() == null ? null : anomaly.getProvingDocument().documentId();
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
		return new AnomalyDto(id, correctiveActionId, provingDocumentId, qualityDecision, anomalyState, description, createBy, createAt, correctedBy, correctedAt, resolvedBy, resolvedAt, archivedBy, archivedAt);
	}
	
	private static String actorOrNull(EventTrace trace) {
	    return trace == null ? null : trace.actorId();
	}

	private static Instant instantOrNull(EventTrace trace) {
	    return trace == null ? null : trace.instant();
	}
}
