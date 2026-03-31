package userInterface;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import application.command.AnomalyCommandService;
import application.command.CommandFailure;
import application.command.CommandResult;
import application.command.CommandSuccess;
import application.dto.AnomalyDto;
import domain.valueobject.Machine;
import domain.valueobject.QualityDecision;
import domain.valueobject.Sector;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
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
    
    private ObjectProperty<AnomalyDto> anomalyProperty;
    private Consumer<UUID> updateCallback;
    private AnomalyCommandService commandService;
    
    @FXML
	public void initialize() {
		
	}
    
    @FXML
    void onSelectMachine() {
    	if(anomalyProperty.get() == null) {
    		return;//TODO popup/handle
    	}
    	UUID id = UUID.fromString(anomalyProperty.get().id());
    	String machine = machineCombo.getSelectionModel().getSelectedItem();
    	CommandResult result = commandService.attachMachine(id, machine);
    	
    	switch (result) {
    	case CommandSuccess success ->{
    		updateCallback.accept(success.anomalyId());
    	}
    	case CommandFailure failure ->{
    		setupSelections();
    	}//TODO popup/handle
    	};
    }

    @FXML
    void onClickCorrectAnomaly() {
    	if(anomalyProperty.get() == null) {
    		return;//TODO popup/handle
    	}
    	UUID id = UUID.fromString(anomalyProperty.get().id());
    	CommandResult result = commandService.transitionToCorrected(id);
    	
    	switch (result) {
	    	case CommandSuccess success ->{
	    		updateCallback.accept(success.anomalyId());
	    	}
	    	case CommandFailure failure ->{}//TODO popup/handle
    	};
    }

    @FXML
    void onClickCorrectProductionOrder() {
    	if(anomalyProperty.get() == null) {
    		return;//TODO popup/handle
    	}
    	UUID id = UUID.fromString(anomalyProperty.get().id());
    	Integer order = Integer.valueOf(productionOrderTextField.getText());
    	CommandResult result = commandService.attachProductionOrder(id, order);
    	
    	switch (result) {
    	case CommandSuccess success ->{
    		updateCallback.accept(success.anomalyId());
    	}
    	case CommandFailure failure ->{
    		setupSelections();
    	}//TODO popup/handle
    	};
    }

    @FXML
    void onClickCorrectQuantity() {
    	if(anomalyProperty.get() == null) {
    		return;//TODO popup/handle
    	}
    	UUID id = UUID.fromString(anomalyProperty.get().id());
    	Integer quantity = Integer.valueOf(impactedQuantityTextField.getText());
    	CommandResult result = commandService.attachImpactedQuantity(id, quantity);
    	
    	switch (result) {
    	case CommandSuccess success ->{
    		updateCallback.accept(success.anomalyId());
    	}
    	case CommandFailure failure ->{
    		setupSelections();
    	}//TODO popup/handle
    	};
    }

    @FXML
    void onClickCorrectedDescritpion() {
    	if(anomalyProperty.get() == null) {
    		return;//TODO popup/handle
    	}
    	UUID id = UUID.fromString(anomalyProperty.get().id());
    	String text = descriptionTextArea.getText();
    	CommandResult result = commandService.attachDescription(id, text);
    	
    	switch (result) {
    	case CommandSuccess success ->{
    		updateCallback.accept(success.anomalyId());
    	}
    	case CommandFailure failure ->{
    		setupSelections();
    	}//TODO popup/handle
    	};
    }

    @FXML
    void onClickNa() {
    	if(anomalyProperty.get() == null) {
    		return;//TODO popup/handle
    	}
    	UUID id = UUID.fromString(anomalyProperty.get().id());
    	CommandResult result = commandService.attachQualityDecision(id, QualityDecision.NA);
    	
    	switch (result) {
    	case CommandSuccess success ->{
    		updateCallback.accept(success.anomalyId());
    	}
    	case CommandFailure failure ->{
    		setupSelections();
    	}//TODO popup/handle
    	};
    }

    @FXML
    void onClickRepair() {
    	if(anomalyProperty.get() == null) {
    		return;//TODO popup/handle
    	}
    	UUID id = UUID.fromString(anomalyProperty.get().id());
    	CommandResult result = commandService.attachQualityDecision(id, QualityDecision.REPAIR);
    	
    	switch (result) {
    	case CommandSuccess success ->{
    		updateCallback.accept(success.anomalyId());
    	}
    	case CommandFailure failure ->{
    		setupSelections();
    	}//TODO popup/handle
    	};
    }

    @FXML
    void onClickScrap() {
    	if(anomalyProperty.get() == null) {
    		return;//TODO popup/handle
    	}
    	UUID id = UUID.fromString(anomalyProperty.get().id());
    	CommandResult result = commandService.attachQualityDecision(id, QualityDecision.SCRAP);
    	
    	switch (result) {
    	case CommandSuccess success ->{
    		updateCallback.accept(success.anomalyId());
    	}
    	case CommandFailure failure ->{
    		setupSelections();
    	}//TODO popup/handle
    	};
    }

    @FXML
    void onClickValidationCorrectiveAction() {
    	if(anomalyProperty.get() == null) {
    		return;//TODO popup/handle
    	}
    	UUID id = UUID.fromString(anomalyProperty.get().id());
    	String text = correctiveActionTextField.getText();
    	CommandResult result = commandService.attachCorrectiveAction(id, text);
    	
    	switch (result) {
    	case CommandSuccess success ->{
    		updateCallback.accept(success.anomalyId());
    	}
    	case CommandFailure failure ->{
    		setupSelections();
    	}//TODO popup/handle
    	};
    }

    @FXML
    void onSelectSector() {
    	if(anomalyProperty.get() == null) {
    		return;//TODO popup/handle
    	}
    	UUID id = UUID.fromString(anomalyProperty.get().id());
    	String sector = sectorCombo.getSelectionModel().getSelectedItem();
    	CommandResult result = commandService.attachSector(id, sector);
    	
    	switch (result) {
	    	case CommandSuccess success ->{
	    		updateCallback.accept(success.anomalyId());
	    	}
	    	case CommandFailure failure ->{
	    		setupSelections();
	    	}//TODO popup/handle
    	};
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
    
    public void initController(ObjectProperty<AnomalyDto> anomalyProperty, AnomalyCommandService commandService, Consumer<UUID> updateCallback) {
    	this.anomalyProperty = anomalyProperty;
    	this.commandService = commandService;
    	this.updateCallback = updateCallback;
    	
    	bindTransitionButton();
    	applyFilterOnTextField(impactedQuantityTextField);
    	applyFilterOnTextField(productionOrderTextField);
    	setupBoxes();
    	setupSelections();
    }
    
    private void setupBoxes() {
		EnumSet<Sector> sectors = EnumSet.allOf(Sector.class);
		List<String> list = sectors.stream()
			    .map(Enum::name)
			    .toList();
		sectorCombo.setItems(FXCollections.observableArrayList(list));
		
		EnumSet<Machine> machine = EnumSet.allOf(Machine.class);
		List<String> listMachine = machine.stream()
			    .map(Enum::name)
			    .toList();
		machineCombo.setItems(FXCollections.observableArrayList(listMachine));
	}
    
    private void setupTextFields() {
    	if(anomalyProperty.get() == null) {
    		return;
    	}
    	String correctiveAction = anomalyProperty.get().correctiveActionId();
    	if(correctiveAction != null && !(correctiveAction.isBlank())) {
    		correctiveActionTextField.setText(correctiveAction);
    	}
    	
    	String description = anomalyProperty.get().description();
    	if(description != null && !(description.isBlank())) {
    		descriptionTextArea.setText(description);
    	}

    	Integer ProductionOrder = anomalyProperty.get().productionOrder();
    	if(ProductionOrder != null && ProductionOrder > 0) {
    		productionOrderTextField.setText(ProductionOrder.toString());
    	}
    	
    	Integer impactedQuantity = anomalyProperty.get().impactedQuantity();
    	if(impactedQuantity != null && impactedQuantity >= 0) {
    		impactedQuantityTextField.setText(impactedQuantity.toString());
    	}
    }
    
    private void applyFilterOnTextField(TextField text) {
		TextFormatter<String> format = new TextFormatter<>(i->{
			if(i.getControlNewText().matches("[0-9]*")) {
				return i;
			}else {
				return null;
			}
		});
		text.setTextFormatter(format);
	}
    
    private void preselectComboBoxes() {
    	if(anomalyProperty.get() == null) {
    		return;
    	}
    	
    	if(anomalyProperty.get().machine() != null) {
    		machineCombo.setValue(anomalyProperty.get().machine());   		
    	}
    	
    	if(anomalyProperty.get().sector() != null) {
    		sectorCombo.setValue(anomalyProperty.get().sector());    		
    	}
    }
    
    private void preselectQualityDecision() {
    	if(anomalyProperty.get() != null && anomalyProperty.get().qualityDecision() != null) {
    		switch(anomalyProperty.get().qualityDecision()) {
    		case "REPAIR" -> repairButton.setSelected(true);
    		case "SCRAP" -> scrapButton.setSelected(true);
    		case "NA" -> naButton.setSelected(true);
    		default ->{}
    		}
    	}
    }
    
    private void bindTransitionButton() {
    	correctAnomalyButton.disableProperty().bind(
    			correctiveActionTextField.textProperty().isEmpty().or(
    			impactedQuantityTextField.textProperty().isEmpty().or(
    			productionOrderTextField.textProperty().isEmpty().or(
    			descriptionTextArea.textProperty().isEmpty().or(
    			machineCombo.selectionModelProperty().isNull().or(
    			sectorCombo.selectionModelProperty().isNull().or(
    			qualityDecisionGroup.selectedToggleProperty().isNull()))))))
    			);
    }
    
    private void setupSelections() {
    	setupTextFields();
    	preselectComboBoxes();
    	preselectQualityDecision();
    }
}
