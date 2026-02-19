package application;

public final class CommandFailure implements CommandResult{
	
	private final String message;

	public CommandFailure(String message) {
		this.message = message;
	}
	
	
}
