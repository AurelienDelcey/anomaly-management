package userInterface;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ErrorViewController {

    @FXML private Button cancelButton;
    @FXML private Label errorLabel;
    @FXML private VBox root;
    
    @FXML
    void onClickCancel() {
    	Stage stage = (Stage) root.getScene().getWindow();
		stage.close();
    }
    
    public void initController(String message) {
    	this.errorLabel.setText(message);
    }
}
