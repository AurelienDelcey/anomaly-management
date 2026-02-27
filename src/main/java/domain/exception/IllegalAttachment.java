package domain.exception;

public class IllegalAttachment extends DomainException{

	private static final long serialVersionUID = 6612377276232857269L;

	public IllegalAttachment() {
		super();
	}

	public IllegalAttachment(String message, Throwable cause) {
		super(message, cause);
	}

	public IllegalAttachment(String message) {
		super(message);
	}
}
