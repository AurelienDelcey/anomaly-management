package domain.traceability;

import java.time.Instant;

public record EventTrace(String actorId, Instant instant) {
	public EventTrace{
		if(actorId == null || actorId.isBlank() || instant == null){
			throw new IllegalArgumentException("ActorId cannot be null or blank and instant cannot be null.");

		}
	}
}
