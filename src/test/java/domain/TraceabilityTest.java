package domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class TraceabilityTest {
	
	private final static Instant FIXED_INSTANT = Instant.parse("2026-02-16T00:00:00Z");
	private final static String VALID_ACTOR_ID = "0000";

	@Test
	void Constructor_ShouldReturnValideTraceability() {
		EventTrace creatingTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		Traceability trace = new Traceability(creatingTrace);
		
		assertNotNull(trace.getCreation());
		assertNull(trace.getToCorrected());
		assertNull(trace.getToResolved());
		assertNull(trace.getToArchived());
		
		assertEquals(VALID_ACTOR_ID, trace.getCreation().actorId());
		assertEquals(FIXED_INSTANT, trace.getCreation().instant());
	}
	
	@Test
	void addToCorrectedTrace_ShouldReturnValideTraceability() {
		EventTrace creatingTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		EventTrace correctedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		Traceability trace = new Traceability(creatingTrace);
		
		Traceability traceabilityWithToCorrectedTrace = assertDoesNotThrow(()->trace.addToCorrectedTrace(correctedTrace));
		
		assertNull(trace.getToCorrected());
		assertNotNull(traceabilityWithToCorrectedTrace.getToCorrected());
		assertEquals(VALID_ACTOR_ID, traceabilityWithToCorrectedTrace.getToCorrected().actorId());
		assertEquals(FIXED_INSTANT, traceabilityWithToCorrectedTrace.getToCorrected().instant());
		assertNull(traceabilityWithToCorrectedTrace.getToResolved());
		assertNull(traceabilityWithToCorrectedTrace.getToArchived());
	}
	
	@Test
	void addToResolvedTrace_ShouldReturnValideTraceability() {
		EventTrace creatingTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		EventTrace resolvedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		Traceability trace = new Traceability(creatingTrace);
		
		Traceability traceabilityWithToResolvedTrace = assertDoesNotThrow(()->trace.addToResolvedTrace(resolvedTrace));
		
		assertNull(trace.getToResolved());
		assertNotNull(traceabilityWithToResolvedTrace.getToResolved());
		assertEquals(VALID_ACTOR_ID, traceabilityWithToResolvedTrace.getToResolved().actorId());
		assertEquals(FIXED_INSTANT, traceabilityWithToResolvedTrace.getToResolved().instant());
		assertNull(traceabilityWithToResolvedTrace.getToCorrected());
		assertNull(traceabilityWithToResolvedTrace.getToArchived());
	}
	
	@Test
	void addToArchivedTrace_ShouldReturnValideTraceability() {
		EventTrace creatingTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		EventTrace archivedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		Traceability trace = new Traceability(creatingTrace);
		
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
		EventTrace creatingTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		EventTrace correctedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		EventTrace otherToCorrectedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		Traceability trace = new Traceability(creatingTrace);
		
		Traceability traceabilityWithToCorrectedTrace = assertDoesNotThrow(()->trace.addToCorrectedTrace(correctedTrace));
		assertThrows(IllegalTraceErasureTentative.class, ()->traceabilityWithToCorrectedTrace.addToCorrectedTrace(otherToCorrectedTrace));
	}
	
	@Test
	void addToResolvedTrace_ShouldReturnException_WhenToResolvedTraceAlreadyExists(){
		EventTrace creatingTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		EventTrace toResolvedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		EventTrace otherToResolvedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		Traceability trace = new Traceability(creatingTrace);
		
		Traceability traceabilityWithToResolvedTrace = assertDoesNotThrow(()->trace.addToResolvedTrace(toResolvedTrace));
		assertThrows(IllegalTraceErasureTentative.class, ()->traceabilityWithToResolvedTrace.addToResolvedTrace(otherToResolvedTrace));
	}
	
	@Test
	void addToArchivedTrace_ShouldReturnException_WhenToArchivedTraceAlreadyExists(){
		EventTrace creatingTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		EventTrace archivedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		EventTrace otherToArchivedTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		Traceability trace = new Traceability(creatingTrace);
		
		Traceability traceabilityWithToArchivedTrace = assertDoesNotThrow(()->trace.addToArchivedTrace(archivedTrace));
		assertThrows(IllegalTraceErasureTentative.class, ()-> traceabilityWithToArchivedTrace.addToArchivedTrace(otherToArchivedTrace));
	}
}
