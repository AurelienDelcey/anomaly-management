package userInterface.detail.dynamicPanel;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;

import application.command.AnomalyCommandService;
import application.command.CommandFailure;
import application.command.CommandResult;
import application.command.CommandSuccess;
import application.dto.AnomalyDto;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Modality;
import javafx.stage.Stage;
import userInterface.detail.GetProlongationMessageViewController;
import userInterface.dialog.ErrorViewController;

public class ResolvedPanelController implements Panel{

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
	private Consumer<String> feedbackCallback;
    private AnomalyCommandService commandService;
    private String prolongationMessage;
    
    public void initController(ObjectProperty<AnomalyDto> anomalyProperty, AnomalyCommandService commandService, Consumer<UUID> updateCallback, Consumer<String> feedbackCallback) {
		this.anomalyProperty = anomalyProperty;
		this.commandService = commandService;
		this.updateCallback = updateCallback;
		this.feedbackCallback = feedbackCallback;
		
		bindTransitionButton();
		setupLabels();
	}

	@FXML
    void onClickArchiveAnomaly() {
    	UUID id = UUID.fromString(anomalyProperty.get().id());
    	if(validationGroup.getSelectedToggle() == validButton ) {
        	CommandResult result = commandService.transitionToArchived(id);
        	switch (result) {
    	    	case CommandSuccess success ->{
    	    		feedbackCallback.accept("Success!!");
    	    		updateCallback.accept(success.anomalyId());
    	    	}
    	    	case CommandFailure failure ->{
    	    		feedbackCallback.accept(failure.message());
    	    	}
        	};
    	}else if(validationGroup.getSelectedToggle() == invalidButton ) {
    		getMessage();
    		if(this.prolongationMessage == null) {
    			return;
    		}
    		CommandResult result = commandService.transitionToArchivedWithProlongation(id, this.prolongationMessage);
        	switch (result) {
    	    	case CommandSuccess success ->{
    	    		feedbackCallback.accept("Success!!");
    	    		updateCallback.accept(id);
    	    		updateCallback.accept(success.anomalyId());
    	    	}
    	    	case CommandFailure failure ->{
    	    		feedbackCallback.accept(failure.message());
    	    	}
        	};
    	}
    }
    
    private void bindTransitionButton() {
    	archiveAnomalyButton.disableProperty().bind(
    			validationGroup.selectedToggleProperty().isNull()
    			);
    }
    
    private void setupLabels() {
    	String evidence = anomalyProperty.get().evidenceId();
    	evidenceLabel.setText(evidence);
    	
    	String correctiveAction = anomalyProperty.get().correctiveActionId();
    	correctiveActionLabel.setText(correctiveAction);
    	
    	String description = anomalyProperty.get().description();
    	descriptionTextArea.setText(description);

    	Integer productionOrder = anomalyProperty.get().productionOrder();
    	productionOrderLabel.setText(productionOrder.toString());
    	
    	Integer impactedQuantity = anomalyProperty.get().impactedQuantity();
    	impactedQuantityLabel.setText(impactedQuantity.toString());
    	
    	String sector = anomalyProperty.get().sector();
    	sectorLabel.setText(sector);
    	
    	String machine = anomalyProperty.get().machine();
    	machineLabel.setText(machine);
    	
    	String qualityDecision = anomalyProperty.get().qualityDecision();
    	qualityDecisionLabel.setText(qualityDecision);
    }
    
    private void setProlongationMessage(String message) {
    	this.prolongationMessage = message;
    }

	private void getMessage() {
		try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/getProlongationMessageView.fxml"));
	        Parent view = loader.load();
	
	        GetProlongationMessageViewController controller = loader.getController();
	        controller.initController((e)->setProlongationMessage(e));
	        Scene scene = new Scene(view);
			Stage stage = new Stage();
			stage.setScene(scene);
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.initOwner(archiveAnomalyButton.getScene().getWindow());
			stage.showAndWait();
			
	    } catch (IOException e) {
	        e.printStackTrace();
	        showError("Unexpected error occurred");
	    }
	}
	
	private void showError(String message) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/errorView.fxml"));
			Parent view = loader.load();
			ErrorViewController controller = loader.getController();
			controller.initController(message);
			
			Scene scene = new Scene(view);
			Stage stage = new Stage();
			stage.setScene(scene);
			
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.initOwner(archiveAnomalyButton.getScene().getWindow());
			
			stage.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
}

