package userInterface;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import application.command.AnomalyCommandService;
import application.dto.AnomalyDto;
import application.query.AnomalyQueryService;
import application.query.QueryFailure;
import application.query.QueryNotFound;
import application.query.QueryResult;
import application.query.QuerySuccess;
import domain.anomaly.AnomalyState;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class DetailViewLayoutController {
	
	@FXML private Label idLabel;
	@FXML private Label stateLabel;
	@FXML private Label createdAtLabel;
	@FXML private Label createdByLabel;
	@FXML private Button previousAnomalyButton;
	@FXML private Button nextAnomalyButton;
	@FXML private Button historyButton;
	@FXML private BorderPane root;
	
	private AnomalyCommandService commandService;
	private AnomalyQueryService queryService;
	
	private final ObjectProperty<AnomalyDto> anomalyProperty = new SimpleObjectProperty<>();
	private final  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	
	@FXML
	public void initialize() {
		
	}
	
	@FXML
	public void onClickPrevious() {
		if(anomalyProperty.get() == null) {
			return;
		}
		AnomalyDto newAnomaly = getAnomaly(UUID.fromString(anomalyProperty.get().parentId()));
		anomalyProperty.set(newAnomaly);
	}
	
	@FXML
	public void onClickNext() {
		if(anomalyProperty.get() == null) {
			return;
		}
		AnomalyDto newAnomaly = getAnomaly(UUID.fromString(anomalyProperty.get().childId()));
		anomalyProperty.set(newAnomaly);
	}
	
	@FXML
	public void onClickHistory() {
		//load history in modal window
	}
	
	public void initController(AnomalyDto anomaly, AnomalyCommandService commandService, AnomalyQueryService queryService) {
		this.commandService = commandService;
		this.queryService = queryService;
		this.anomalyProperty.addListener((obs, old, newAnomaly) -> {
				if (newAnomaly != null) {
				    anomalyProperty.set(newAnomaly);
				}
		    });
		this.anomalyProperty.set(anomaly);
		bindHeader();
		bindButtons();
	}
	
	public void bindHeader() {
		idLabel.textProperty().bind(Bindings.createStringBinding(
				()-> anomalyProperty.get() ==null ? "" : anomalyProperty.get().businessId(), anomalyProperty
				));
		stateLabel.textProperty().bind(Bindings.createStringBinding(
				()-> anomalyProperty.get() ==null ? "" : anomalyProperty.get().anomalyState(), anomalyProperty
				));
		createdAtLabel.textProperty().bind(Bindings.createStringBinding(
				()-> anomalyProperty.get() ==null ? "" : 
					LocalDateTime.ofInstant(anomalyProperty.get().createdAt(), ZoneId.systemDefault()).format(formatter), anomalyProperty
				));
		createdByLabel.textProperty().bind(Bindings.createStringBinding(
				()-> anomalyProperty.get() ==null ? "" : anomalyProperty.get().createdBy(), anomalyProperty
				));
	}
	
	public void bindButtons() {
		previousAnomalyButton.disableProperty().bind(Bindings.createBooleanBinding(
				()->anomalyProperty.get() == null || anomalyProperty.get().parentId() == null, anomalyProperty
		));
		nextAnomalyButton.disableProperty().bind(Bindings.createBooleanBinding(
				()->anomalyProperty.get() == null || anomalyProperty.get().childId() == null, anomalyProperty
		));
		historyButton.disableProperty().bind(Bindings.createBooleanBinding(
				()-> anomalyProperty.get() == null || (anomalyProperty.get().parentId() == null && anomalyProperty.get().childId() == null), anomalyProperty
		));
	}
	
	private AnomalyDto getAnomaly(UUID id) {
		QueryResult<AnomalyDto> result = queryService.findById(id);
		return switch(result) {
		case QuerySuccess<AnomalyDto> success-> {
			 yield success.payload();
		 }
		 case QueryNotFound<AnomalyDto> notFound-> {yield null;}//TODO popup
		 case QueryFailure<AnomalyDto> failure -> {yield null;}//TODO popup
		};
	}
	
	private void reloadDynamicPanel(AnomalyDto newAnomaly) {
		Map<String, Function<AnomalyDto, Node>> loader = Map.of(
				"PENDING", e->loadPendingPanel(e),
				"CORRECTED", e->loadCorrectedPanel(e),
				"RESOLVED", e->loadResolvedPanel(e),
				"ARCHIVED", e->loadArchivedPanel(e)
				);
		root.setCenter(loader.get(newAnomaly.anomalyState()).apply(newAnomaly));
	}
	
	private Node loadPendingPanel(AnomalyDto anomaly) {
		try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/pendingPanel.fxml"));
	        Node node = loader.load();

	        PendingPanelController controller = loader.getController();
	       // controller.initController(anomaly);

	        return node;

	    } catch (IOException e) {
	        throw new RuntimeException(e);
	    }
	}
	
	private Node loadCorrectedPanel(AnomalyDto anomaly) {
		try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/correctedPanel.fxml"));
	        Node node = loader.load();

	        CorrectedPanelController controller = loader.getController();
	       // controller.initController(anomaly);

	        return node;

	    } catch (IOException e) {
	        throw new RuntimeException(e);
	    }
	}
	
	private Node loadResolvedPanel(AnomalyDto anomaly) {
		try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/resolvedPanel.fxml"));
	        Node node = loader.load();

	        ResolvedPanelController controller = loader.getController();
	       // controller.initController(anomaly);

	        return node;

	    } catch (IOException e) {
	        throw new RuntimeException(e);
	    }
	}
	
	private Node loadArchivedPanel(AnomalyDto anomaly) {
		try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/archivedPanel.fxml"));
	        Node node = loader.load();

	        ArchivedPanelController controller = loader.getController();
	       // controller.initController(anomaly);

	        return node;

	    } catch (IOException e) {
	        throw new RuntimeException(e);
	    }
	}
}
