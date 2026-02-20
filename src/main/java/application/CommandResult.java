package application;

public sealed interface CommandResult 
	permits CommandSuccess, CommandFailure{
	
}
