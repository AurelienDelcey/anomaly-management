package infrastructure;

public class StructureInconsistencyException extends TechnicalException{

	private static final long serialVersionUID = 9013804107155052466L;

	public StructureInconsistencyException() {
		super();
	}

	public StructureInconsistencyException(String message) {
		super(message);
	}

	public StructureInconsistencyException(String message, Throwable cause) {
		super(message, cause);
	}
}
