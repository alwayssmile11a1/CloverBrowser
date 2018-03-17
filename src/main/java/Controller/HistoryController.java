package Controller;

import Model.ReferencableInterface.IReferencable;
import Model.ReferencableInterface.ReferencableManager;
import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
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
import javafx.util.Callback;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class HistoryController implements Initializable, IReferencable {
    public static final String FXMLPATH = "/View/History.fxml";

    @FXML
    private JFXTreeTableView tbvHistory;
    /*@FXML
    TableView tbHistory;*/

    JFXTreeTableColumn<HistoryView, String> dateCol = new JFXTreeTableColumn<HistoryView, String>("Date");

    JFXTreeTableColumn<HistoryView, String> linkCol = new JFXTreeTableColumn<HistoryView, String>("Link");

    JFXTreeTableColumn<HistoryView, String> timeCol = new JFXTreeTableColumn<HistoryView, String>("Time");

    JFXTreeTableColumn<HistoryView, String> domainCol = new JFXTreeTableColumn<HistoryView, String>("Domain Name");

    JFXTreeTableColumn<HistoryView, String> titleCol = new JFXTreeTableColumn<HistoryView, String>("Title");

    TreeItem<HistoryView> root;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (ReferencableManager.getInstance().contain(this)){
            ReferencableManager.getInstance().add(this);
        }
        addColumns();
        try{

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
        }
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
        tbvHistory.setShowRoot(true);
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