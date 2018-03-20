package Controller;

import Model.MySqlDatabase.MySqlDatabase;
import Model.ReferencableInterface.IReferencable;
import Model.ReferencableInterface.ReferencableManager;
import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import com.sun.istack.internal.NotNull;
import javafx.animation.PathTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Callback;
import javafx.util.Duration;

import javax.swing.plaf.nimbus.State;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class HistoryController implements Initializable, IReferencable {
    public static final String FXMLPATH = "/View/History.fxml";

    @FXML
    private JFXTreeTableView tbvHistory;

    @FXML
    private GridPane actionBar;

    @FXML
    private Button cancelButton;

    @FXML
    private Label lblNumberSelectedItems;

    @FXML
    private Button gotoWebsiteButton;

    JFXTreeTableColumn<HistoryView, String> dateCol = new JFXTreeTableColumn<HistoryView, String>("Date");

    JFXTreeTableColumn<HistoryView, String> linkCol = new JFXTreeTableColumn<HistoryView, String>("Link");

    JFXTreeTableColumn<HistoryView, String> timeCol = new JFXTreeTableColumn<HistoryView, String>("Time");

    JFXTreeTableColumn<HistoryView, String> domainCol = new JFXTreeTableColumn<HistoryView, String>("Domain Name");

    JFXTreeTableColumn<HistoryView, String> titleCol = new JFXTreeTableColumn<HistoryView, String>("Title");

    TreeItem<HistoryView> root;

    TranslateTransition actionBarTransition, searchBarTransition;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (ReferencableManager.getInstance().contain(this)){
            ReferencableManager.getInstance().add(this);
        }
        //region add columns and data
        addColumns();
        /*try{

            FileReader fileReader = new FileReader("history.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine())!=null){
                String[] temp = line.split("´");
                HistoryView historyView = new HistoryView(temp[1], temp[0], temp[2], temp[4], temp[3]);
                root.getChildren().add(new TreeItem<>(historyView));
            }
        }
        catch (IOException e){
            System.out.println(getClass().getSimpleName());
            e.printStackTrace();
        }*/
        try{
            Statement statement = MySqlDatabase.getInstance().getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM history");
            while (resultSet.next()){
                String url = resultSet.getString("url");
                String date = resultSet.getString("accessdate");
                String time = resultSet.getString("accesstime");
                String title = resultSet.getString("title");
                String domain = resultSet.getString("domain");
                HistoryView historyView = new HistoryView(date, url, time, domain, title);
                root.getChildren().add(new TreeItem<>(historyView));
            }
            MySqlDatabase.getInstance().Disconnect();
        }
        catch (SQLException e){
            System.out.println("History controller initialize");
            e.printStackTrace();
        }
        //endregion

        //region actionBar transition
        actionBarTransition = new TranslateTransition();
        actionBarTransition.setDuration(Duration.millis(500));
        actionBarTransition.setNode(actionBar);
        //endregion

        cancelButton.setOnMouseClicked(e->{
            playTransition(-60);

        });

        tbvHistory.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tbvHistory.setOnMouseClicked(e->{
            int numberOfSelectedItems = tbvHistory.getSelectionModel().getSelectedItems().size();
            if (actionBar.getLayoutY() < 0 && numberOfSelectedItems > 0) playTransition(60);
            if (numberOfSelectedItems > 0 && numberOfSelectedItems < 2)
                gotoWebsiteButton.setDisable(false);
            else
                gotoWebsiteButton.setDisable(true);
            TreeItem<HistoryView> temp = (TreeItem<HistoryView>) tbvHistory.getSelectionModel().getSelectedItem();
            lblNumberSelectedItems.setText(numberOfSelectedItems + " selected items");
            int a = 2;
        });

        gotoWebsiteButton.setOnMouseClicked(e->gotoWebSite());
    }

    private void addColumns(){
        dateCol.setPrefWidth(150);
        dateCol.setCellValueFactory(
                new Callback<TreeTableColumn.CellDataFeatures<HistoryView, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<HistoryView, String> param) {
                        return param.getValue().getValue().date;
                    }
                });

        linkCol.setPrefWidth(400);
        linkCol.setCellValueFactory(
                new Callback<TreeTableColumn.CellDataFeatures<HistoryView, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<HistoryView, String> param) {
                        return param.getValue().getValue().link;
                    }
                });
        timeCol.setPrefWidth(150);
        timeCol.setCellValueFactory(
                new Callback<TreeTableColumn.CellDataFeatures<HistoryView, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<HistoryView, String> param) {
                        return param.getValue().getValue().time;
                    }
                });
        domainCol.setPrefWidth(200);
        domainCol.setCellValueFactory(
                new Callback<TreeTableColumn.CellDataFeatures<HistoryView, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<HistoryView, String> param) {
                        return param.getValue().getValue().domain;
                    }
                });
        titleCol.setPrefWidth(150);
        titleCol.setCellValueFactory(
                new Callback<TreeTableColumn.CellDataFeatures<HistoryView, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<HistoryView, String> param) {
                        return param.getValue().getValue().title;
                    }
                });

        //final TreeItem<HistoryView> root = new RecursiveTreeItem<HistoryView>(,
        //        RecursiveTreeObject::getChildren);
        /*List<HistoryView> list = Arrays.<HistoryView>asList(
                new HistoryView("11/03/2018", "www.facebook.com", "9:16", "facebook.com", "Facebook"),
                new HistoryView("11/03/2018", "www.google.com", "9:31", "Google.com.vn", "Google"),
                new HistoryView("11/03/2018", "www.youtube.com", "9:40", "youtube.com", "Youtube")
        );*/
        root = new RecursiveTreeItem<HistoryView>(new HistoryView("","","","",""),
                RecursiveTreeObject::getChildren);
        tbvHistory.setRoot(root);
        tbvHistory.getColumns().setAll(dateCol, linkCol, timeCol, domainCol, titleCol);
        tbvHistory.setShowRoot(false);
    }

    public JFXTreeTableView getTbvHistory() {
        return tbvHistory;
    }

    @Override
    public String getID() {
        return FXMLPATH;
    }

    @Override
    public Object getController() {
        return this;
    }

    private void playTransition(int a){
        actionBarTransition.setToY(a);
        actionBarTransition.play();
    }

    private void gotoWebSite(){
        TreeItem<HistoryView> historyView = (TreeItem<HistoryView>)tbvHistory.getSelectionModel().getSelectedItem();
        String url =  historyView.getValue().link.getValue().substring(8);
        TabPaneController tabPaneController = (TabPaneController) ReferencableManager.getInstance().get(TabPaneController.FXMLPATH);
        tabPaneController.addNewTab(url);
    }
}

class HistoryView extends RecursiveTreeObject<HistoryView>{
    SimpleStringProperty date;
    SimpleStringProperty link;
    SimpleStringProperty time;
    SimpleStringProperty domain;
    SimpleStringProperty title;

    public HistoryView(String date, String link, String time, String domain, String title) {
        this.date = new SimpleStringProperty(date);
        this.link = new SimpleStringProperty(link);
        this.time = new SimpleStringProperty(time);
        this.domain = new SimpleStringProperty(domain);
        this.title = new SimpleStringProperty(title);
    }
}