package Controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;

public class TabContentController implements Initializable {

    public static final String FXMLPATH = "/View/tabcontent.fxml";

    //manage web pages
    private WebEngine webEngine;

    //FXML variables
    @FXML
    private  WebView webView;

    @FXML
    private TextField addressBar;

    @FXML
    private Button goBackButton;

    @FXML
    private Button goForwardButton;

    @FXML
    private Button refreshButton;

    private String httpHeader = "https://www.";


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        webEngine = webView.getEngine();

        //load google.com by default
        webEngine.load(httpHeader+ "google.com");

        //region go back and go forward and refresh
        // there something i need to test in this region
        goBackButton.setOnMouseClicked(this::OnBackButtonClicked);
        goForwardButton.setOnMouseClicked(this::OnForwardButtonClicked);
        refreshButton.setOnMouseClicked(e->{
            webEngine.reload();
            ObservableList<WebHistory.Entry> entries = webEngine.getHistory().getEntries();

            int a = 2;
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
    }

    public void loadPage()
    {
        addressBar.setFocusTraversable(false);
        webEngine.load(httpHeader+ addressBar.getText());
    }

    private void OnBackButtonClicked(MouseEvent e){
        Platform.runLater(() -> {
            webEngine.executeScript("history.back()");
        });
    }

    private void OnForwardButtonClicked(MouseEvent e){
        Platform.runLater(() -> {
            webEngine.executeScript("history.forward()");
        });
    }

}