package domain.exception;

public class IllegalTraceErasureTentative extends DomainException{

	private static final long serialVersionUID = -6068094883659085321L;

	public IllegalTraceErasureTentative() {
		super();
	}

	public IllegalTraceErasureTentative(String message, Throwable cause) {
		super(message, cause);
	}

	public IllegalTraceErasureTentative(String message) {
		super(message);
	}
}
