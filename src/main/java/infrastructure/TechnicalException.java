package infrastructure;

public class TechnicalException extends RuntimeException{

	private static final long serialVersionUID = 3827471396034181347L;

	public TechnicalException() {
		super();
	}

	public TechnicalException(String message) {
		super(message);
	}

	public TechnicalException(String message, Throwable cause) {
		super(message, cause);
	}
}
