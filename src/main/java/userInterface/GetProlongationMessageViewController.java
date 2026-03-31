package userInterface;

import java.util.function.Consumer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class GetProlongationMessageViewController {

    @FXML private Button cancelButton;
    @FXML private TextArea messageArea;
    @FXML private Button submitButton;
    
    private Consumer<String> messageCallback;

    @FXML
    void onClickCancel() {
    	Stage stage = (Stage) cancelButton.getScene().getWindow();
		stage.close();
    }

    @FXML
    void onClickSubmit() {
    	String message = messageArea.getText();
    	messageCallback.accept(message);
    	onClickCancel();
    }
    
    public void initController(Consumer<String> messageCallback) {
    	this.messageCallback = messageCallback;
    }

}
