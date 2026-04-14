package domain.valueobject;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import domain.exception.InvalidValueException;

class ProlongationContextTest {
	
	private final static String FIXED_UUID = "3f6b8a4c-9e21-4c7f-b8d2-1a5e0f6c2d9b";

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {""," ","  ","\n"})
	void prolongationContext_ShouldThrowException_WhenCommentIsInvalid(String comment) {
		assertThrows(InvalidValueException.class, ()-> new ProlongationContext(UUID.randomUUID(),comment));
	}
	
	@Test
	void prolongationContext_ShouldThrowException_WhenParentIdIsNull() {
		assertThrows(InvalidValueException.class, ()-> new ProlongationContext(null,"valid"));
	}
	
	@Test
	void shouldReturnValidProlongationContext_WhenParentIdAndCommentIsValid() {
		ProlongationContext prolongationContext = new ProlongationContext(UUID.fromString(FIXED_UUID), "abcd");
		assertEquals("abcd", prolongationContext.prolongationComment());
		assertEquals(FIXED_UUID, prolongationContext.parentId().toString());
	}
}
