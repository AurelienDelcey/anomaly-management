package domain;

public record CorrectiveAction(String documentId) {
	public CorrectiveAction{
		if(documentId == null || !documentId.matches("[A-Z]{3}-[0-9]{1,}-[0-9]{6}")) {
			throw new IllegalStringFormat();
		}
	}
}
