package Controller;

import Model.MonthToNum.MonthToNum;
import Model.ReferencableInterface.IReferencable;
import Model.ReferencableInterface.ReferencableManager;
import Model.SqliteDatabase.SQLiteDatabase;
import Model.SqliteDatabase.SqliteDatabaseBookmarks;
import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import com.sun.istack.internal.NotNull;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class BookmarksController implements Initializable, IReferencable {
    public static final String FXMLPATH = "/View/Bookmarks.fxml";

    @FXML
    private JFXTreeTableView tbvBookmarks;

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

    JFXTreeTableColumn<BookmarksView, String> titleCol = new JFXTreeTableColumn<BookmarksView, String>("Title");

    JFXTreeTableColumn<BookmarksView, String> linkCol = new JFXTreeTableColumn<BookmarksView, String>("Link");

    TreeItem<BookmarksView> root;

    TranslateTransition actionBarTransition, searchBarTransition;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!ReferencableManager.getInstance().contain(this)) {
            ReferencableManager.getInstance().add(this);
        }
        //region add columns and data
        addColumns();
        try {
            Statement statement = SqliteDatabaseBookmarks.getInstance().getConnection().createStatement();
            root = new RecursiveTreeItem<BookmarksView>(new BookmarksView("", ""), RecursiveTreeObject::getChildren);
            tbvBookmarks.getRoot().getChildren().add(root);
            ResultSet resultSet = statement.executeQuery("SELECT * FROM 'bookmarks' ");
            while (resultSet.next()) {
                String title = resultSet.getString("title");
                String url = resultSet.getString("url");
                BookmarksView bookmarksView = new BookmarksView(title, url);
                root.getChildren().add(new TreeItem<>(bookmarksView));
            }

        } catch (SQLException e) {
            System.out.println("Bookmarks controller initialize");
            e.printStackTrace();
        } finally {
            SqliteDatabaseBookmarks.getInstance().Disconnect();
        }
        //endregion

        //region actionBar transition
        actionBarTransition = new TranslateTransition();
        actionBarTransition.setDuration(Duration.millis(500));
        actionBarTransition.setNode(actionBar);
        //endregion

        cancelButton.setOnMouseClicked(e -> {
            playTransition(-60);

        });

        tbvBookmarks.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tbvBookmarks.setOnMouseClicked(e -> {
            int numberOfSelectedItems = tbvBookmarks.getSelectionModel().getSelectedItems().size();
            if (actionBar.getLayoutY() < 0 && numberOfSelectedItems > 0) playTransition(60);
            if (numberOfSelectedItems > 0 && numberOfSelectedItems < 2)
                gotoWebsiteButton.setDisable(false);
            else
                gotoWebsiteButton.setDisable(true);
            TreeItem<BookmarksView> temp = (TreeItem<BookmarksView>) tbvBookmarks.getSelectionModel().getSelectedItem();
            lblNumberSelectedItems.setText(numberOfSelectedItems + " selected items");
            int a = 2;
        });

        gotoWebsiteButton.setOnMouseClicked(e -> gotoWebSite());
        deleteButton.setOnMouseClicked(e -> deleteBookmarks());
    }

    private void addColumns() {
        titleCol.setPrefWidth(150);
        titleCol.setCellValueFactory(
                new Callback<TreeTableColumn.CellDataFeatures<BookmarksView, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<BookmarksView, String> param) {
                        return param.getValue().getValue().title;
                    }
                });

        linkCol.setPrefWidth(400);
        linkCol.setCellValueFactory(
                new Callback<TreeTableColumn.CellDataFeatures<BookmarksView, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<BookmarksView, String> param) {
                        return param.getValue().getValue().link;
                    }
                });

        root = new RecursiveTreeItem<BookmarksView>(new BookmarksView("", ""),
                RecursiveTreeObject::getChildren);
        tbvBookmarks.setRoot(root);
        tbvBookmarks.getColumns().setAll(titleCol, linkCol);
        tbvBookmarks.setShowRoot(false);
    }

    public JFXTreeTableView getTbvBookmarks() {
        return tbvBookmarks;
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
        TreeItem<BookmarksView> bookmarksView = (TreeItem<BookmarksView>) tbvBookmarks.getSelectionModel().getSelectedItem();
        String url = bookmarksView.getValue().link.getValue().substring(8);
        TabPaneController tabPaneController = (TabPaneController) ReferencableManager.getInstance().get(TabPaneController.FXMLPATH);
        tabPaneController.addNewTab(url);
    }

    private void deleteBookmarks() {
        ObservableList<TreeItem<BookmarksView>> bookmarks = tbvBookmarks.getSelectionModel().getSelectedItems();
        for (TreeItem<BookmarksView> item : bookmarks) {
            String url = item.getValue().link.getValue();
            if (url.equals("")) {
                System.out.println("It's root");
                continue;
            }
            try {
                //region add to DB
                PreparedStatement preparedStatement = SqliteDatabaseBookmarks.getInstance().getConnection().prepareStatement("DELETE FROM bookmarks WHERE url=?");
                preparedStatement.setString(1, url);
                preparedStatement.executeUpdate();
                //endregion
                System.out.println("Successfully delete in db");
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            } finally {
                SqliteDatabaseBookmarks.getInstance().Disconnect();
            }
        }
        List<TreeItem<BookmarksView>> nodes = tbvBookmarks.getSelectionModel().getSelectedItems();
        int n = nodes.size();
        while(n > 0){
            TreeItem<BookmarksView> temp = nodes.get(0);
            TreeItem<BookmarksView> parent = nodes.get(0).getParent();
            if (parent != null){
                parent.getChildren().remove(nodes.get(0));
                n--;
            }
            else {
                return;
            }
        }
    }
}
class BookmarksView extends RecursiveTreeObject<BookmarksView>{
    SimpleStringProperty title;
    SimpleStringProperty link;

    public BookmarksView(String title, String link) {
        this.title = new SimpleStringProperty(title);
        this.link = new SimpleStringProperty(link);
    }
}