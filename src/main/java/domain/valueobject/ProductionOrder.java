package domain.valueobject;

public record ProductionOrder(int productionOrder) {
	public ProductionOrder{
		if(productionOrder <= 0) {
			throw new IllegalArgumentException("ProductionOrder must be a positive number");
		}
	}
}
