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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class CorrectedPanelController {

    @FXML private Label correctiveActionLabel;
    @FXML private TextArea descriptionTextArea;
    @FXML private TextField evidenceTextField;
    @FXML private Label impactedQuantityLabel;
    @FXML private Label machineLabel;
    @FXML private Label productionOrderLabel;
    @FXML private Label qualityDecisionLabel;
    @FXML private Button resolveAnomalyButton;
    @FXML private Label sectorLabel;
    @FXML private Button validEvidenceButton;
    
    private ObjectProperty<AnomalyDto> anomalyProperty;
    private Consumer<UUID> updateCallback;
	private Consumer<String> feedbackCallback;
    private AnomalyCommandService commandService;
    
    @FXML
	public void initialize() {
		
	}
    
    @FXML
    void onClickResolveAnomaly() {
    	if(anomalyProperty.get() == null) {
    		return;//TODO popup/handle
    	}
    	UUID id = UUID.fromString(anomalyProperty.get().id());
    	CommandResult result = commandService.transitionToResolved(id);
    	
    	switch (result) {
	    	case CommandSuccess success ->{
	    		feedbackCallback.accept("Success!!");
	    		updateCallback.accept(success.anomalyId());
	    	}
	    	case CommandFailure failure ->{
	    		feedbackCallback.accept(failure.message());
	    	}//TODO popup/handle
    	};
    }

    @FXML
    void onClickValidEvidence() {
    	if(anomalyProperty.get() == null) {
    		return;//TODO popup/handle
    	}
    	UUID id = UUID.fromString(anomalyProperty.get().id());
    	String text = evidenceTextField.getText();
    	CommandResult result = commandService.attachEvidence(id, text);
    	
    	switch (result) {
    	case CommandSuccess success ->{
    		feedbackCallback.accept("Success!!");
    		updateCallback.accept(success.anomalyId());
    	}
    	case CommandFailure failure ->{
    		feedbackCallback.accept(failure.message());
    		setupLabels();
    	}//TODO popup/handle
    	};
    }
    
    public void initController(ObjectProperty<AnomalyDto> anomalyProperty, AnomalyCommandService commandService, Consumer<UUID> updateCallback, Consumer<String> feedbackCallback) {
    	this.anomalyProperty = anomalyProperty;
    	this.commandService = commandService;
    	this.updateCallback = updateCallback;
    	this.feedbackCallback = feedbackCallback;
    	
    	bindTransitionButton();
    	setupLabels();
    }
    
    private void bindTransitionButton() {
    	resolveAnomalyButton.disableProperty().bind(
    			evidenceTextField.textProperty().isEmpty()
    			);
    }
    
    private void setupLabels() {
    	if(anomalyProperty.get() == null) {
    		return;
    	}
    	
    	String evidence = anomalyProperty.get().evidenceId();
    	if(evidence != null && !(evidence.isBlank())) {
    		evidenceTextField.setText(evidence);
    	}
    	
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
