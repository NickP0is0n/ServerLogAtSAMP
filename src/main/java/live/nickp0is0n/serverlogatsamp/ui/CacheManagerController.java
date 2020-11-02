package live.nickp0is0n.serverlogatsamp.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import live.nickp0is0n.serverlogatsamp.models.Log;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    }

    public void setMainController(Controller mainController) {
        this.mainController = mainController;
    }
}
