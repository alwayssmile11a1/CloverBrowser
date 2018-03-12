package Controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXPopup;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

import java.awt.print.Book;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TabContentController implements Initializable {

    public static final String FXMLPATH = "/View/tabcontent.fxml";

    //manage web pages
    private WebEngine webEngine;
    private JFXPopup popup;

    //FXML variables
    @FXML
    private WebView webView;

    @FXML
    private TextField addressBar;

    @FXML
    private Button goBackButton;

    @FXML
    private Button goForwardButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button menuButton;

    private String httpHeader = "https://www.";


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        webEngine = webView.getEngine();

        //load google.com by default
        webEngine.load(httpHeader + "google.com");

        //region go back and go forward and refresh
        goBackButton.setOnMouseClicked(this::OnBackButtonClicked);
        goForwardButton.setOnMouseClicked(this::OnForwardButtonClicked);
        refreshButton.setOnMouseClicked(e -> {
            webEngine.reload();
        });
        //endregion
        addressBar.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (addressBar.isFocused() && !addressBar.getText().isEmpty()) {
                            addressBar.selectAll();
                            System.out.println("asasdasd");
                        }
                    }
                });
            }
        });


        popup = new JFXPopup(menuButton);
        JFXButton History = new JFXButton("History");
        History.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                onHistoryButtonClicked();
            }
        });
        JFXButton Bookmarks = new JFXButton("Bookmarks");
        VBox vBox = new VBox();
        vBox.getChildren().addAll(History, Bookmarks);
        popup.setPopupContent(vBox);
        menuButton.setOnMouseClicked(this::OnMenuButtonClicked);
    }

    public void loadPage() {
        addressBar.setFocusTraversable(false);
        webEngine.load(httpHeader + addressBar.getText());
        addToHistory();

    }

    private void addToHistory() {
        ObservableList<WebHistory.Entry> entries = webEngine.getHistory().getEntries();
        int i = entries.size() - 1;
        String url = entries.get(i).getUrl();
        String title = entries.get(i).getTitle();
        String[] temp = entries.get(i).getLastVisitedDate().toString().split(" ");
        String date = temp[2] + " " + temp[1] + " " + temp[5];
        String time = temp[3];


        HistoryController.tbvHistory.getRoot().getChildren().add(new TreeItem<>(new HistoryView(date, url, time, "a", title)));
    }

    private void OnBackButtonClicked(MouseEvent e) {
        Platform.runLater(() -> {
            webEngine.executeScript("history.back()");
        });
    }

    private void OnForwardButtonClicked(MouseEvent e) {
        Platform.runLater(() -> {
            webEngine.executeScript("history.forward()");
        });
    }

    private void OnMenuButtonClicked(MouseEvent e) {
        int a;
        popup.show(menuButton, JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.LEFT);
    }

    private void onHistoryButtonClicked() {
        openHistoryTab();
    }

    private void openHistoryTab() {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource((TabPaneController.FXMLPATH)));
            AnchorPane frame = fxmlLoader.load();
            TabPaneController tabPaneController = (TabPaneController) fxmlLoader.getController();
            TabPane tabPane = tabPaneController.getTabPane();

            //Create a new tab
            Tab tab = new Tab();
            tab.setContent(FXMLLoader.load(getClass().getResource(HistoryController.FXMLPATH)));

            tabPane.getTabs().add(tabPane.getTabs().size() - 1, tab);
            tabPane.getSelectionModel().select(tab);

            tab.setText("History");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
