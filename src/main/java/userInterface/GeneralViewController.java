package userInterface;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class GeneralViewController {
	
	@FXML private TableView<AnomalyDto> anomalyTable;
	@FXML private Button createButton;
	@FXML private Button refreshButton;
	@FXML private Button detailsButton;
	@FXML private ComboBox<String> sortCombo;
	@FXML private CheckBox hideArchivedCheckBox;
	
	@FXML private TableColumn<AnomalyDto, String> idColumn;
	@FXML private TableColumn<AnomalyDto, String> sectorColumn;
	@FXML private TableColumn<AnomalyDto, String> stateColumn;
	@FXML private TableColumn<AnomalyDto, String> descriptionColumn;
	@FXML private TableColumn<AnomalyDto, String> createdByColumn;
	@FXML private TableColumn<AnomalyDto, String> createdAtColumn;
	
	private final ObservableList<AnomalyDto> items = FXCollections.observableArrayList();
	private final  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	
	private AnomalyQueryService queryService;
	private AnomalyCommandService commandService;
	
	@FXML
	public void initialize() {
		this.anomalyTable.setItems(items);
		bindTableColumns();
	}
	
	@FXML
	public void onClickCreate() {
		CommandResult result = commandService.createAnomaly("coucou", Sector.FORGING);
		
		switch(result) {
		case CommandSuccess success ->{}//TODO refresh using query with payload (UUID new anomaly)
		case CommandFailure failure ->{}//TODO popup error message
		}
	}
	
	@FXML
	public void onClickRefresh() throws InconsistentAnomalyStateException {
		loadData();
	}
	
	@FXML
	public void onClickDetails() {
		
	}
	
	public void setupServiceAndLoad(AnomalyQueryService queryService, AnomalyCommandService commandService) throws InconsistentAnomalyStateException {
		this.queryService = queryService;
		this.commandService = commandService;
		
		loadData();
	}
	
	public void loadData() throws InconsistentAnomalyStateException {//TODO change IconsistentAnomalyStateException to runtime exeption
		 QueryResult<List<AnomalyDto>> result = queryService.findPage(1);
		 switch (result) {
		 case QuerySuccess<List<AnomalyDto>> success-> {
			 items.setAll(success.payload());
		 }
		 case QueryNotFound<List<AnomalyDto>> notFound-> {}//TODO nothing, empty list here
		 case QueryFailure <List<AnomalyDto>> failure -> {}//TODO error popup
		 };
	}
	
	public void bindTableColumns() {
		idColumn.setCellValueFactory(cell ->
        new SimpleStringProperty(cell.getValue().businessId().toString()));
		
		sectorColumn.setCellValueFactory(cell ->
        new SimpleStringProperty(cell.getValue().sector().toString()));
		
		descriptionColumn.setCellValueFactory(cell ->
        new SimpleStringProperty(cell.getValue().description()));
		
		stateColumn.setCellValueFactory(cell ->
        new SimpleStringProperty(cell.getValue().anomalyState().toString()));
		
		createdAtColumn.setCellValueFactory(cell ->
        new SimpleStringProperty(LocalDateTime.ofInstant(cell.getValue().createdAt(), ZoneId.systemDefault()).format(formatter)));
		
		createdByColumn.setCellValueFactory(cell ->
        new SimpleStringProperty(cell.getValue().createdBy().toString()));
	}
}
