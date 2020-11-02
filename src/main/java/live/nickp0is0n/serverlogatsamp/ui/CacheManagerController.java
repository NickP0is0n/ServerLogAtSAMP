package live.nickp0is0n.serverlogatsamp.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import live.nickp0is0n.serverlogatsamp.models.Log;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CacheManagerController {

    @FXML
    private ListView<String> cacheListView;

    @FXML
    private TextField newNameTextField;

    private Controller mainController;

    @FXML
    void initialize() {
        File cacheDirectory = new File("downloadedLogs");
        ArrayList<File> cacheFiles = new ArrayList<>(Arrays.asList(cacheDirectory.listFiles()));
        List<String> cacheFileNames = new ArrayList<String>();
        cacheFiles.forEach((it) -> cacheFileNames.add(FilenameUtils.removeExtension(it.getName())));
        cacheListView.setItems(FXCollections.observableArrayList(cacheFileNames));
    }

    @FXML
    void onChangeNameButtonClick(ActionEvent event) {
        String selectedItem = null;
        if((selectedItem = cacheListView.getSelectionModel().getSelectedItem()) != null) {
            File selectedLogFile = new File("downloadedLogs/" + selectedItem + ".txt");
            File renamedLogFile = new File("downloadedLogs/" + newNameTextField.getText() + ".txt");
            selectedLogFile.renameTo(renamedLogFile);
            cacheListView.getItems().set(cacheListView.getSelectionModel().getSelectedIndex(), newNameTextField.getText());
        }
    }

    @FXML
    void onLoadButtonClick(ActionEvent event) {
        final String selectedItem;
        if((selectedItem = cacheListView.getSelectionModel().getSelectedItem()) != null) {
            mainController.setProgressBarState(ProgressBarState.ENABLED, "Загрузка логов из кэша");
            new Thread(() -> {
                try {
                    mainController.setServerLog(new Log(new File("downloadedLogs/" + selectedItem + ".txt")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> {
                    mainController.updateLogView(mainController.getServerLog());
                    mainController.setProgressBarState(ProgressBarState.DISABLED, "");
                });
            }).start();
        }
    }

    @FXML
    void onRemoveButtonClick(ActionEvent event) {
        String selectedItem = null;
        if((selectedItem = cacheListView.getSelectionModel().getSelectedItem()) != null) {
            Alert removalConfirmationAlert = new Alert(Alert.AlertType.CONFIRMATION, "Вы действительно хотите удалить файл логов \"" + selectedItem + "\"? Это действие не может быть отменено.");
            Optional<ButtonType> buttonPressed = removalConfirmationAlert.showAndWait();
            if (buttonPressed.isPresent() && buttonPressed.get().equals(ButtonType.OK)) {
                cacheListView.getItems().remove(selectedItem);
                File selectedLogFile = new File("downloadedLogs/" + selectedItem + ".txt");
                selectedLogFile.delete();
            }
        }
    }

    public void setMainController(Controller mainController) {
        this.mainController = mainController;
    }
}
