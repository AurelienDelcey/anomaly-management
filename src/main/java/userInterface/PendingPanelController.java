package userInterface;

import application.dto.AnomalyDto;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

public class PendingPanelController {

    @FXML private Button correctAnomalyButton;
    @FXML private Button correctImpactedQuantity;
    @FXML private Button correctProductionOrderButton;
    @FXML private Button correctedDescriptionButton;
    @FXML private Button validationCorrectiveActionButton;
    @FXML private ToggleGroup qualityDecisionGroup;
    @FXML private RadioButton repairButton;
    @FXML private RadioButton scrapButton;
    @FXML private RadioButton naButton;
    @FXML private TextField correctiveActionTextField;
    @FXML private TextField impactedQuantityTextField;
    @FXML private TextField productionOrderTextField;
    @FXML private TextArea descriptionTextArea;
    @FXML private ComboBox<String> machineCombo;
    @FXML private ComboBox<String> sectorCombo;
    
    private SimpleObjectProperty<AnomalyDto> anomalyProperty;
    
    @FXML
	public void initialize() {
		
	}
    
    @FXML
    void onSelectMachine() {

    }

    @FXML
    void onClickCorrectAnomaly() {

    }

    @FXML
    void onClickCorrectProductionOrder() {

    }

    @FXML
    void onClickCorrectQuantity() {

    }

    @FXML
    void onClickCorrectedDescritpion() {

    }

    @FXML
    void onClickNa() {

    }

    @FXML
    void onClickRepair() {

    }

    @FXML
    void onClickScrap() {

    }

    @FXML
    void onClickValidationCorrectiveAction() {

    }

    @FXML
    void onSelectSector() {

    }
    
    @FXML
    void onActionQuantity(ActionEvent event) {
    	onClickCorrectQuantity();
    }
    
    @FXML
    void onActionCorrectiveAction(ActionEvent event) {
    	onClickValidationCorrectiveAction();
    }
    
    @FXML
    void onActionProductionOrder(ActionEvent event) {
    	onClickCorrectProductionOrder();
    }
    
    public void initController(AnomalyDto anomaly) {
    	
    }

}
