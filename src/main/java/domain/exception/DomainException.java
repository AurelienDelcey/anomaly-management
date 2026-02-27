package domain.exception;

public abstract class DomainException extends Exception{

	private static final long serialVersionUID = 7342351339768992529L;

	public DomainException() {
		super();
	}

	public DomainException(String message, Throwable cause) {
		super(message, cause);
	}

	public DomainException(String message) {
		super(message);
	}
}
