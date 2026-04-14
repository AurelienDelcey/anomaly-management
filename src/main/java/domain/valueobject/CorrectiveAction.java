package domain.valueobject;

import domain.exception.InvalidValueException;

public record CorrectiveAction(String documentId) {
	public CorrectiveAction{
		if(documentId == null || !documentId.matches("[A-Z]{3}-[0-9]{1,}-[0-9]{6}")) {
			throw new InvalidValueException("document ID has an invalid format or is null.");
		}
	}
}
