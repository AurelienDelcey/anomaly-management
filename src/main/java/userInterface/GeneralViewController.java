package userInterface;

import java.util.List;

import application.command.AnomalyCommandService;
import application.command.CommandFailure;
import application.command.CommandResult;
import application.command.CommandSuccess;
import application.dto.AnomalyDto;
import application.query.AnomalyQueryService;
import application.query.QueryFailure;
import application.query.QueryNotFound;
import application.query.QueryResult;
import application.query.QuerySuccess;
import domain.exception.InconsistentAnomalyStateException;
import domain.valueobject.Sector;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;

public class GeneralViewController {
	
	@FXML private TableView<AnomalyDto> anomalyTable;
	@FXML private Button createButton;
	@FXML private Button refreshButton;
	@FXML private Button detailsButton;
	@FXML private ComboBox<String> sortCombo;
	@FXML private CheckBox hideArchivedCheckBox;
	
	private final ObservableList<AnomalyDto> items = FXCollections.observableArrayList();
	
	private AnomalyQueryService queryService;
	private AnomalyCommandService commandService;
	
	@FXML
	public void initialize() {
	
	}
	
	@FXML
	public void onClickCreate() {
		
	}
	
	@FXML
	public void onClickRefresh() {
	}
	
	@FXML
	public void onClickDetails() {
		
	}
	
	public void setupServiceAndLoad() {
		
	}
}
