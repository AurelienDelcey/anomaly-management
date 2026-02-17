package domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CorrectiveActionTest {
	
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {"aaa-1-091991","AAA--091991",
			"AAA-00-","AAA-00-1234567","AAA00091991",
			"AAA-0-12345"})
	void correctiveAction_ShouldThrowException_WhenTheDocumentIdIsInvalid(String docID) {
		assertThrows(IllegalArgumentException.class, ()->new CorrectiveAction(docID));
	}
	
	@Test
	void correctiveAction_ShouldReturnValidCorrectiveAction() {
		CorrectiveAction action = new CorrectiveAction("AAA-0-091991");
		assertEquals("AAA-0-091991",action.documentId());
	}
}
