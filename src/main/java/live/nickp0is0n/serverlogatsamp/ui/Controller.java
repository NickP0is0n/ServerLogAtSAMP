package live.nickp0is0n.serverlogatsamp.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import live.nickp0is0n.serverlogatsamp.models.FTPAccount;
import live.nickp0is0n.serverlogatsamp.models.Log;
import live.nickp0is0n.serverlogatsamp.network.FTPDownloader;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

public class Controller {

    @FXML
    private TextField ftpLoginTextField;

    @FXML
    private PasswordField ftpPasswordTextField;

    @FXML
    private TextField ftpServerTextField;

    @FXML
    private TextField filterTagTextField;

    @FXML
    private TextField filterTimeTextField;

    @FXML
    private TextField filterContainsTextField;

    @FXML
    private ListView<String> logDisplayView;

    @FXML
    private ProgressIndicator progressBar;

    @FXML
    private Text progressBarDescription;

    public void setServerLog(Log serverLog) {
        this.serverLog = serverLog;
    }

    public Log getServerLog() {
        return serverLog;
    }

    private Log serverLog;
    private final File saveData = new File("savedata");

    @FXML
    void initialize() throws IOException, ClassNotFoundException {
        File cacheDirectory = new File("downloadedLogs");
        if (!cacheDirectory.exists()) cacheDirectory.mkdir();
        loadFtpAccountData();
    }

    @FXML
    void onAboutMenuItemClick(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("about.fxml"));
        Parent root = loader.load();
        Stage aboutStage = new Stage();
        aboutStage.setTitle("О ServerLog@SAMP");
        aboutStage.setScene(new Scene(root, 600, 400));
        aboutStage.initStyle(StageStyle.UNDECORATED);
        aboutStage.show();
    }

    @FXML
    void onClearSaveDataMenuItemClick(ActionEvent event) {
        if (saveData.exists()) saveData.delete();
    }

    @FXML
    void onExitMenuItemClick(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    void onFilterButtonClick(ActionEvent event) {
        if (serverLog == null) {
            new Alert(Alert.AlertType.ERROR, "Файл логов не загружен!")
                    .showAndWait();
        }
        Log filteredLog = serverLog;
        if (!filterTagTextField.getText().isEmpty()) {
            filteredLog = filteredLog.filterByTag(filterTagTextField.getText());
        }
        if (!filterTimeTextField.getText().isEmpty()) {
            filteredLog = filteredLog.filterByTime(filterTimeTextField.getText());
        }
        if (!filterContainsTextField.getText().isEmpty()) {
            filteredLog = filteredLog.filterByKeyword(filterContainsTextField.getText());
        }
        updateLogView(filteredLog);
    }

    @FXML
    void onOpenLogFileMenuItemClick(ActionEvent event) throws IOException {
        FileChooser chooser = new FileChooser(); //диалог открытия
        chooser.setTitle("Выберите файл логов");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Текстовые файлы (.txt)", "*.txt")); //фильтр файлов
        File logFile = chooser.showOpenDialog(new Stage()); //показ диалога на отдельной сцене
        if(logFile != null) {
            setProgressBarState(ProgressBarState.ENABLED, "Загрузка логов из файла");
            new Thread(() -> {
                try {
                    serverLog = new Log(logFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> {
                    updateLogView(this.serverLog);
                    setProgressBarState(ProgressBarState.DISABLED, "");
                });
            }).start();
        }
    }

    @FXML
    void onReceiveLogsButtonClick(ActionEvent event) throws IOException {
        saveFtpAccountData();
        FTPDownloader downloader = new FTPDownloader(ftpServerTextField.getText(), ftpLoginTextField.getText(), ftpPasswordTextField.getText());
        downloader.setOnFtpConnectionListener(() -> {
            Platform.runLater(() -> {
                setProgressBarState(ProgressBarState.ENABLED, "Подключение к FTP серверу");
            });
        });
        downloader.setOnFtpLoginListener(() -> {
            Platform.runLater(() -> {
                setProgressBarState(ProgressBarState.ENABLED, "Авторизация на FTP сервере");
            });
        });
        downloader.setOnFtpDownloadListener(() -> {
            Platform.runLater(() -> {
                setProgressBarState(ProgressBarState.ENABLED, "Загрузка файла логов");
            });
        });
        new Thread(() -> {
            try {
                File logFile = new File("downloadedLogs/" + new Date().toString() + ".txt");
                downloader.connect();
                downloader.downloadFile("server_log.txt", logFile.getAbsolutePath());
                downloader.close();
                serverLog = new Log(logFile);
                Platform.runLater(() -> {
                    setProgressBarState(ProgressBarState.DISABLED, "");
                    updateLogView(serverLog);
                });
            } catch (Exception e) {
                try {
                   downloader.close();
                } catch (Exception ioException) {
                    ioException.printStackTrace();
                }
                e.printStackTrace();
                Platform.runLater(() -> {
                    setProgressBarState(ProgressBarState.DISABLED, "");
                    new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
                });
            }
        }).start();
    }

    @FXML
    void onCacheManagerMenuItemClick(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("cachemanager.fxml"));
        Parent root = loader.load();
        CacheManagerController controller = loader.getController();
        controller.setMainController(this);
        Stage cacheManagerStage = new Stage();
        cacheManagerStage.setTitle("Менеджер кэша");
        cacheManagerStage.setScene(new Scene(root, 500, 300));
        cacheManagerStage.show();
    }

    @FXML
    void onClearCacheMenuItemClick(ActionEvent event) {
        Alert removalConfirmationAlert = new Alert(Alert.AlertType.CONFIRMATION, "Вы действительно хотите очистить кэш логов? Это действие не может быть отменено.");
        Optional<ButtonType> buttonPressed = removalConfirmationAlert.showAndWait();
        if (buttonPressed.isPresent() && buttonPressed.get().equals(ButtonType.OK)) {
            File cacheDirectory = new File("downloadedLogs");
            ArrayList<File> cacheFiles = new ArrayList(Arrays.asList(cacheDirectory.listFiles()));
            cacheFiles.forEach(File::delete);
        }
    }

    void updateLogView(Log serverLog) {
        logDisplayView.setItems(FXCollections.observableArrayList(serverLog.getLogEntries()));
    }

    void setProgressBarState(ProgressBarState state, String description) {
        switch (state) {
            case ENABLED: {
                progressBar.setVisible(true);
                progressBarDescription.setVisible(true);
                progressBarDescription.setText(description);
                break;
            }
            case DISABLED: {
                progressBar.setVisible(false);
                progressBarDescription.setVisible(false);
                break;
            }
        }
    }

    private void saveFtpAccountData() throws IOException {
        FTPAccount savedAccount = new FTPAccount(ftpServerTextField.getText(),
                ftpLoginTextField.getText(),
                ftpPasswordTextField.getText());
        if (!saveData.exists()) {
            saveData.createNewFile();
        }
        try(ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(saveData))) {
            stream.writeObject(savedAccount);
        }
    }

    private void loadFtpAccountData() throws IOException, ClassNotFoundException {
        File saveData = new File("savedata");
        if (saveData.exists()) {
            try(ObjectInputStream stream = new ObjectInputStream(new FileInputStream(saveData))) {
                FTPAccount savedAccount = (FTPAccount) stream.readObject();
                ftpServerTextField.setText(savedAccount.getServer());
                ftpLoginTextField.setText(savedAccount.getUsername());
                ftpPasswordTextField.setText(savedAccount.getPassword());
            }
        }
    }

}
