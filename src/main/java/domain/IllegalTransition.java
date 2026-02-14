package domain;

public class IllegalTransition extends Exception{

	private static final long serialVersionUID = 7766355511128753735L;

	public IllegalTransition(String message) {
		super(message);
	}
}
