package domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import domain.exception.IllegalTraceErasureTentative;
import domain.traceability.EventTrace;
import domain.traceability.Traceability;

class TraceabilityTest {
	
	private static final Instant FIXED_INSTANT = Instant.parse("2026-02-16T00:00:00Z");
	private static final String VALID_ACTOR_ID = "0000";

	@Test
	void constructor_ShouldReturnValidTraceability() {
		EventTrace creationTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		Traceability trace = new Traceability(creationTrace);
		
		assertNotNull(trace.getCreation());
		assertNull(trace.getToCorrected());
		assertNull(trace.getToResolved());
		assertNull(trace.getToArchived());
		
		assertEquals(VALID_ACTOR_ID, trace.getCreation().actorId());
		assertEquals(FIXED_INSTANT, trace.getCreation().instant());
	}
	
	@Test
	void constructor_ShouldReturnException_WhenCreationTraceIsNull() {
		assertThrows(IllegalArgumentException.class, ()-> new Traceability(null));
	}
	
	@Test
	void addToCorrectedTrace_ShouldReturnValidTraceability() {
		EventTrace correctedTrace = getValidEventTrace();
		Traceability trace = getValidTraceability();
		
		Traceability traceabilityWithToCorrectedTrace = assertDoesNotThrow(()->trace.addToCorrectedTrace(correctedTrace));
		
		assertNull(trace.getToCorrected());
		assertNotNull(traceabilityWithToCorrectedTrace.getToCorrected());
		assertEquals(VALID_ACTOR_ID, traceabilityWithToCorrectedTrace.getToCorrected().actorId());
		assertEquals(FIXED_INSTANT, traceabilityWithToCorrectedTrace.getToCorrected().instant());
		assertNull(traceabilityWithToCorrectedTrace.getToResolved());
		assertNull(traceabilityWithToCorrectedTrace.getToArchived());
	}
	
	@Test
	void addToResolvedTrace_ShouldReturnValidTraceability() {
		EventTrace resolvedTrace = getValidEventTrace();
		Traceability trace = getValidTraceability();
		
		Traceability traceabilityWithToResolvedTrace = assertDoesNotThrow(()->trace.addToResolvedTrace(resolvedTrace));
		
		assertNull(trace.getToResolved());
		assertNotNull(traceabilityWithToResolvedTrace.getToResolved());
		assertEquals(VALID_ACTOR_ID, traceabilityWithToResolvedTrace.getToResolved().actorId());
		assertEquals(FIXED_INSTANT, traceabilityWithToResolvedTrace.getToResolved().instant());
		assertNull(traceabilityWithToResolvedTrace.getToCorrected());
		assertNull(traceabilityWithToResolvedTrace.getToArchived());
	}
	
	@Test
	void addToArchivedTrace_ShouldReturnValidTraceability() {
		EventTrace archivedTrace = getValidEventTrace();
		Traceability trace = getValidTraceability();
		
		Traceability traceabilityWithToArchivedTrace = assertDoesNotThrow(()->trace.addToArchivedTrace(archivedTrace));
		
		assertNull(trace.getToArchived());
		assertNotNull(traceabilityWithToArchivedTrace.getToArchived());
		assertEquals(VALID_ACTOR_ID, traceabilityWithToArchivedTrace.getToArchived().actorId());
		assertEquals(FIXED_INSTANT, traceabilityWithToArchivedTrace.getToArchived().instant());
		assertNull(traceabilityWithToArchivedTrace.getToCorrected());
		assertNull(traceabilityWithToArchivedTrace.getToResolved());
	}
	
	@Test
	void addToCorrectedTrace_ShouldReturnException_WhenToCorrectedTraceAlreadyExists(){
		EventTrace correctedTrace = getValidEventTrace();
		EventTrace otherToCorrectedTrace = getValidEventTrace();
		Traceability trace = getValidTraceability();
		Traceability traceabilityWithToCorrectedTrace = assertDoesNotThrow(()->trace.addToCorrectedTrace(correctedTrace));
		
		assertThrows(IllegalTraceErasureTentative.class, ()->traceabilityWithToCorrectedTrace.addToCorrectedTrace(otherToCorrectedTrace));
	}
	
	@Test
	void addToResolvedTrace_ShouldReturnException_WhenToResolvedTraceAlreadyExists(){
		EventTrace toResolvedTrace = getValidEventTrace();
		EventTrace otherToResolvedTrace = getValidEventTrace();
		Traceability trace = getValidTraceability();
		Traceability traceabilityWithToResolvedTrace = assertDoesNotThrow(()->trace.addToResolvedTrace(toResolvedTrace));
		
		assertThrows(IllegalTraceErasureTentative.class, ()->traceabilityWithToResolvedTrace.addToResolvedTrace(otherToResolvedTrace));
	}
	
	@Test
	void addToArchivedTrace_ShouldReturnException_WhenToArchivedTraceAlreadyExists(){
		EventTrace archivedTrace = getValidEventTrace();
		EventTrace otherToArchivedTrace = getValidEventTrace();
		Traceability trace = getValidTraceability();
		Traceability traceabilityWithToArchivedTrace = assertDoesNotThrow(()->trace.addToArchivedTrace(archivedTrace));
		
		assertThrows(IllegalTraceErasureTentative.class, ()-> traceabilityWithToArchivedTrace.addToArchivedTrace(otherToArchivedTrace));
	}
	
	private EventTrace getValidEventTrace() {
		return new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
	}
	private Traceability getValidTraceability() {
		return new Traceability(getValidEventTrace());
	}
}
