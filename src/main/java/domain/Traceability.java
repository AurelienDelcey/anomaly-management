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
	
	public Traceability addToCorrectedTrace(EventTrace ToCorrectedTrace) throws IllegalDomainMachintruc{
		if(this.toCorrected != null) {
			throw new IllegalDomainMachintruc();
		}
		return new Traceability(creation, ToCorrectedTrace, toResolved, toArchived);
	}
	
	public Traceability addToResolvedTrace(EventTrace toResolvedTrace) throws IllegalDomainMachintruc{
		if(this.toResolved != null) {
			throw new IllegalDomainMachintruc();
		}
		return new Traceability(creation, toCorrected, toResolvedTrace, toArchived);
	}
	
	public Traceability addToArchivedTrace(EventTrace toArchivedTrace) throws IllegalDomainMachintruc{
		if(this.toArchived != null) {
			throw new IllegalDomainMachintruc();
		}
		return new Traceability(creation, toCorrected, toResolved, toArchivedTrace);
	}
}
