package userInterface.detail;

import java.util.List;
import java.util.function.Consumer;

import application.dto.AnomalyDto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class HistoryViewController {

    @FXML private ListView<AnomalyDto> anomalyListView;
    
    private Consumer<AnomalyDto> openDetailsCallback;
    
    private final ObservableList<AnomalyDto> items = FXCollections.observableArrayList();
    
    public void initController(List<AnomalyDto> anomalyList, Consumer<AnomalyDto> openDetailsCallback) {
    	this.openDetailsCallback = openDetailsCallback;
    	items.setAll(anomalyList);
    	anomalyListView.setItems(items);
    	anomalyListView.setCellFactory(lv -> new ListCell<>() {
    	    @Override
    	    protected void updateItem(AnomalyDto item, boolean empty) {
    	        super.updateItem(item, empty);
    	        if (empty || item == null) {
    	            setText(null);
    	        } else {
    	            setText(item.businessId()+"\n"+item.anomalyState());
    	        }
    	    }
    	});
    	setDoubleClick();
    	setEntrePressed();
    }
    
    @FXML
    void onClickOpen() {
    	AnomalyDto selected = anomalyListView.getSelectionModel().getSelectedItem();
    	if(selected != null) {
    	    openDetailsCallback.accept(selected);
    	    Stage stage = (Stage) anomalyListView.getScene().getWindow();
    	    stage.close();
    	}
    }
    
    private void setDoubleClick() {
    	this.anomalyListView.setOnMouseClicked(e->{
    		if(e.getClickCount() == 2) {
    			onClickOpen();
    		}
    	});
    }
    
    private void setEntrePressed() {
    	this.anomalyListView.setOnKeyPressed(event->{
    		switch(event.getCode()) {
    		case ENTER -> onClickOpen();
			default -> {}
    		};
    	});
    }
}
