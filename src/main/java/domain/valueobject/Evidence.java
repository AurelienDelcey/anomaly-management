package domain.valueobject;

public record Evidence(String documentId) {
	public Evidence{
		if(documentId == null || !documentId.matches("[A-Z]{3}-[0-9]{1,}-[0-9]{6}")) {
			throw new IllegalArgumentException("document ID has an invalid format or is null.");
		}
	}
}