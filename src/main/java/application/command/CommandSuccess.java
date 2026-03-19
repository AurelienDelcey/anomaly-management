package application.command;

import java.util.UUID;

public record CommandSuccess(UUID anomalyId) implements CommandResult{

}
