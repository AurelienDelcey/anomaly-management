package application.dto;

import java.time.Instant;

public record AnomalyDto(String id,
		String businessId,
		String parentId,
		String prolongationComent,
		String childId,
		String sector,
		String correctiveActionId,
		int productionOrder,
		int impactedQuantity,
		String machine,
		String evidenceId,
		String qualityDecision,
		String anomalyState,
		String description,
		String createdBy,
		Instant createdAt,
		String correctedBy,
		Instant correctedAt,
		String resolvedBy,
		Instant resolvedAt,
		String archivedBy,
		Instant archivedAt) {

	@Override
	public boolean equals(Object o) {
	    if (this == o) return true;
	    if (!(o instanceof AnomalyDto other)) return false;
	    return id.equals(other.id());
	}

	    @Override
	public int hashCode() {
		return this.id.hashCode();
	}
	
	

}
