package domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import domain.exception.InvalidValueException;
import domain.traceability.EventTrace;

class EventTraceTest {
	
	private static final Instant FIXED_INSTANT = Instant.parse("2026-02-16T00:00:00Z");
	private static final String VALID_ACTOR_ID = "0000";
	private static final String VALID_ACTOR_NAME = "Dupont";

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {""," ","  ","\n"})
	void eventTrace_ShouldThrowException_WhenActorIdIsInvalid(String actor) {
		assertThrows(InvalidValueException.class, ()-> new EventTrace(actor, VALID_ACTOR_NAME, FIXED_INSTANT));
	}
	
	@Test
	void eventTrace_ShouldThrowException_WhenInstantIsNull() {
		assertThrows(InvalidValueException.class, ()-> new EventTrace(VALID_ACTOR_ID, VALID_ACTOR_NAME, null));
	}
	
	@Test
	void eventTrace_ShouldReturnValidEventTrace() {
		EventTrace trace = new EventTrace(VALID_ACTOR_ID, VALID_ACTOR_NAME, FIXED_INSTANT);
		assertEquals(VALID_ACTOR_ID, trace.actorId());
		assertEquals(FIXED_INSTANT, trace.instant());
	}
}
