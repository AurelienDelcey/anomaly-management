package infrastructure.exception;

public class AnomalyNotFoundException extends Exception{

	private static final long serialVersionUID = 7300275750801149382L;

	public AnomalyNotFoundException() {
		super();
	}

	public AnomalyNotFoundException(String message) {
		super(message);
	}

	public AnomalyNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
