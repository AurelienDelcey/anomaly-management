package domain.valueobject;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import domain.exception.InvalidValueException;

class DescriptionTest {
	
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {""," ","  ","\n"})
	void description_ShouldThrowException_WhenDescriptionIsInvalid(String description) {
		assertThrows(InvalidValueException.class, ()-> new Description(description));
	}
	
	@Test
	void shouldReturnValidDescription_WhenDescriptionIsValid() {
		Description description = new Description("abcd");
		assertEquals("abcd", description.description());
	}
}
