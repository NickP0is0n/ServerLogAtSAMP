package live.nickp0is0n.serverlogatsamp.ui;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class AboutController {

    @FXML
    private AnchorPane appPane;

    @FXML
    void onCloseForm(MouseEvent event) {
        ((Stage)appPane.getScene().getWindow()).close();
    }

}