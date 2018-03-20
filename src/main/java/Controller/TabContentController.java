package Controller;

import Application.Main;
import Model.HTMLtoPDF.HTMLtoPDFHelper;
import Model.MySqlDatabase.MySqlDatabase;
import Model.Printing.PrintingHelper;
import Model.ReferencableInterface.IReferencable;
import Model.ReferencableInterface.ReferencableManager;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPopup;
import com.sun.istack.internal.NotNull;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ResourceBundle;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.stage.PopupWindow;


public class TabContentController implements Initializable{

    public static final String FXMLPATH = "/View/tabcontent.fxml";

    //manage web pages
    private WebEngine webEngine;
    Worker<Void> worker;
    WebHistory webHistory;

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

    @FXML
    private ProgressBar progressLoad;

    private String httpHeader = "https://www.";

    private JFXPopup popup;
    public static boolean loadDefault = true;

    public static String link;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        webEngine = webView.getEngine();

        webHistory = webEngine.getHistory();
        webHistory.getEntries().addListener(new ListChangeListener<WebHistory.Entry>() {
            @Override
            public void onChanged(Change<? extends WebHistory.Entry> c) {
                //int n = c.getAddedSize();
                ObservableList<WebHistory.Entry> listEntry = (ObservableList<WebHistory.Entry>)c.getList();
                addressBar.setText(listEntry.get(listEntry.size()-1).getUrl());
                /*try{

                }
                catch (Exception e){
                    e.printStackTrace();
                }*/
                webEngine.reload();
                int a = 2;
            }
        });

        //region worker
        worker = webEngine.getLoadWorker();
        worker.stateProperty().addListener(new ChangeListener<State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                System.out.println("Loading state: " + newValue.toString());
                if (newValue == Worker.State.SUCCEEDED) {
                    ObservableList<WebHistory.Entry> listEntry = (ObservableList<WebHistory.Entry>)webHistory.getEntries();
                    System.out.println("Finish!");
                    try {
                        String title = webEngine.getTitle();
                        TabPaneController tabPaneController = (TabPaneController) (ReferencableManager.getInstance().get(TabPaneController.FXMLPATH));
                        tabPaneController.changeTabText(title);
                        progressLoad.setVisible(false);
                        //region add to file history
                        int i = webHistory.getEntries().size() - 1;
                        WebHistory.Entry entry = webHistory.getEntries().get(i);
                        String url = entry.getUrl();
                        String[] temp = entry.getLastVisitedDate().toString().split(" ");
                        String date = temp[5] + "-" + MonthToNum(temp[1]) + "-" + temp[2];
                        String time = temp[3];
                        String domain = url.split("/")[2].substring(4);
                        String separator="´";
                        String history = url+separator+date+separator+time+separator+title+separator+domain+"\r\n";
                        FileWriter fileWriter = new FileWriter("history.txt", true);
                        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                        bufferedWriter.append(history);
                        bufferedWriter.close();
                        fileWriter.close();
                        int a = 2;
                        //endregion

                        //region add to DB
                        PreparedStatement preparedStatement = MySqlDatabase.getInstance().getConnection().prepareStatement("INSERT INTO history value (?,?,?,?,?)");
                        preparedStatement.setString(1, url);
                        preparedStatement.setString(2, date);
                        preparedStatement.setString(3, time);
                        preparedStatement.setString(4, title);
                        preparedStatement.setString(5, domain);
                        preparedStatement.executeUpdate();
                        MySqlDatabase.getInstance().Disconnect();
                        //endregion
                    }
                    catch (Exception e){
                        System.out.println("changed");
                        e.printStackTrace();
                    }
                }

            }
        });
        //endregion

        //load google.com by default
        if (loadDefault)
            webEngine.load(httpHeader + "google.com");
        else
            webEngine.load(httpHeader + link);
        

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

        //region task button + popup
        //Setting up task button
        popup = new JFXPopup();
        JFXButton historyButton = new JFXButton("History");
        HBox historyHBox = new HBox();
        addImageToPopup(historyHBox, "../resources/Drawable/icons8-time-machine-25.png");
        historyHBox.getChildren().add(historyButton);
        historyHBox.setMargin(historyButton, new Insets(5, 5, 5, 5));
        //historyHBox.setMargin(historyButton, new Insets(0, 0, 0 ,5));

        JFXButton bookmarkButton = new JFXButton("Bookmarks");
        HBox bookmarkHBox = new HBox();
        addImageToPopup(bookmarkHBox, "../resources/Drawable/icons8-bookmark-25 (1).png");
        bookmarkHBox.getChildren().add(bookmarkButton);
        bookmarkHBox.setMargin(bookmarkButton, new Insets(5, 5, 5, 5));

        JFXButton toPDFButton = new JFXButton("ToPDF");
        HBox toPDFHBox = new HBox();
        addImageToPopup(toPDFHBox, "../resources/Drawable/icons8-pdf-25.png");
        toPDFHBox.getChildren().add(toPDFButton);
        toPDFHBox.setMargin(toPDFButton, new Insets(5, 5, 5, 5));

        JFXButton printButton = new JFXButton("Printing");
        HBox printHBox = new HBox();
        addImageToPopup(printHBox, "../resources/Drawable/printer.png");
        printHBox.getChildren().add(printButton);
        printHBox.setMargin(printButton, new Insets(5, 5, 5, 5));

        VBox vBox = new VBox();
        vBox.getChildren().addAll(historyHBox, bookmarkHBox, toPDFHBox, printHBox);
        popup.setPopupContent(vBox);

        taskButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                popup.show(taskButton, JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.RIGHT, event.getX(), event.getY());
            }
        });


        toPDFButton.setOnMouseClicked(event -> convertToPDF());
        printButton.setOnMouseClicked(event -> printWebPage());
        historyButton.setOnMouseClicked(e->addHistoryTab());
        //endregion
    }

    private void addImageToPopup(HBox h, String url){
        try{
            ImageView imageView = new ImageView(new Image(url, 25, 25, false, false));
            //historyImage.setFitHeight(25);
            //historyImage.setFitWidth(25);
            h.getChildren().add(imageView);
            h.setMargin(imageView, new Insets(8, 8, 0, 5));
        }
        catch (Exception e){
            System.out.println("TabContentController");
            e.printStackTrace();
        }
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
        progressLoad.setVisible(true);
        progressLoad.progressProperty().bind(worker.progressProperty());

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

    private void addHistoryTab(){
        TabPaneController tabPaneController = (TabPaneController)ReferencableManager.getInstance().get(TabPaneController.FXMLPATH);
        tabPaneController.addNewTab(true);
        popup.hide();
    }

    @NotNull
    private String MonthToNum(String m){
        switch (m){
            case "Jan": return "01";
            case "Feb": return "02";
            case "Mar": return "03";
            case "Apr": return "04";
            case "May": return "05";
            case "Jun": return "06";
            case "Jul": return "07";
            case "Aug": return "08";
            case "Sep": return "09";
            case "Oct": return "10";
            case "Nov": return "11";
            case "Dec": return "12";
            default:return"";
        }
    }

}
