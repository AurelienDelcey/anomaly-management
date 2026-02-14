package domain;

public class Traceability {
	
	private final EventTrace creation;
	private final EventTrace toCorrected;
	private final EventTrace toResolved;
	private final EventTrace toArchived;
	
	public Traceability(EventTrace creationTrace) {
		this.creation = creationTrace;
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
	
	public Traceability addToCorrectedTrace(EventTrace toCorrectedTrace) throws IllegalTraceErasureTentative{
		if(this.toCorrected != null) {
			throw new IllegalTraceErasureTentative("Traceability for CORRECTED transition already exists.");
		}
		return new Traceability(creation, toCorrectedTrace, toResolved, toArchived);
	}
	
	public Traceability addToResolvedTrace(EventTrace toResolvedTrace) throws IllegalTraceErasureTentative{
		if(this.toResolved != null) {
			throw new IllegalTraceErasureTentative("Traceability for RESOLVED transition already exists.");
		}
		return new Traceability(creation, toCorrected, toResolvedTrace, toArchived);
	}
	
	public Traceability addToArchivedTrace(EventTrace toArchivedTrace) throws IllegalTraceErasureTentative{
		if(this.toArchived != null) {
			throw new IllegalTraceErasureTentative("Traceability for ARCHIVED transition already exists.");
		}
		return new Traceability(creation, toCorrected, toResolved, toArchivedTrace);
	}
}
