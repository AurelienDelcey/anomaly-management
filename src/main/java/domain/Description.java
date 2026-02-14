package domain;

public record Description(String description) {
	public Description{
		if(description == null || description.isBlank()) {
			throw new IllegalArgumentException("Description cannot be null or blank.");
		}
	}
}
