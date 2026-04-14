package domain.valueobject;

import domain.exception.InvalidValueException;

public record ImpactedQuantity(int quantity) {
	public ImpactedQuantity{
		if(quantity < 0 || quantity > 1000000) {
			throw new InvalidValueException("quantity must be a positive number, smaller than 1 million.");
		}
	}
}
