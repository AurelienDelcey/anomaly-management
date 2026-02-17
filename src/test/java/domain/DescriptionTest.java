package domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class DescriptionTest {
	
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {""," ","  ","\n"})
	void description_ShouldThrowException_WhenDescriptionIsInvalid(String description) {
		assertThrows(IllegalArgumentException.class, ()-> new Description(description));
	}
	
	@Test
	void description_ShouldReturnValidDescription() {
		Description description = new Description("abcd");
		assertEquals("abcd", description.description());
	}
}
