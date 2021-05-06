package live.nickp0is0n.serverlogatsamp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        ClassLoader loader = getClass().getClassLoader();
        Parent root = FXMLLoader.load(loader.getResource("serverlog.fxml"));
        primaryStage.setTitle("ServerLog@SAMP");
        Scene mainScene = new Scene(root, 740, 400);
        mainScene.getStylesheets().add(loader.getResource("styles.css").toExternalForm());
        primaryStage.setScene(mainScene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
