package application.command;

public sealed interface CommandResult 
	permits CommandSuccess, CommandFailure{
	
}
