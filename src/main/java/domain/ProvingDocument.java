package domain;

public record ProvingDocument(String documentId) {
	public ProvingDocument{
		if(documentId == null || !documentId.matches("[A-Z]{3}-[0-9]{1,}-[0-9]{6}")) {
			throw new IllegalArgumentException("document id has an invalid format or is null.");
		}
	}
}