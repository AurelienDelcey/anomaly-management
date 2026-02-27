package domain.anomaly;

import static org.junit.jupiter.api.Assertions.*;
import static domain.anomaly.AnomalyConstructor.rehydrate;
import static domain.anomaly.AnomalyState.*;
import static domain.valueobject.QualityDecision.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import domain.exception.IllegalTraceErasureTentative;
import domain.exception.InconsistentAnomalyStateException;
import domain.traceability.EventTrace;
import domain.traceability.Traceability;
import domain.valueobject.CorrectiveAction;
import domain.valueobject.Description;
import domain.valueobject.ProvingDocument;

class AnomalyConstructorTest {
	
	private final static Instant FIXED_INSTANT = Instant.parse("2026-02-16T00:00:00Z");
	private final static String DESCRIPTION = "anomalyTest";
	private final static String VALID_DOC_ID = "XXX-000-091991";
	private final static String VALID_ACTOR_ID = "0000";
	private final static String FIXED_UUID = "3f6b8a4c-9e21-4c7f-b8d2-1a5e0f6c2d9b";

	@Test
	void rehydrateAnomalyInPendingState_withValidData_shouldReturnValidAnomaly() {
		Anomaly anomaly = assertDoesNotThrow(()->rehydrate(
					UUID.fromString(FIXED_UUID),null,null,null,null,
					createValidTraceabilityAtState(PENDING),EMPTY,PENDING,
					createValidDescription())
		);
		
		assertEquals(FIXED_UUID, anomaly.getId().toString());
		assertNull(anomaly.getParentId());
		assertNull(anomaly.getChildId());
		assertNull(anomaly.getCorrectiveAction());
		assertNull(anomaly.getProvingDocument());
		assertNotNull(anomaly.getTraceability().getCreation());
		assertNull(anomaly.getTraceability().getToCorrected());
		assertNull(anomaly.getTraceability().getToResolved());
		assertNull(anomaly.getTraceability().getToArchived());
		assertEquals(EMPTY, anomaly.getQualityDecision());
		assertEquals(PENDING, anomaly.getAnomalyState());
		assertEquals(DESCRIPTION, anomaly.getDescription().description());
	}
	
	@Test
	void rehydrateAnomalyInCorrectedState_withValidData_shouldReturnValidAnomaly() {
		Anomaly anomaly = assertDoesNotThrow(()->rehydrate(
					UUID.fromString(FIXED_UUID),null,null,
					createValidCorrectiveAction(),
					null,
					createValidTraceabilityAtState(CORRECTED),NA,CORRECTED,
					createValidDescription())
		);
		
		assertEquals(FIXED_UUID, anomaly.getId().toString());
		assertNull(anomaly.getParentId());
		assertNull(anomaly.getChildId());
		assertEquals(VALID_DOC_ID, anomaly.getCorrectiveAction().documentId());
		assertNull(anomaly.getProvingDocument());
		assertNotNull(anomaly.getTraceability().getCreation());
		assertNotNull(anomaly.getTraceability().getToCorrected());
		assertNull(anomaly.getTraceability().getToResolved());
		assertNull(anomaly.getTraceability().getToArchived());
		assertEquals(NA, anomaly.getQualityDecision());
		assertEquals(CORRECTED, anomaly.getAnomalyState());
		assertEquals(DESCRIPTION, anomaly.getDescription().description());
	}

	@Test
	void rehydrateAnomalyInResolvedState_withValidData_shouldReturnValidAnomaly() {
		Anomaly anomaly = assertDoesNotThrow(()->rehydrate(
					UUID.fromString(FIXED_UUID),null,null,
					createValidCorrectiveAction(),
					createValidProvingDocument(),
					createValidTraceabilityAtState(RESOLVED),NA,RESOLVED,
					createValidDescription())
		);
		
		assertEquals(FIXED_UUID, anomaly.getId().toString());
		assertNull(anomaly.getParentId());
		assertNull(anomaly.getChildId());
		assertEquals(VALID_DOC_ID, anomaly.getCorrectiveAction().documentId());
		assertEquals(VALID_DOC_ID, anomaly.getProvingDocument().documentId());
		assertNotNull(anomaly.getTraceability().getCreation());
		assertNotNull(anomaly.getTraceability().getToCorrected());
		assertNotNull(anomaly.getTraceability().getToResolved());
		assertNull(anomaly.getTraceability().getToArchived());
		assertEquals(NA, anomaly.getQualityDecision());
		assertEquals(RESOLVED, anomaly.getAnomalyState());
		assertEquals(DESCRIPTION, anomaly.getDescription().description());
	}
	
