package domain.traceability;

import java.time.Instant;

import domain.exception.InvalidValueException;

public record EventTrace(String actorId,String name, Instant instant) {
	public EventTrace{
		if(name == null || name.isBlank() ||actorId == null || actorId.isBlank() || instant == null){
			throw new InvalidValueException("ActorId or name cannot be null or blank and instant cannot be null.");

		}
	}
	

	public String idFormat() {
		return name + " (" + actorId +")" ;
	}
}

