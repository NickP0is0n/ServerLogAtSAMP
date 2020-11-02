package live.nickp0is0n.serverlogatsamp.ui;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CacheManagerController {

    @FXML
    private ListView<String> cacheListView;

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

    }

    @FXML
    void onRemoveButtonClick(ActionEvent event) {

    }

}
