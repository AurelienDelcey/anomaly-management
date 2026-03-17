package domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class EvidenceTest {

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {"aaa-1-091991","AAA--091991",
			"AAA-00-","AAA-00-1234567","AAA00091991",
			"AAA-0-12345"})
	void evidence_ShouldThrowException_WhenTheDocumentIdIsInvalid(String docId) {
		assertThrows(IllegalArgumentException.class, ()->new Evidence(docId));
	}
	
	@Test
	void evidence_ShouldReturnValidEvidence() {
		Evidence evidence = new Evidence("AAA-0-091991");
		assertEquals("AAA-0-091991", evidence.documentId());
	}
}
