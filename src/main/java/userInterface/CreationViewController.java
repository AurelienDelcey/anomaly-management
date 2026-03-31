package userInterface;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import application.command.AnomalyCommandService;
import application.command.CommandFailure;
import application.command.CommandResult;
import application.command.CommandSuccess;
import domain.valueobject.Machine;
import domain.valueobject.Sector;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CreationViewController {
	
	@FXML private Button createButton;
	@FXML private Button createAndOpenButton;
	@FXML private Button cancelButton;
	@FXML private TextField productionOrderField;
	@FXML private TextField quantityField;
	@FXML private TextArea descriptionField;
	@FXML private ComboBox<String> sectorBox;
	@FXML private ComboBox<String> machineBox;
	@FXML private VBox root;
	
	private AnomalyCommandService commandService;
	private Consumer<UUID> updateCallback;
	private Consumer<UUID> updateAndOpenCallback;
	
	@FXML
	public void initialize() {
		setupBoxes();
		bindCreateButtons(createButton);
		bindCreateButtons(createAndOpenButton);
		applyFilterOnTextField(quantityField);
		applyFilterOnTextField(productionOrderField);
	}
	
	public void initController(AnomalyCommandService commandService, Consumer<UUID> updateCallback, Consumer<UUID> updateAndOpen) {
		this.commandService = commandService;
		this.updateCallback = updateCallback;
		this.updateAndOpenCallback = updateAndOpen;
	}

	@FXML
	public void onClickCreate() throws IOException {
		CommandResult result = commandService.createAnomaly(descriptionField.getText(), 
									 sectorBox.getValue(), 
									 Integer.valueOf(quantityField.getText()), 
									 Integer.valueOf(productionOrderField.getText()), 
									 machineBox.getValue());
		switch(result) {
			case CommandSuccess success ->{
				updateCallback.accept(success.anomalyId());
				onClickCancel();
			}
			case CommandFailure failure ->{
				showError(failure.message());
			}
		}
	}
	
	@FXML
	public void onClickCreateAndOpen() throws IOException {
		CommandResult result = commandService.createAnomaly(descriptionField.getText(), 
				 sectorBox.getValue(), 
				 Integer.valueOf(quantityField.getText()), 
				 Integer.valueOf(productionOrderField.getText()), 
				 machineBox.getValue());
		switch(result) {
			case CommandSuccess success ->{
				updateAndOpenCallback.accept(success.anomalyId());
				onClickCancel();
			}
			case CommandFailure failure ->{
				showError(failure.message());
			}
		}
	}
	
	@FXML
	public void onClickCancel() {
		Stage stage = (Stage) root.getScene().getWindow();
		stage.close();
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
	
	private void showError(String message) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/errorView.fxml"));
		Parent view = loader.load();
			
		ErrorViewController controller = loader.getController();
		controller.initController(message);
		
		Scene scene = new Scene(view);
		Stage stage = new Stage();
		stage.setScene(scene);
		
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(createButton.getScene().getWindow());
		
		stage.showAndWait();
	}
}
