package domain;

public class IllegalAttachment extends Exception{

	private static final long serialVersionUID = 6612377276232857269L;

	public IllegalAttachment(String message) {
		super(message);
	}
}
