package userInterface;

import java.util.EnumSet;
import java.util.List;

import application.command.AnomalyCommandService;
import domain.valueobject.Machine;
import domain.valueobject.Sector;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class CreationViewController {
	
	@FXML private Button createButton;
	@FXML private Button createAndOpenButton;
	@FXML private Button cancelButton;
	@FXML private TextField productionOrderField;
	@FXML private TextField quantityField;
	@FXML private TextField descriptionField;
	@FXML private ComboBox<String> sectorBox;
	@FXML private ComboBox<String> machineBox;
	
	private AnomalyCommandService commandService;
	
	@FXML
	public void initialize() {
		
		
	}
	
	@FXML
	public void onClickCreate() {
		
	}
	
	@FXML
	public void onClickCreateAndOpen() {
		
	}
	
	@FXML
	public void onClickCancel() {
		
	}
	
	public void initController(AnomalyCommandService commandService) {
		this.commandService = commandService;
	}
	
}
