package userInterface;

import application.dto.AnomalyDto;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class ArchivedPanelController {

    @FXML private Label conclusionLabel;
    @FXML private Label correctiveActionLabel;
    @FXML private TextArea descriptionTextArea;
    @FXML private Label evidenceLabel;
    @FXML private Label impactedQuantityLabel;
    @FXML private Label machineLabel;
    @FXML private Label productionOrderLabel;
    @FXML private Label qualityDecisionLabel;
    @FXML private Label sectorLabel;
    
    private ObjectProperty<AnomalyDto> anomalyProperty;
    
    public void initController(ObjectProperty<AnomalyDto> anomalyProperty) {
    	this.anomalyProperty = anomalyProperty;
    	setupLabels();
    }
    
    private void setupLabels() {
    	if(anomalyProperty.get().childId() == null) {
    		conclusionLabel.setText("Valid");
    	}else {
    		conclusionLabel.setText("Invalid");
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
