package userInterface;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

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
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
	public void onClickCreate() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/creationView.fxml"));
		Parent view = loader.load();
			
		CreationViewController controller = loader.getController();
		
		controller.initController(commandService, (e)->updateCommand(e));
		
		Scene scene = new Scene(view);
		Stage stage = new Stage();
		stage.setScene(scene);
		
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(createButton.getScene().getWindow());
		
		stage.setMinWidth(406);
		stage.setMinHeight(563);
		stage.setResizable(false);
		stage.showAndWait();
		
	}
	
	@FXML
	public void onClickRefresh() {
		loadData();
	}
	
	@FXML
	public void onClickDetails() {
		AnomalyDto anomaly = anomalyTable.getSelectionModel().getSelectedItem();
		
		if (anomaly == null) {
			return;
		}
		
		//changement de vue, on passe l'anomaly a detailler en parametre... ou on injecte apres coup.
		
		/*FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/detailView.fxml"));
		Parent view = loader.load();
			
		DetailController controller = loader.getController();
		controller.setup(anomaly, queryService, commandService);*/
	}
	
	public void setupServiceAndLoad(AnomalyQueryService queryService, AnomalyCommandService commandService) {
		this.queryService = queryService;
		this.commandService = commandService;
		
		loadData();
	}
	
	public void loadData() {
		 QueryResult<List<AnomalyDto>> result = queryService.findPage(1);
		 switch (result) {
		 case QuerySuccess<List<AnomalyDto>> success-> {
			 items.setAll(success.payload());
		 }
		 case QueryNotFound<List<AnomalyDto>> notFound-> {}//TODO nothing, empty list here
		 case QueryFailure <List<AnomalyDto>> failure -> {}//TODO error popup
		 };
	}
	
	private void bindTableColumns() {
		idColumn.setCellValueFactory(cell ->
        new SimpleStringProperty(cell.getValue().businessId()));
		
		sectorColumn.setCellValueFactory(cell ->
        new SimpleStringProperty(cell.getValue().sector()));
		
		descriptionColumn.setCellValueFactory(cell ->
        new SimpleStringProperty(cell.getValue().description()));
		
		stateColumn.setCellValueFactory(cell ->
        new SimpleStringProperty(cell.getValue().anomalyState()));
		
		createdAtColumn.setCellValueFactory(cell ->
        new SimpleStringProperty(LocalDateTime.ofInstant(cell.getValue().createdAt(), ZoneId.systemDefault()).format(formatter)));
		
		createdByColumn.setCellValueFactory(cell ->
        new SimpleStringProperty(cell.getValue().createdBy()));
	}
	
	private void updateCommand(UUID anomalyId)  {
		QueryResult<AnomalyDto> result = queryService.findById(anomalyId);
		
		switch (result) {
		 case QuerySuccess<AnomalyDto> success-> {
			 AnomalyDto anomaly = success.payload();
			 int index = items.indexOf(anomaly);
			 if(index >= 0) {
				 items.set(index, anomaly);
			 }else {
				 items.add(0,anomaly);
			 }
		 }
		 case QueryNotFound<AnomalyDto> notFound-> {}//TODO popup
		 case QueryFailure <AnomalyDto> failure -> {}//TODO error message
		 };
	}
	
}
