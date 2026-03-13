package application.dto;

import java.time.Instant;

import domain.anomaly.AnomalyState;
import domain.anomaly.Sector;
import domain.valueobject.QualityDecision;

public record AnomalyDto(String id,
		String parentId,
		String childId,
		Sector sector,
		String correctiveActionId,
		String evidenceId,
		QualityDecision qualityDecision,
		AnomalyState anomalyState,
		String description,
		String createdBy,
		Instant createdAt,
		String correctedBy,
		Instant correctedAt,
		String resolvedBy,
		Instant resolvedAt,
		String archivedBy,
		Instant archivedAt) {

}
