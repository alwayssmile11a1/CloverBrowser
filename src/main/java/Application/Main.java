package Application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/View/homepage.fxml"));
        //Parent root = FXMLLoader.load(getClass().getResource("/View/History.fxml"));
        primaryStage.setTitle("Web Browser");
        primaryStage.setScene(new Scene(root, 1200, 600));
        //primaryStage.setMaximized(true);
        //primaryStage.initStyle(StageStyle.UNDECORATED);

        primaryStage.show();
    }


    public static void main(String[] args) {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        launch(args);
    }
}
