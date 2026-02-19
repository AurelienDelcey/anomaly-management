package application;

public sealed interface CommandResult 
	permits CommandSucces, CommandFailure{
	
}
