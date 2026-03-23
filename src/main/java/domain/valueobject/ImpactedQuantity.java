package domain.valueobject;

public record ImpactedQuantity(int quantity) {
	public ImpactedQuantity{
		if(quantity > 0) {
			throw new IllegalArgumentException("quantity must be a positive number");
		}
	}
}
