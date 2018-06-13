package Controller;

import Model.MonthToNum.MonthToNum;
import Model.ReferencableInterface.IReferencable;
import Model.ReferencableInterface.ReferencableManager;
import Model.SqliteDatabase.SQLiteDatabase;
import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import com.sun.istack.internal.NotNull;
import com.sun.org.apache.xpath.internal.SourceTree;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
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
import javafx.fxml.FXMLLoader;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

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

    @FXML
    private Button deleteButton;

    @FXML
    private TextField txbSearch;

    @FXML Button btnRefresh;

    JFXTreeTableColumn<HistoryView, String> dateCol = new JFXTreeTableColumn<HistoryView, String>("Date");

    JFXTreeTableColumn<HistoryView, String> linkCol = new JFXTreeTableColumn<HistoryView, String>("Link");

    JFXTreeTableColumn<HistoryView, String> timeCol = new JFXTreeTableColumn<HistoryView, String>("Time");

    JFXTreeTableColumn<HistoryView, String> domainCol = new JFXTreeTableColumn<HistoryView, String>("Domain Name");

    JFXTreeTableColumn<HistoryView, String> titleCol = new JFXTreeTableColumn<HistoryView, String>("Title");

    TreeItem<HistoryView> root;

    List<TreeItem<HistoryView>> listRoot = new ArrayList<>();

    TranslateTransition actionBarTransition, searchBarTransition;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!ReferencableManager.getInstance().contain(this)) {
            ReferencableManager.getInstance().add(this);
        }
        //region add columns and data
        addColumns();

        addDataToTreeTableView();
        //endregion

        //region actionBar transition
        actionBarTransition = new TranslateTransition();
        actionBarTransition.setDuration(Duration.millis(500));
        actionBarTransition.setNode(actionBar);
        //endregion

        cancelButton.setOnMouseClicked(e -> {
            playTransition(-60);

        });

        tbvHistory.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tbvHistory.setOnMouseClicked(e -> {
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

        gotoWebsiteButton.setOnMouseClicked(e -> gotoWebSite());
        deleteButton.setOnMouseClicked(e -> deleteHistory());

        txbSearch.textProperty().addListener(e->SearchChange());

        btnRefresh.setOnAction(e->RefreshHistory());
    }

    private void addColumns() {
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
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-yyyy");
        root = new RecursiveTreeItem<HistoryView>(new HistoryView(simpleDateFormat.format(new Date()), "", "", "", ""),
                RecursiveTreeObject::getChildren);
        tbvHistory.setRoot(root);
        tbvHistory.getColumns().setAll(dateCol, linkCol, timeCol, domainCol, titleCol);
        tbvHistory.setShowRoot(false);
    }

    private void addDataToTreeTableView() {
        try {
            Statement statement = SQLiteDatabase.getInstance().getConnection().createStatement();
            PreparedStatement preparedStatement = SQLiteDatabase.getInstance().getConnection().prepareStatement("SELECT name FROM sqlite_master WHERE type=?");
            preparedStatement.setString(1, "table");
            ResultSet tables = preparedStatement.executeQuery();
            while (tables.next()){
                String name = tables.getString("name");
                String table = name.substring(5).substring(0,2) + "-" + name.substring(5).substring(2);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-yyyy");
                root = new RecursiveTreeItem<HistoryView>(new HistoryView(table, "", "", "", ""),
                        RecursiveTreeObject::getChildren);
                //System.out.println(root.getValue().date);
                tbvHistory.getRoot().getChildren().add(root);
                ResultSet resultSet = statement.executeQuery("SELECT * FROM " + name);
                while (resultSet.next()) {
                    AddDataToRow(resultSet, root);
                }
                listRoot.add(root);
            }

        } catch (SQLException e) {
            System.out.println("History controller initialize");
            e.printStackTrace();
        } finally {
            SQLiteDatabase.getInstance().Disconnect();
        }
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

    private void playTransition(int a) {
        actionBarTransition.setToY(a);
        actionBarTransition.play();
    }

    private void gotoWebSite() {
        TreeItem<HistoryView> historyView = (TreeItem<HistoryView>) tbvHistory.getSelectionModel().getSelectedItem();
        String t = historyView.getValue().date.getValue().toString();
        if (t.length() > 7) {
            String url = historyView.getValue().link.getValue().substring(8);
            TabPaneController tabPaneController = (TabPaneController) ReferencableManager.getInstance().get(TabPaneController.FXMLPATH);
            tabPaneController.addNewTab(url);
        }
    }

    private void deleteHistory() {
        ObservableList<TreeItem<HistoryView>> histories = tbvHistory.getSelectionModel().getSelectedItems();
        try {
            for (TreeItem<HistoryView> item : histories) {
                String url = item.getValue().link.getValue();
                if (url.equals("")) {
                    System.out.println("It's root");
                    continue;
                }
                String accessDate = item.getValue().date.getValue();
                String accessTime = item.getValue().time.getValue();


                TreeItem<HistoryView> parent = item.getParent();
                String []temp = parent.getValue().date.getValue().split("-");
                String table = "month"+temp[0]+temp[1];
                //parent.getValue().
                //region delete from DB
                PreparedStatement preparedStatement = SQLiteDatabase.getInstance().getConnection().prepareStatement("DELETE FROM " + table + " WHERE url=? AND accessdate = ? AND accesstime = ?");
                preparedStatement.setString(1, url);
                preparedStatement.setString(2, accessDate);
                preparedStatement.setString(3, accessTime);
                preparedStatement.executeUpdate();
                //endregion
                System.out.println("successfully delete in db");
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            SQLiteDatabase.getInstance().Disconnect();
        }

        List<TreeItem<HistoryView>> nodes = tbvHistory.getSelectionModel().getSelectedItems();
        int n = histories.size();
        while(n > 0){
            TreeItem<HistoryView> temp = nodes.get(n - 1);
            if(temp.getValue().link.getValue().equals("")){
                n--;
                continue;
            }
            TreeItem<HistoryView> parent = temp.getParent();
            if (parent != null){
                parent.getChildren().remove(temp);
                n--;
            }
            else {
                return;
            }
        }
        tbvHistory.getSelectionModel().clearSelection();
        //tbvHistory.getSelectionModel().getSelectedItems().clear();
    }

    private void SearchChange(){
        if(txbSearch.getText().length() == 0){
            tbvHistory.getRoot().getChildren().clear();
            for (TreeItem<HistoryView> item : listRoot) {
                tbvHistory.getRoot().getChildren().add(item);
            }
        }
        else {
            tbvHistory.getRoot().getChildren().clear();
            PreparedStatement preparedStatementTable = null;
            PreparedStatement preparedStatementData = null;
            try {
                preparedStatementTable = SQLiteDatabase.getInstance().getConnection().prepareStatement("SELECT name FROM sqlite_master WHERE type=?");
                preparedStatementTable.setString(1, "table");
                ResultSet tables = preparedStatementTable.executeQuery();
                while (tables.next()){
                    String name = tables.getString("name");
                    preparedStatementData = SQLiteDatabase.getInstance().getConnection().prepareStatement("SELECT * FROM " + name + " WHERE url LIKE ?");
                    preparedStatementData.setString(1, "%" + txbSearch.getText() + "%");
                    ResultSet resultSet = preparedStatementData.executeQuery();
                    while (resultSet.next()){
                        AddDataToRow(resultSet, tbvHistory.getRoot());
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            finally {
                SQLiteDatabase.getInstance().Disconnect();
            }
        }
    }

    private void RefreshHistory(){
        Statement statementTable = null;
        Statement statementData = null;
        try {
            statementTable = SQLiteDatabase.getInstance().getConnection().createStatement();
            ResultSet resultSet = statementTable.executeQuery("SELECT name FROM sqlite_master where type = 'table'");
            String table = "";
            while (resultSet.next()){
                 table = resultSet.getString("name");
                System.out.println(table);
            }
            if(!table.equals("")){
                String name = table.substring(5).substring(0,2) + "-" + table.substring(5).substring(2);
                statementData = SQLiteDatabase.getInstance().getConnection().createStatement();
                ResultSet resultSetData = statementData.executeQuery("SELECT * FROM " + table);
                TreeItem<HistoryView> root = new RecursiveTreeItem<HistoryView>(new HistoryView(name, "", "", "", ""),
                        RecursiveTreeObject::getChildren);
                listRoot.remove(listRoot.size()-1);
                tbvHistory.getRoot().getChildren().remove(tbvHistory.getRoot().getChildren().size() - 1);
                //root.getChildren().removeAll();
                while (resultSetData.next()){
                    AddDataToRow(resultSetData, root);
                }
                listRoot.add(root);
                tbvHistory.getRoot().getChildren().add(root);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
//        int n = tbvHistory.getRoot().getChildren().size();
//        tbvHistory.getRoot().getChildren().remove(n - 1);
    }

    private void AddDataToRow(ResultSet resultSet, TreeItem<HistoryView> root) throws SQLException {
        String url = resultSet.getString("url");
        String date = resultSet.getString("accessdate");
        String time = resultSet.getString("accesstime");
        String title = resultSet.getString("title");
        String domain = resultSet.getString("domain");
        HistoryView historyView = new HistoryView(date, url, time, domain, title);
        root.getChildren().add(new TreeItem<>(historyView));
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