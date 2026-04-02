package domain.valueobject;

public record ImpactedQuantity(int quantity) {
	public ImpactedQuantity{
		if(quantity < 0 || quantity > 1000000) {
			throw new IllegalArgumentException("quantity must be a positive number, smaller than 1 million.");
		}
	}
}
