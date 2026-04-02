package userInterface;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import application.dto.AnomalyDto;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class TraceabilityViewController {

    @FXML private Label archivedLabel;
    @FXML private Label correctedLabel;
    @FXML private Label createdLabel;
    @FXML private Label resolvedLabel;
    @FXML private Button cancelButton;
    
    private ObjectProperty<AnomalyDto> anomalyProperty;
    private final  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    public void initController(ObjectProperty<AnomalyDto> anomalyProperty) {
    	this.anomalyProperty = anomalyProperty;
    	initLabels();
    }

	private void initLabels() {
		createdLabel.setText("At " + formatDate(anomalyProperty.get().createdAt()) + " By " + 
				anomalyProperty.get().createdBy());
		
		if(anomalyProperty.get().correctedBy() != null && anomalyProperty.get().correctedAt() != null) {
			correctedLabel.setText("At " + formatDate(anomalyProperty.get().correctedAt()) + " By " + 
					anomalyProperty.get().correctedBy());
		}else {
			correctedLabel.setText("Anomaly is not yet corrected.");
		}
		
		if(anomalyProperty.get().resolvedBy() != null && anomalyProperty.get().resolvedAt() != null) {
			resolvedLabel.setText("At " + formatDate(anomalyProperty.get().resolvedAt()) + " By " + 
					anomalyProperty.get().resolvedBy());
		}else {
			resolvedLabel.setText("Anomaly is not yet resolved.");
		}
		
		if(anomalyProperty.get().archivedBy() != null && anomalyProperty.get().archivedAt() != null) {
			archivedLabel.setText("At " + formatDate(anomalyProperty.get().archivedAt()) + " By " + 
					anomalyProperty.get().archivedBy());
		}else {
			archivedLabel.setText("Anomaly is not yet archived.");
		}
	}
	
	@FXML
    void onClickCancel() {
		Stage stage = (Stage) cancelButton.getScene().getWindow();
		stage.close();
    }
	
	private String formatDate(Instant instant) {
		return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(formatter);
	}
}
