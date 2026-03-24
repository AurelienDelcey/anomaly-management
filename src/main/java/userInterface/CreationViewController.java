package userInterface;

import java.util.EnumSet;
import java.util.List;

import application.command.AnomalyCommandService;
import application.command.CommandFailure;
import application.command.CommandResult;
import application.command.CommandSuccess;
import domain.valueobject.Machine;
import domain.valueobject.Sector;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CreationViewController {
	
	@FXML private Button createButton;
	@FXML private Button createAndOpenButton;
	@FXML private Button cancelButton;
	@FXML private TextField productionOrderField;
	@FXML private TextField quantityField;
	@FXML private TextField descriptionField;
	@FXML private ComboBox<String> sectorBox;
	@FXML private ComboBox<String> machineBox;
	@FXML private VBox root;
	
	private AnomalyCommandService commandService;
	
	@FXML
	public void initialize() {
		setupBoxes();
		bindCreateButtons(createButton);
		bindCreateButtons(createAndOpenButton);
		applyFilterOnTextField(quantityField);
		applyFilterOnTextField(productionOrderField);
	}
	
	@FXML
	public void onClickCreate() {
		CommandResult result = commandService.createAnomaly(descriptionField.getText(), 
									 sectorBox.getValue(), 
									 Integer.valueOf(quantityField.getText()), 
									 Integer.valueOf(productionOrderField.getText()), 
									 machineBox.getValue());
		switch(result) {
			case CommandSuccess success ->{
				//TODO return ID
				onClickCancel();
			}
			case CommandFailure failure ->{
				//TODO popup error message
			}
		}
	}
	
	@FXML
	public void onClickCreateAndOpen() {
		CommandResult result = commandService.createAnomaly(descriptionField.getText(), 
				 sectorBox.getValue(), 
				 Integer.valueOf(quantityField.getText()), 
				 Integer.valueOf(productionOrderField.getText()), 
				 machineBox.getValue());
		switch(result) {
			case CommandSuccess success ->{
				onClickCancel();//TODO go to detail view.
			}
			case CommandFailure failure ->{
				//TODO popup error message
			}
		}
	}
	
	@FXML
	public void onClickCancel() {
		Stage stage = (Stage) root.getScene().getWindow();
		stage.close();
	}
	
	public void initController(AnomalyCommandService commandService) {
		this.commandService = commandService;
	}
	
	private void setupBoxes() {
		EnumSet<Sector> sectors = EnumSet.allOf(Sector.class);
		List<String> list = sectors.stream()
			    .map(Enum::name)
			    .toList();
		sectorBox.setItems(FXCollections.observableArrayList(list));
		
		EnumSet<Machine> machine = EnumSet.allOf(Machine.class);
		List<String> listMachine = machine.stream()
			    .map(Enum::name)
			    .toList();
		machineBox.setItems(FXCollections.observableArrayList(listMachine));
	}
	
	private void bindCreateButtons(Button button) {
		button.disableProperty().bind(productionOrderField.textProperty().isEmpty().or(
				quantityField.textProperty().isEmpty()).or(
				descriptionField.textProperty().isEmpty()).or(
				sectorBox.valueProperty().isNull().or(
				machineBox.valueProperty().isNull())));
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
}
