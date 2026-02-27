package domain.exception;

public class IllegalTransition extends DomainException{

	private static final long serialVersionUID = 7766355511128753735L;

	public IllegalTransition() {
		super();
	}

	public IllegalTransition(String message, Throwable cause) {
		super(message, cause);
	}

	public IllegalTransition(String message) {
		super(message);
	}
}
