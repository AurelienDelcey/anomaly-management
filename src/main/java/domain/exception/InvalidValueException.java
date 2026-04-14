package domain.exception;

public class InvalidValueException extends DomainException{

	private static final long serialVersionUID = -2089840729118228170L;

	public InvalidValueException() {
		super();
	}

	public InvalidValueException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidValueException(String message) {
		super(message);
	}
	
	
}
