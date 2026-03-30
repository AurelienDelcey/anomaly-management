package userInterface;

import java.util.UUID;
import java.util.function.Consumer;

import application.command.AnomalyCommandService;
import application.command.CommandFailure;
import application.command.CommandResult;
import application.command.CommandSuccess;
import application.dto.AnomalyDto;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;

public class ResolvedPanelController {

    @FXML private Label correctiveActionLabel;
    @FXML private TextArea descriptionTextArea;
    @FXML private Label evidenceLabel;
    @FXML private Label impactedQuantityLabel;
    @FXML private Label machineLabel;
    @FXML private Label productionOrderLabel;
    @FXML private Label qualityDecisionLabel;
    @FXML private Button archiveAnomalyButton;
    @FXML private Label sectorLabel;
    @FXML private ToggleGroup validationGroup;
    @FXML private RadioButton validButton;
    @FXML private RadioButton invalidButton;

    private ObjectProperty<AnomalyDto> anomalyProperty;
    private Consumer<UUID> updateCallback;
    private AnomalyCommandService commandService;
    
    @FXML
    void onClickArchiveAnomaly() {
    	if(anomalyProperty.get() == null) {
    		return;//TODO popup/handle
    	}
    	UUID id = UUID.fromString(anomalyProperty.get().id());
    	if(validationGroup.getSelectedToggle() == validButton ) {
        	CommandResult result = commandService.transitionToArchived(id);
        	switch (result) {
    	    	case CommandSuccess success ->{
    	    		updateCallback.accept(success.anomalyId());
    	    	}
    	    	case CommandFailure failure ->{}//TODO popup/handle
        	};
    	}else if(validationGroup.getSelectedToggle() == invalidButton ) {
    		CommandResult result = commandService.transitionToArchivedWithProlongation(id, "prolongationMessage");//TODO popup for prolongationMessage
        	switch (result) {
    	    	case CommandSuccess success ->{
    	    		updateCallback.accept(id);
    	    		updateCallback.accept(success.anomalyId());// load new prolongation directly
    	    	}
    	    	case CommandFailure failure ->{}//TODO popup/handle
        	};
    	}
    }
    
    public void initController(ObjectProperty<AnomalyDto> anomalyProperty, AnomalyCommandService commandService, Consumer<UUID> updateCallback) {
    	this.anomalyProperty = anomalyProperty;
    	this.commandService = commandService;
    	this.updateCallback = updateCallback;
    	
    	bindTransitionButton();
    	setupLabels();
    }
    
    private void bindTransitionButton() {
    	archiveAnomalyButton.disableProperty().bind(
    			validationGroup.selectedToggleProperty().isNull()
    			);
    }
    
    private void setupLabels() {
    	if(anomalyProperty.get() == null) {
    		return;
    	}
    	
    	String evidence = anomalyProperty.get().evidenceId();
    	evidenceLabel.setText(evidence);
    	
    	String correctiveAction = anomalyProperty.get().correctiveActionId();
    	correctiveActionLabel.setText(correctiveAction);
    	
    	String description = anomalyProperty.get().description();
    	descriptionTextArea.setText(description);

    	Integer ProductionOrder = anomalyProperty.get().productionOrder();
    	productionOrderLabel.setText(ProductionOrder.toString());
    	
    	Integer impactedQuantity = anomalyProperty.get().impactedQuantity();
    	impactedQuantityLabel.setText(impactedQuantity.toString());
    	
    	String sector = anomalyProperty.get().sector();
    	sectorLabel.setText(sector);
    	
    	String machine = anomalyProperty.get().machine();
    	machineLabel.setText(machine);
    	
    	String qualityDecision = anomalyProperty.get().qualityDecision();
    	qualityDecisionLabel.setText(qualityDecision);
    }

}

