package Application;

import Model.SqliteDatabase.SQLiteDatabase;
import Model.SqliteDatabase.SqliteDatabaseBookmarks;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage currentStage;
    public static SQLiteDatabase webHistoryDB;
    public static SqliteDatabaseBookmarks bookmarksDB;

    @Override
    public void start(Stage primaryStage) throws Exception{
        //region connect to mysql
        /*try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Where is your MySQL JDBC Driver?");
            e.printStackTrace();
            return;
        }*/
        webHistoryDB = new SQLiteDatabase();
        webHistoryDB.ConnectToDatabase();
        bookmarksDB = new SqliteDatabaseBookmarks();
        bookmarksDB.ConnectToDatabase();
        //boolean TableExists = webHistoryDB.CreateTableIfUnexists();
        //endregion
        Parent root = FXMLLoader.load(getClass().getResource("/View/tabpane.fxml"));
        primaryStage.setTitle("Web Browser");
        primaryStage.setScene(new Scene(root, 1200, 600));
        primaryStage.getIcons().add(new Image("../resources/Drawable/three_leaf_clover.png"));
        currentStage = primaryStage;
        primaryStage.show();
    }


    public static void main(String[] args) {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        launch(args);
    }


    public static Stage getStage()
    {
        return currentStage;
    }

}
