package domain.valueobject;

import domain.exception.InvalidValueException;

public record ProductionOrder(int productionOrder) {
	public ProductionOrder{
		if(productionOrder <= 0 || productionOrder > 1000000) {
			throw new InvalidValueException("ProductionOrder must be a positive number, smaller than 1 million.");
		}
	}
}
