package domain.exception;

public class InconsistentAnomalyStateException extends DomainException{

	private static final long serialVersionUID = 8178331756570906129L;

	public InconsistentAnomalyStateException(String message) {
		super(message);
	}
}
