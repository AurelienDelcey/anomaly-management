package userInterface;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import application.command.AnomalyCommandService;
import application.dto.AnomalyDto;
import application.query.AnomalyQueryService;
import application.query.QueryFailure;
import application.query.QueryNotFound;
import application.query.QueryResult;
import application.query.QuerySuccess;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DetailViewLayoutController {
	
	@FXML private Label idLabel;
	@FXML private Label stateLabel;
	@FXML private Label createdAtLabel;
	@FXML private Label createdByLabel;
	@FXML private Label isProlongationLabel;
	@FXML private Button previousAnomalyButton;
	@FXML private Button nextAnomalyButton;
	@FXML private Button historyButton;
	@FXML private Button prolongationMessageButton;
	@FXML private BorderPane root;
	@FXML private TextArea feedbackBox;
	
	private AnomalyCommandService commandService;
	private AnomalyQueryService queryService;
	private Consumer<AnomalyDto> updateCallback;
	private List<AnomalyDto> historyCache;
	
	private final ObjectProperty<AnomalyDto> anomalyProperty = new SimpleObjectProperty<>();
	private final  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	
	public void initController(AnomalyDto anomaly, AnomalyCommandService commandService, AnomalyQueryService queryService, Consumer<AnomalyDto> updateCallback) {
		this.commandService = commandService;
		this.queryService = queryService;
		this.updateCallback = updateCallback;
		this.anomalyProperty.set(anomaly);
		reloadDynamicPanel(anomaly);
		bindHeader();
		bindButtons();
	}

	@FXML
	public void onClickProlongationMessage() {
		 Stage modal = new Stage();

		    modal.initModality(Modality.APPLICATION_MODAL);
		    modal.setTitle("Message");

		    Label label = new Label();
		    label.setText(anomalyProperty.get().prolongationComent());
		    Button closeButton = new Button("Close");

		    closeButton.setOnAction(e -> modal.close());

		    VBox layout = new VBox(10);
		    layout.getChildren().addAll(label, closeButton);
		    layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

		    Scene scene = new Scene(layout);
		    modal.setScene(scene);

		    modal.showAndWait();
	}
	
	@FXML
	public void onClickPrevious() {
		Optional<AnomalyDto>optAnomaly = getAnomaly(UUID.fromString(anomalyProperty.get().parentId()));
		optAnomaly.ifPresent((newAnomaly)->{
			anomalyProperty.set(newAnomaly);
			reloadDynamicPanel(newAnomaly);
		});
	}
	
	@FXML
	public void onClickNext() {
		Optional<AnomalyDto>optAnomaly = getAnomaly(UUID.fromString(anomalyProperty.get().childId()));
		optAnomaly.ifPresent((newAnomaly)->{
			anomalyProperty.set(newAnomaly);
			reloadDynamicPanel(newAnomaly);
		});
	}
	
	@FXML
	public void onClickHistory() {
		if(this.historyCache == null) {
			QueryResult<List<AnomalyDto>> result = queryService.findHistory(UUID.fromString(anomalyProperty.get().id()));
			switch(result) {
			case QuerySuccess<List<AnomalyDto>> success-> {
				historyCache = new ArrayList<>(success.payload());
				showHistory(success.payload());
				 }
			 case QueryNotFound<List<AnomalyDto>> notFound-> {showFeedback("History not found");}
			 case QueryFailure<List<AnomalyDto>> failure -> {showFeedback(failure.message());}
			};
		}else {
			showHistory(this.historyCache);
		}
	}
	
	private void bindHeader() {
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
		isProlongationLabel.visibleProperty().bind(Bindings.createBooleanBinding(
				()-> anomalyProperty.get() == null ? false : anomalyProperty.get().parentId() != null, anomalyProperty
				));
		prolongationMessageButton.visibleProperty().bind(Bindings.createBooleanBinding(
				()-> anomalyProperty.get() == null ? false : anomalyProperty.get().parentId() != null, anomalyProperty
				));
	}

	private void bindButtons() {
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

	private Optional<AnomalyDto> getAnomaly(UUID id) {
		QueryResult<AnomalyDto> result = queryService.findById(id);
		return switch(result) {
		case QuerySuccess<AnomalyDto> success-> {
			 yield Optional.of(success.payload());
		 }
		 case QueryNotFound<AnomalyDto> notFound-> {
			 showFeedback("Anomaly not found");
			 yield Optional.empty();
			 }
		 case QueryFailure<AnomalyDto> failure -> {
			 showFeedback(failure.message());
			 yield Optional.empty();
			 }
		};
	}
	
	private void reloadDynamicPanel(AnomalyDto newAnomaly) {
		Map<String, Supplier<Node>> loader = Map.of(
				"PENDING", ()->loadPendingPanel(),
				"CORRECTED", ()->loadCorrectedPanel(),
				"RESOLVED", ()->loadResolvedPanel(),
				"ARCHIVED", ()->loadArchivedPanel()
				);
		Supplier<Node> supplier = loader.get(newAnomaly.anomalyState());

		if (supplier == null) {
			showFeedback("Unknow state, impossible to load panel");
		    return;
		}
		
		root.setCenter(supplier.get());
	}
	
	private Node loadPendingPanel() {
		try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/pendingPanel.fxml"));
	        Node node = loader.load();

	        PendingPanelController controller = loader.getController();
	        controller.initController(anomalyProperty, commandService, (e)->loadNewAnomaly(e), (e)->showFeedback(e));

	        return node;

		 } catch (IOException e) {
		        e.printStackTrace();
		        showError("Unexpected error occurred");
		 }
		 return null;
	}
	
	private Node loadCorrectedPanel() {
		try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/correctedPanel.fxml"));
	        Node node = loader.load();

	        CorrectedPanelController controller = loader.getController();
	        controller.initController(anomalyProperty, commandService, (e)->loadNewAnomaly(e), (e)->showFeedback(e));

	        return node;

		 } catch (IOException e) {
		        e.printStackTrace();
		        showError("Unexpected error occurred");
		 }
		 return null;
	}
	
	private Node loadResolvedPanel() {
		try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/resolvedPanel.fxml"));
	        Node node = loader.load();

	        ResolvedPanelController controller = loader.getController();
	        controller.initController(anomalyProperty, commandService, (e)->loadNewAnomaly(e), (e)->showFeedback(e));

	        return node;

		 } catch (IOException e) {
		        e.printStackTrace();
		        showError("Unexpected error occurred");
		 }
		 return null;
	}
	
	private Node loadArchivedPanel() {
		try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/archivedPanel.fxml"));
	        Node node = loader.load();

	        ArchivedPanelController controller = loader.getController();
	        controller.initController(anomalyProperty);

	        return node;

	    } catch (IOException e) {
	        e.printStackTrace();
	        showError("Unexpected error occurred");
	    }
		return null;
	}
	
	private void loadNewAnomaly(UUID id) {
		QueryResult<AnomalyDto> result = queryService.findById(id);
		
		switch (result) {
			 case QuerySuccess<AnomalyDto> success-> {
				 AnomalyDto anomaly = success.payload();
				 updateCallback.accept(anomaly);
				 this.anomalyProperty.set(anomaly);
				 reloadDynamicPanel(anomaly);
				 updateHistoryCache(anomaly);
			 }
			 case QueryNotFound<AnomalyDto> notFound-> {showFeedback("Anomaly not found");}
			 case QueryFailure <AnomalyDto> failure -> {showFeedback(failure.message());}
		 };
	}
	
	private void updateHistoryCache(AnomalyDto anomaly) {
		if(this.historyCache == null) {
			return;
		}
		int index = this.historyCache.indexOf(anomaly);
		if(index >= 0) {
			 this.historyCache.set(index, anomaly);
			 return;
		 }
		
		if (anomaly.parentId() == null) {
			return;
		}
		if(findParentInCache(anomaly.parentId())){
			this.historyCache.add(anomaly);
			return;
		}
		
	}
	
	private boolean findParentInCache(String id) {
		if(this.historyCache == null) {
			return false;
		}
		for(AnomalyDto dto : this.historyCache) {
			if(Objects.equals(dto.id(),id)){
				return true;
			}
		}
		return false;
	}

	private void showHistory(List<AnomalyDto> list) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/historyView.fxml"));
			Parent view = loader.load();
			HistoryViewController controller = loader.getController();
			controller.initController(list, (e)->{
				anomalyProperty.set(e);
				reloadDynamicPanel(e);
			});
			Scene scene = new Scene(view);
			Stage stage = new Stage();
			stage.setScene(scene);
			
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.initOwner(root.getScene().getWindow());
			
			stage.setMinWidth(70);
			stage.setMinHeight(400);
			
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
			showError("Unexpected error occurred");
		}
	}
	
	private void showFeedback(String message) {
		feedbackBox.setText(message);
		if(message != null && message.contains("Success")) {
			feedbackBox.setStyle("-fx-text-fill: green;");
		}else {
			feedbackBox.setStyle("-fx-text-fill: red;");
		}
	}
	
	private void showError(String message) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/errorView.fxml"));
			Parent view = loader.load();
			ErrorViewController controller = loader.getController();
			controller.initController(message);
			
			Scene scene = new Scene(view);
			Stage stage = new Stage();
			stage.setScene(scene);
			
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.initOwner(root.getScene().getWindow());
			
			stage.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
			
		}	
	}
}
