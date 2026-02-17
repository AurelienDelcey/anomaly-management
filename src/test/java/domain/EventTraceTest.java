package domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class EventTraceTest {
	
	private final static Instant FIXED_INSTANT = Instant.parse("2026-02-16T00:00:00Z");
	private final static String VALID_ACTOR_ID = "0000";

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {""," ","  ","\n"})
	void eventTrace_ShouldThrowException_WhenActorIdIsInvalid(String actor) {
		assertThrows(IllegalArgumentException.class, ()-> new EventTrace(actor, FIXED_INSTANT));
	}
	
	@Test
	void eventTrace_ShouldThrowException_WhenInstantIsNull() {
		assertThrows(IllegalArgumentException.class, ()-> new EventTrace(VALID_ACTOR_ID,null));
	}
	
	@Test
	void eventTrace_ShouldReturnValidEventTrace() {
		EventTrace trace = new EventTrace(VALID_ACTOR_ID,FIXED_INSTANT);
		assertEquals(VALID_ACTOR_ID, trace.actorId());
		assertEquals(FIXED_INSTANT, trace.instant());
	}
}
