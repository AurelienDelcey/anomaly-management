package domain;

import java.time.Instant;

public record EventTrace(String actorId, Instant instant) {
	public EventTrace{
		if(actorId == null || actorId.isBlank() || instant == null){
			throw new IllegalArgumentException();
		}
	}
}
