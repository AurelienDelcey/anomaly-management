package userInterface.detail.dynamicPanel;

import java.util.UUID;
import java.util.function.Consumer;

import application.command.AnomalyCommandService;
import application.dto.AnomalyDto;
import javafx.beans.property.ObjectProperty;

public interface Panel {
	public void initController(ObjectProperty<AnomalyDto> anomalyProperty, AnomalyCommandService commandService, Consumer<UUID> updateCallback, Consumer<String> feedbackCallback);
}