	@Test
	void rehydrateAnomalyInArchivedState_withValidData_shouldReturnValidAnomaly() {
		Anomaly anomaly = assertDoesNotThrow(()->rehydrate(
					UUID.fromString(FIXED_UUID),null,null,
					createValidCorrectiveAction(),
					createValidProvingDocument(),
					createValidTraceabilityAtState(ARCHIVED),NA,ARCHIVED,
					createValidDescription())
		);
		
		assertEquals(FIXED_UUID, anomaly.getId().toString());
		assertNull(anomaly.getParentId());
		assertNull(anomaly.getChildId());
		assertEquals(VALID_DOC_ID, anomaly.getCorrectiveAction().documentId());
		assertEquals(VALID_DOC_ID, anomaly.getProvingDocument().documentId());
		assertNotNull(anomaly.getTraceability().getCreation());
		assertNotNull(anomaly.getTraceability().getToCorrected());
		assertNotNull(anomaly.getTraceability().getToResolved());
		assertNotNull(anomaly.getTraceability().getToArchived());
		assertEquals(NA, anomaly.getQualityDecision());
		assertEquals(ARCHIVED, anomaly.getAnomalyState());
		assertEquals(DESCRIPTION, anomaly.getDescription().description());
	}
	
	@Test
	void rehydrateAnomaly_shouldThrowException_WhenStateIsMissing() {
		assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
				null,null,null,null,null,
				createValidTraceabilityAtState(PENDING),EMPTY,PENDING,
				createValidDescription())
				);
	}
	
	@ParameterizedTest
	@EnumSource(value = AnomalyState.class)
	void rehydrateAnomaly_shouldThrowException_WhenDescriptionIsMissing(AnomalyState state) {
		switch(state) {
			case PENDING ->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						UUID.fromString(FIXED_UUID),null,null,null,null,
						createValidTraceabilityAtState(PENDING),EMPTY,PENDING,
						null)
						);
			}
			case CORRECTED->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						UUID.fromString(FIXED_UUID),null,null,
						createValidCorrectiveAction(),
						null,
						createValidTraceabilityAtState(CORRECTED),NA,CORRECTED,
						null)
						);
			}
			case RESOLVED->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						UUID.fromString(FIXED_UUID),null,null,
						createValidCorrectiveAction(),
						createValidProvingDocument(),
						createValidTraceabilityAtState(RESOLVED),NA,RESOLVED,
						null)
						);
			}
			case ARCHIVED->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						UUID.fromString(FIXED_UUID),null,null,
						createValidCorrectiveAction(),
						createValidProvingDocument(),
						createValidTraceabilityAtState(ARCHIVED),NA,ARCHIVED,
						null)
						);
			}
		}
	}
	
	@ParameterizedTest
	@EnumSource(value = AnomalyState.class)
	void rehydrateAnomaly_shouldThrowException_WhenTraceabilityIsMissing(AnomalyState state) {
		switch(state) {
			case PENDING ->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						UUID.fromString(FIXED_UUID),null,null,null,null,
						null,EMPTY,PENDING,
						createValidDescription())
						);
			}
			case CORRECTED->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						UUID.fromString(FIXED_UUID),null,null,
						createValidCorrectiveAction(),
						null,
						null,NA,CORRECTED,
						createValidDescription())
						);
			}
			case RESOLVED->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						UUID.fromString(FIXED_UUID),null,null,
						createValidCorrectiveAction(),
						createValidProvingDocument(),
						null,NA,RESOLVED,
						createValidDescription())
						);
			}
			case ARCHIVED->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						UUID.fromString(FIXED_UUID),null,null,
						createValidCorrectiveAction(),
						createValidProvingDocument(),
						null,NA,ARCHIVED,
						createValidDescription())
						);
			}
		}
	}
	
	@ParameterizedTest
	@EnumSource(value = AnomalyState.class)
	void rehydrateAnomaly_shouldThrowException_WhenIdIsMissing(AnomalyState state) {
		switch(state) {
			case PENDING ->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						null,null,null,null,null,
						createValidTraceabilityAtState(PENDING),EMPTY,PENDING,
						createValidDescription())
						);
			}
			case CORRECTED->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						null,null,null,
						createValidCorrectiveAction(),
						null,
						createValidTraceabilityAtState(CORRECTED),NA,CORRECTED,
						createValidDescription())
						);
			}
			case RESOLVED->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						null,null,null,
						createValidCorrectiveAction(),
						createValidProvingDocument(),
						createValidTraceabilityAtState(RESOLVED),NA,RESOLVED,
						createValidDescription())
						);
			}
			case ARCHIVED->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						null,null,null,
						createValidCorrectiveAction(),
						createValidProvingDocument(),
						createValidTraceabilityAtState(ARCHIVED),NA,ARCHIVED,
						createValidDescription())
						);
			}
		}
	}
	
	@ParameterizedTest
	@EnumSource(value = AnomalyState.class,
	mode = EnumSource.Mode.EXCLUDE,
	names = "PENDING"
	)
	void rehydrateAnomalyNotPending_shouldThrowException_WhenQualityDecisionIsEmpty(AnomalyState state) {
		switch(state) {
			case PENDING ->{}
			case CORRECTED->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						UUID.fromString(FIXED_UUID),null,null,
						createValidCorrectiveAction(),
						null,
						createValidTraceabilityAtState(CORRECTED),EMPTY,CORRECTED,
						createValidDescription())
						);
			}
			case RESOLVED->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						UUID.fromString(FIXED_UUID),null,null,
						createValidCorrectiveAction(),
						createValidProvingDocument(),
						createValidTraceabilityAtState(RESOLVED),EMPTY,RESOLVED,
						createValidDescription())
						);
			}
			case ARCHIVED->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						UUID.fromString(FIXED_UUID),null,null,
						createValidCorrectiveAction(),
						createValidProvingDocument(),
						createValidTraceabilityAtState(ARCHIVED),EMPTY,ARCHIVED,
						createValidDescription())
						);
			}
		}
	}
	
	@ParameterizedTest
	@EnumSource(value = AnomalyState.class,
	mode = EnumSource.Mode.EXCLUDE,
	names = "PENDING"
	)
	void rehydrateAnomalyNotPending_shouldThrowException_WhenCorrectiveActionIsMissing(AnomalyState state) {
		switch(state) {
			case PENDING ->{}
			case CORRECTED->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						UUID.fromString(FIXED_UUID),null,null,null,null,
						createValidTraceabilityAtState(CORRECTED),NA,CORRECTED,
						createValidDescription())
						);
			}
			case RESOLVED->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						UUID.fromString(FIXED_UUID),null,null,null,
						createValidProvingDocument(),
						createValidTraceabilityAtState(RESOLVED),NA,RESOLVED,
						createValidDescription())
						);
			}
			case ARCHIVED->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						UUID.fromString(FIXED_UUID),null,null,null,
						createValidProvingDocument(),
						createValidTraceabilityAtState(ARCHIVED),NA,ARCHIVED,
						createValidDescription())
						);
			}
		}
	}
	
	@ParameterizedTest
	@EnumSource(value = AnomalyState.class,
	mode = EnumSource.Mode.EXCLUDE,
	names = {"PENDING", "CORRECTED"}
	)
	void rehydrateAnomalyNotPendingOrCorrected_shouldThrowException_WhenProvingDocumentIsMissing(AnomalyState state) {
		switch(state) {
			case PENDING ->{}
			case CORRECTED->{}
			case RESOLVED->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						UUID.fromString(FIXED_UUID),null,null,
						createValidCorrectiveAction(),
						null,
						createValidTraceabilityAtState(RESOLVED),NA,RESOLVED,
						createValidDescription())
						);
			}
			case ARCHIVED->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						UUID.fromString(FIXED_UUID),null,null,
						createValidCorrectiveAction(),
						null,
						createValidTraceabilityAtState(ARCHIVED),NA,ARCHIVED,
						createValidDescription())
						);
			}
		}
	}
	
	@Test
	void rehydrateAnomalyPending_shouldThrowException_WhenProvingDocument() {
		assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
				UUID.fromString(FIXED_UUID),null,null,null,
				createValidProvingDocument(),
				createValidTraceabilityAtState(PENDING),EMPTY,PENDING,
				createValidDescription())
				);
	}
	
	@ParameterizedTest
	@EnumSource(value = AnomalyState.class,
				mode = EnumSource.Mode.EXCLUDE,
				names = "PENDING"
				)
	void rehydrateAnomalyNotPending_shouldThrowException_WhenTraceabilityIsToPendingStep(AnomalyState state) {
			switch(state) {
			case PENDING ->{}
			case CORRECTED->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						UUID.fromString(FIXED_UUID),null,null,
						createValidCorrectiveAction(),
						null,
						createValidTraceabilityAtState(PENDING),NA,CORRECTED,
						createValidDescription())
						);
			}
			case RESOLVED->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						UUID.fromString(FIXED_UUID),null,null,
						createValidCorrectiveAction(),
						createValidProvingDocument(),
						createValidTraceabilityAtState(PENDING),NA,RESOLVED,
						createValidDescription())
						);
			}
			case ARCHIVED->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						UUID.fromString(FIXED_UUID),null,null,
						createValidCorrectiveAction(),
						createValidProvingDocument(),
						createValidTraceabilityAtState(PENDING),NA,ARCHIVED,
						createValidDescription())
						);
			}
		}
	}
	
	@ParameterizedTest
	@EnumSource(value = AnomalyState.class,
				mode = EnumSource.Mode.EXCLUDE,
				names = "CORRECTED"
				)
	void rehydrateAnomalyNotCorrected_shouldThrowException_WhenTraceabilityIsToCorrectedStep(AnomalyState state) {
			switch(state) {
			case PENDING ->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						UUID.fromString(FIXED_UUID),null,null,null,null,
						createValidTraceabilityAtState(CORRECTED),EMPTY,PENDING,
						createValidDescription())
						);
			}
			case CORRECTED->{}
			case RESOLVED->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						UUID.fromString(FIXED_UUID),null,null,
						createValidCorrectiveAction(),
						createValidProvingDocument(),
						createValidTraceabilityAtState(CORRECTED),NA,RESOLVED,
						createValidDescription())
						);
			}
			case ARCHIVED->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						UUID.fromString(FIXED_UUID),null,null,
						createValidCorrectiveAction(),
						createValidProvingDocument(),
						createValidTraceabilityAtState(CORRECTED),NA,ARCHIVED,
						createValidDescription())
						);
			}
		}
	}
	
	@ParameterizedTest
	@EnumSource(value = AnomalyState.class,
				mode = EnumSource.Mode.EXCLUDE,
				names = "RESOLVED"
				)
	void rehydrateAnomalyNotResolved_shouldThrowException_WhenTraceabilityIsToResolvedStep(AnomalyState state) {
			switch(state) {
			case PENDING ->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						UUID.fromString(FIXED_UUID),null,null,null,null,
						createValidTraceabilityAtState(RESOLVED),EMPTY,PENDING,
						createValidDescription())
						);
			}
			case CORRECTED->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						UUID.fromString(FIXED_UUID),null,null,
						createValidCorrectiveAction(),
						null,
						createValidTraceabilityAtState(RESOLVED),NA,CORRECTED,
						createValidDescription())
						);
			}
			case RESOLVED->{}
			case ARCHIVED->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						UUID.fromString(FIXED_UUID),null,null,
						createValidCorrectiveAction(),
						createValidProvingDocument(),
						createValidTraceabilityAtState(RESOLVED),NA,ARCHIVED,
						createValidDescription())
						);
			}
		}
	}
	
	@ParameterizedTest
	@EnumSource(value = AnomalyState.class,
				mode = EnumSource.Mode.EXCLUDE,
				names = "ARCHIVED"
				)
	void rehydrateAnomalyNotArchived_shouldThrowException_WhenTraceabilityIsToArchivedStep(AnomalyState state) {
			switch(state) {
			case PENDING ->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						UUID.fromString(FIXED_UUID),null,null,null,null,
						createValidTraceabilityAtState(ARCHIVED),EMPTY,PENDING,
						createValidDescription())
						);
			}
			case CORRECTED->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						UUID.fromString(FIXED_UUID),null,null,
						createValidCorrectiveAction(),
						null,
						createValidTraceabilityAtState(ARCHIVED),NA,CORRECTED,
						createValidDescription())
						);
			}
			case RESOLVED->{
				assertThrows(InconsistentAnomalyStateException.class, ()->rehydrate(
						UUID.fromString(FIXED_UUID),null,null,
						createValidCorrectiveAction(),
						createValidProvingDocument(),
						createValidTraceabilityAtState(ARCHIVED),NA,RESOLVED,
						createValidDescription())
						);
			}
			case ARCHIVED->{}
		}
	}
	
	private EventTrace createValidTrace () {
		return new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
	}
	
	private CorrectiveAction createValidCorrectiveAction() {
		return new CorrectiveAction(VALID_DOC_ID);
	}
	
	private ProvingDocument createValidProvingDocument() {
		return new ProvingDocument(VALID_DOC_ID);
	}
	
	private Description createValidDescription() {
		return new Description(DESCRIPTION);
	}
	
	private Traceability createValidTraceabilityAtState(AnomalyState state) throws IllegalTraceErasureTentative {
		Traceability traceability = new Traceability(createValidTrace());
		return switch(state) {
			case PENDING -> {
				yield traceability;
			}
			case CORRECTED -> {
				traceability = traceability.addToCorrectedTrace(createValidTrace());
				yield traceability;
			}
			case RESOLVED -> {
				traceability = traceability.addToCorrectedTrace(createValidTrace());
				traceability = traceability.addToResolvedTrace(createValidTrace());
				yield traceability;
			}
			case ARCHIVED -> {
				traceability = traceability.addToCorrectedTrace(createValidTrace());
				traceability = traceability.addToResolvedTrace(createValidTrace());
				traceability = traceability.addToArchivedTrace(createValidTrace());
				yield traceability;
			}
		};
	}
}

