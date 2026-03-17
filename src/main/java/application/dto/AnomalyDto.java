package application.dto;

import java.time.Instant;

import domain.anomaly.AnomalyState;
import domain.valueobject.QualityDecision;
import domain.valueobject.Sector;

public record AnomalyDto(String id,
		String businessId,
		String parentId,
		String prolongationComent,
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
