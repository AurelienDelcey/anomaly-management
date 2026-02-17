package domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ProvingDocumentTest {

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {"aaa-1-091991","AAA--091991",
			"AAA-00-","AAA-00-1234567","AAA00091991",
			"AAA-0-12345"})
	void provingDocument_ShouldThrowException_WhenTheDocumentIdIsInvalid(String docId) {
		assertThrows(IllegalArgumentException.class, ()->new ProvingDocument(docId));
	}
	
	@Test
	void provingDocument_ShouldReturnValidProvingDocument() {
		ProvingDocument provingDocument = new ProvingDocument("AAA-0-091991");
		assertEquals("AAA-0-091991", provingDocument.documentId());
	}
}
