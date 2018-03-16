package Controller;

import Application.Main;
import Model.HTMLtoPDF.HTMLtoPDFHelper;
import Model.Printing.PrintingHelper;
import Model.ReferencableInterface.IReferencable;
import Model.ReferencableInterface.ReferencableManager;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPopup;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;


public class TabContentController implements Initializable {

    public static final String FXMLPATH = "/View/tabcontent.fxml";

    //manage web pages
    private WebEngine webEngine;
    Worker<Void> worker;

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
    private Button taskButton;

    private String httpHeader = "https://www.";

    private TabPaneController tabPaneController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        webEngine = webView.getEngine();
        worker = webEngine.getLoadWorker();
        worker.stateProperty().addListener(new ChangeListener<State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
//                System.out.println("Loading state: " + newValue.toString());
//                if (newValue == Worker.State.SUCCEEDED) {
//                    System.out.println("Finish!");
//                    FXMLLoader fxmlLoader = new FXMLLoader();
//                    try {
//                        fxmlLoader.setLocation(Main.class.getResource(TabPaneController.FXMLPATH));
//                        Parent root = fxmlLoader.load();
//                        //fxmlLoader.load();
//                        String title = webEngine.getTitle();
//
//                        TabPaneController controller = fxmlLoader.getController();
//
//
//
//
//                        int a = 2;
//                    }
//                    catch (Exception e){
//                        e.printStackTrace();
//                    }
//                }

            }
        });

        TabPaneController tabPaneController = (TabPaneController) (ReferencableManager.getInstance().get(TabPaneController.FXMLPATH));

        

        //load google.com by default
        webEngine.load(httpHeader + "google.com");
        

        webEngine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
            @Override
            public void handle(WebEvent<String> event) {
                onWebPageChanged();
            }
        });



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

                        }
                    }
                });
            }
        });


        //Setting up task button
        JFXPopup popup = new JFXPopup();
        JFXButton toPDFButton = new JFXButton("ToPDF");
        JFXButton printButton = new JFXButton("Printing");
        VBox vBox = new VBox();
        vBox.getChildren().addAll(toPDFButton, printButton);
        popup.setPopupContent(vBox);

        taskButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                popup.show(taskButton, JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.RIGHT, event.getX(), event.getY());
            }
        });


        toPDFButton.setOnMouseClicked(event -> convertToPDF());
        printButton.setOnMouseClicked(event -> printWebPage());

    }

    private void addToHistory() {
        ObservableList<WebHistory.Entry> entries = webEngine.getHistory().getEntries();
        int i = entries.size() - 1;
        String url = entries.get(i).getUrl();
        String title = entries.get(i).getTitle();
        String[] temp = entries.get(i).getLastVisitedDate().toString().split(" ");
        String date = temp[2] + " " + temp[1] + " " + temp[5];
        String time = temp[3];

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource((TabPaneController.FXMLPATH)));
        HistoryController historyController = fxmlLoader.getController();
        historyController.getTbvHistory().getRoot().getChildren().add(new TreeItem<>(new HistoryView(date, url, time, "a", title)));
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

    private void onWebPageChanged()
    {
        addressBar.setText(webEngine.getLocation().toString());

        //disable go forward button if needed
        if(webEngine.getHistory().getCurrentIndex() == webEngine.getHistory().getEntries().size()-1)
        {
            goForwardButton.setDisable(true);
        }
        else
        {
            goForwardButton.setDisable(false);
        }

        //disable go back button if needed
        if(webEngine.getHistory().getCurrentIndex() == 0)
        {
            goBackButton.setDisable(true);
        }
        else
        {
            goBackButton.setDisable(false);
        }

    }

    public void loadPage() {
        addressBar.setFocusTraversable(false);
        webEngine.load(httpHeader + addressBar.getText());

    }

    private void convertToPDF()
    {
        FileChooser fileChooser = new FileChooser();

        // Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF File (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);

        // Show save file dialog
        // The stage for show dialouge is get from MainClass stage
        File file = fileChooser.showSaveDialog(Main.getStage());

        if(file!=null) {
            HTMLtoPDFHelper.execute(webEngine.getLocation().toString(), file);
        }

    }

    private void printWebPage()
    {
        PrintingHelper.excute(webEngine);
    }


}
