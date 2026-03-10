package application;

import java.time.Instant;

import domain.anomaly.AnomalyState;
import domain.valueobject.QualityDecision;

public record AnomalyDto(String id,
		String parentId,
		String childId,
		String correctiveActionId,
		String evidenceId,
		QualityDecision qualityDecision,
		AnomalyState anomalyState,
		String description,
		String createBy,
		Instant createAt,
		String correctedBy,
		Instant correctedAt,
		String resolvedBy,
		Instant resolvedAt,
		String archivedBy,
		Instant archivedAt) {

}
