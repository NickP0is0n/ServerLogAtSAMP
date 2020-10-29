package live.nickp0is0n.serverlogatsamp.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import live.nickp0is0n.serverlogatsamp.models.FTPAccount;
import live.nickp0is0n.serverlogatsamp.models.Log;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.util.Date;

public class Controller {

    @FXML
    private TextField ftpLoginTextField;

    @FXML
    private TextField ftpPasswordTextField;

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

    private Log serverLog;

    @FXML
    void initialize() throws IOException, ClassNotFoundException {
        loadFtpAccountData();
    }

    @FXML
    void onAboutMenuItemClick(ActionEvent event) {

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
            progressBar.setVisible(true);
            progressBarDescription.setText("Загрузка логов из файла");
            progressBarDescription.setVisible(true);
            new Thread(() -> {
                try {
                    serverLog = new Log(logFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> {
                    updateLogView(this.serverLog);
                    progressBar.setVisible(false);
                    progressBarDescription.setVisible(false);
                });
            }).start();
        }
    }

    @FXML
    void onReceiveLogsButtonClick(ActionEvent event) throws IOException {
        saveFtpAccountData();
        FTPClient ftpClient = new FTPClient();
        new Thread(() -> {
            try {
                Platform.runLater(() -> {
                    progressBar.setVisible(true);
                    progressBarDescription.setText("Подключение к FTP серверу");
                    progressBarDescription.setVisible(true);
                });
                ftpClient.connect(ftpServerTextField.getText());
                if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) throw new Exception("Не удалось подключится к FTP серверу.");
                System.out.println(ftpClient.getReplyString());

                Platform.runLater(() -> {
                    progressBar.setVisible(true);
                    progressBarDescription.setText("Авторизация на FTP сервере");
                    progressBarDescription.setVisible(true);
                });
                ftpClient.login(ftpLoginTextField.getText(), ftpPasswordTextField.getText());
                if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) throw new Exception("Неправильный логин или пароль.");
                System.out.println(ftpClient.getReplyString());
                ftpClient.enterLocalPassiveMode();
                System.out.println(ftpClient.getReplyString());

                Platform.runLater(() -> {
                    progressBar.setVisible(true);
                    progressBarDescription.setText("Загрузка файла логов");
                    progressBarDescription.setVisible(true);
                });
                File logFile = new File("downloadedLogs/" + new Date().toString() + ".txt");
                if (!logFile.exists()) logFile.createNewFile();
                ftpClient.retrieveFile("server_log.txt", new FileOutputStream(logFile));
                System.out.println(ftpClient.getReplyString());

                ftpClient.disconnect();
                System.out.println(ftpClient.getReplyString());

                serverLog = new Log(logFile);
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    progressBarDescription.setVisible(false);
                    updateLogView(serverLog);
                });
            } catch (Exception e) {
                try {
                    ftpClient.disconnect();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                e.printStackTrace();
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    progressBarDescription.setVisible(false);
                    new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
                });
            }
        }).start();
    }

    private void updateLogView(Log serverLog) {
        logDisplayView.setItems(FXCollections.observableArrayList(serverLog.getLogEntries()));
    }

    private void saveFtpAccountData() throws IOException {
        FTPAccount savedAccount = new FTPAccount(ftpServerTextField.getText(),
                ftpLoginTextField.getText(),
                ftpPasswordTextField.getText());
        File saveData = new File("savedata");
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
