package domain.valueobject;

import domain.exception.InvalidValueException;

public record Description(String description) {
	public Description{
		if(description == null || description.isBlank()) {
			throw new InvalidValueException("Description cannot be null or blank.");
		}
	}
}
