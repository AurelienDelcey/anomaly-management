package domain.traceability;

import domain.exception.IllegalTraceErasureTentative;
import domain.exception.InvalidValueException;

public class Traceability {
	
	private final EventTrace creation;
	private final EventTrace toCorrected;
	private final EventTrace toResolved;
	private final EventTrace toArchived;
	
	public Traceability(EventTrace creation) {
		if(creation == null) {
			throw new InvalidValueException("Creation trace cannot be null.");
		}
		this.creation = creation;
		this.toCorrected = null;
		this.toResolved = null;
		this.toArchived = null;
	}
	
	private Traceability(EventTrace creation, EventTrace toCorrected, EventTrace toResolved, EventTrace toArchived) {
		this.creation = creation;
		this.toCorrected = toCorrected;
		this.toResolved = toResolved;
		this.toArchived = toArchived;
	}
	
	public Traceability addToCorrectedTrace(EventTrace correctedTrace) throws IllegalTraceErasureTentative{
		if(this.toCorrected != null) {
			throw new IllegalTraceErasureTentative("Traceability for CORRECTED transition already exists.");
		}
		return new Traceability(creation, correctedTrace, toResolved, toArchived);
	}
	
	public Traceability addToResolvedTrace(EventTrace resolvedTrace) throws IllegalTraceErasureTentative{
		if(this.toResolved != null) {
			throw new IllegalTraceErasureTentative("Traceability for RESOLVED transition already exists.");
		}
		return new Traceability(creation, toCorrected, resolvedTrace, toArchived);
	}
	
	public Traceability addToArchivedTrace(EventTrace archivedTrace) throws IllegalTraceErasureTentative{
		if(this.toArchived != null) {
			throw new IllegalTraceErasureTentative("Traceability for ARCHIVED transition already exists.");
		}
		return new Traceability(creation, toCorrected, toResolved, archivedTrace);
	}

	public EventTrace getCreation() {
		return creation;
	}

	public EventTrace getToCorrected() {
		return toCorrected;
	}

	public EventTrace getToResolved() {
		return toResolved;
	}

	public EventTrace getToArchived() {
		return toArchived;
	}
	
	
}
