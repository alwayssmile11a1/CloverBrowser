package Controller;

import Application.Main;
import Model.HTMLtoPDF.HTMLtoPDFHelper;
import Model.MonthToNum.MonthToNum;
import Model.Printing.PrintingHelper;
import Model.ReferencableInterface.IReferencable;
import Model.ReferencableInterface.ReferencableManager;
import Model.SqliteDatabase.SQLiteDatabase;
import Model.WebProposal.WebProposal;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPopup;
import com.sun.istack.internal.NotNull;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.stage.PopupWindow;
import javafx.stage.WindowEvent;
import net.sf.image4j.codec.ico.ICODecoder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class TabContentController implements Initializable, IReferencable{

    public static final String FXMLPATH = "/View/tabcontent.fxml";

    //manage web pages
    private WebEngine webEngine;
    Worker<Void> worker;
    WebHistory webHistory;

    //region declare variables
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

    private final ContextMenu contextMenu = new ContextMenu();

    public static boolean loadDefault = true;

    public static String link="google.com";

    private ContextMenu webViewContextMenu;
    private ContextMenu tabContextMenu;
    //endregion

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!ReferencableManager.getInstance().contain(this)) {
            ReferencableManager.getInstance().add(this);
        }
        webView.setContextMenuEnabled(false);
        webView.setOnMouseClicked(e->{
            boolean isrightMouse = e.isSecondaryButtonDown();
            if (e.getButton() == MouseButton.SECONDARY){
                webViewContextMenu.show(webView, e.getScreenX(), e.getScreenY());
                tabContextMenu.show(webView, e.getScreenX()+400, e.getScreenY());
            }
            else if (e.getButton() == MouseButton.PRIMARY)
                webViewContextMenu.hide();
        });

        //region getEngine + getHistory
        webEngine = webView.getEngine();

        webHistory = webEngine.getHistory();
        webHistory.getEntries().addListener(new ListChangeListener<WebHistory.Entry>() {
            @Override
            public void onChanged(Change<? extends WebHistory.Entry> c) {
                //int n = c.getAddedSize();
                ObservableList<WebHistory.Entry> listEntry = (ObservableList<WebHistory.Entry>)c.getList();
                addressBar.setText(listEntry.get(listEntry.size()-1).getUrl());
                contextMenu.hide();
                int a = 2;
            }
        });
        //endregion

        //region worker
        worker = webEngine.getLoadWorker();
        worker.stateProperty().addListener(new ChangeListener<State>() {
            @Override
            public void changed(ObservableValue observable, Worker.State oldValue, Worker.State newValue) {
                System.out.println("Loading state: " + newValue.toString());
                if (newValue == Worker.State.SUCCEEDED) {
                    //region set favicon
                    if ("about:blank".equals(webEngine.getLocation()))
                        return;
                    //Determine the full url
                    String favIconFullURL = getHostName(webEngine.getLocation()) + "favicon.ico";
                    TabPaneController tabPaneController = (TabPaneController) ReferencableManager.getInstance().get(TabPaneController.FXMLPATH);
                    tabPaneController.setFavicon(favIconFullURL);
                    //endregion
                    //region store history
                    System.out.println("Finish!");
                    try {
                        String title = webEngine.getTitle();
                        //TabPaneController tabPaneController = (TabPaneController) (ReferencableManager.getInstance().get(TabPaneController.FXMLPATH));
                        progressLoad.setVisible(false);
                        //region add to file history
                        int i = webHistory.getEntries().size() - 1;
                        WebHistory.Entry entry = webHistory.getEntries().get(i);
                        String url = entry.getUrl();
                        String[] temp = entry.getLastVisitedDate().toString().split(" ");
                        String date = temp[5] + "-" + MonthToNum.changeMonthToNum(temp[1]) + "-" + temp[2];
                        String time = temp[3];
                        String domain = url.split("/")[2].substring(4);
                        //region file writer
                        String separator="´";
                        String history = url+separator+date+separator+time+separator+title+separator+domain+"\r\n";
                        FileWriter fileWriter = new FileWriter("history.txt", true);
                        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                        bufferedWriter.append(history);
                        bufferedWriter.close();
                        fileWriter.close();
                        //endregion
                        int a = 2;
                        //endregion

                        //region add to DB
                        PreparedStatement preparedStatement = SQLiteDatabase.getInstance().getConnection().prepareStatement("INSERT INTO "+ SQLiteDatabase.getInstance().getTableName() +" values (?,?,?,?,?)");
                        preparedStatement.setString(1, url);
                        preparedStatement.setString(2, date);
                        preparedStatement.setString(3, time);
                        preparedStatement.setString(4, title);
                        preparedStatement.setString(5, domain);
                        preparedStatement.executeUpdate();
                        //endregion
                    }
                    catch (Exception e){
                        System.out.println("tab content - worker changed");
                        System.err.println(e.getMessage());
                    }
                    finally {
                        SQLiteDatabase.getInstance().Disconnect();
                    }
                    //endregion
                }

            }
        });
        //endregion

        //region load page when creating new tab
        //load google.com by default
        if (loadDefault)
            webEngine.load(httpHeader + link);
        else{
            if (link.contains("www.")){
                link = link.substring(4);
                webEngine.load(httpHeader + link);
            }
            else
                webEngine.load("https://"+link);
            link = "google.com";
        }
        //endregion

        webEngine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
            @Override
            public void handle(WebEvent<String> event) {
                onWebPageChanged(event);
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
        //region proposal list
        // nếu ko có set prewidth nó sẽ bị lệch
        contextMenu.setPrefWidth(200);
        contextMenu.setOnShowing(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                double contextMenuWidth = addressBar.getWidth();
                contextMenu.setStyle("-fx-pref-width: "+contextMenuWidth+";");
            }
        });
        //endregion
        addressBar.textProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (addressBar.getText().length()==0)
                    contextMenu.hide();
                else {
                    if (addressBar.isFocused()){
                        populateContextMenu();
                        contextMenu.show(addressBar, Side.BOTTOM, 0, 0);
                        // Request focus on first item
                        if (!contextMenu.getItems().isEmpty())
                            contextMenu.getSkin().getNode().lookup(".menu-item:nth-child(1)").requestFocus();
                    }
                }
            }
        });
        //region add listener tường minh
        /*final InvalidationListener textListener = v -> {
            if (addressBar.getText().length()==0)
                contextMenu.hide();
            else {
                if (addressBar.isFocused()){
                    populateContextMenu();
                    contextMenu.show(addressBar, Side.BOTTOM, 0, 0);
                    // Request focus on first item
                    if (!contextMenu.getItems().isEmpty())
                        contextMenu.getSkin().getNode().lookup(".menu-item:nth-child(1)").requestFocus();
                }
            }
        };
        addressBar.textProperty().addListener(textListener);*/
        //endregion

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

        JFXButton infoButton = new JFXButton(("About us"));
        HBox infoHBox = new HBox();
        addImageToPopup(infoHBox, "../resources/Drawable/icons8-info-25.png");
        infoHBox.getChildren().add(infoButton);
        infoHBox.setMargin(infoButton, new Insets(5, 5, 5, 5));

        VBox vBox = new VBox();
        vBox.getChildren().addAll(historyHBox, bookmarkHBox, toPDFHBox, printHBox, infoHBox);
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
        infoButton.setOnMouseClicked(e->addInfoTab());
        //endregion

        webViewContextMenu = new ContextMenu();
        webViewContextMenu.setStyle("-fx-pref-width: 300;");
        webViewContextMenu.getItems().addAll(new MenuItem("Back"), new MenuItem("Forward"), new MenuItem(("Reload")));

        tabContextMenu = new ContextMenu();
        tabContextMenu.setStyle("-fx-pref-width: 300;");
        tabContextMenu.getItems().addAll(new MenuItem("New tab"), new MenuItem("Reload"), new MenuItem("Duplicate"),
                                         new MenuItem("Close tab"), new MenuItem("Close other tabs"), new MenuItem("Reopen closed tab"),
                                         new MenuItem("Bookmark all tabs"));

        //region binding web title + change tooltip
        TabPaneController tabPaneController = (TabPaneController) ReferencableManager.getInstance().get(TabPaneController.FXMLPATH);
        if (!tabPaneController.getCurrentTab().getText().equals("+") || !tabPaneController.getCurrentTab().getText().equals("history")){
            tabPaneController.getCurrentTab().textProperty().bind(webEngine.titleProperty());
        }
        webEngine.titleProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                tabPaneController.changeTooltip(webEngine.getTitle());
            }
        });
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

    private void onWebPageChanged(WebEvent<String> event) {

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
        String address = addressBar.getText();
        if(address.contains("www") && !address.contains("http")){
            webEngine.load("https://" + address);
        }
        else {
            webEngine.load(httpHeader + address);
        }
    }
    private void LoadWithContextMenu(String url){
        addressBar.setFocusTraversable(false);
        if(url.contains("www") && !url.contains("http")){
            webEngine.load("https://" + url);
        }
        else if(!url.contains("www") && !url.contains("http")){
            webEngine.load(httpHeader + url);
        }
        else {
            webEngine.load(url);
        }
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

           Thread thread = new Thread(new HTMLtoPDFHelper(webEngine.getLocation(), file));

           thread.start();
        }

    }

    private void printWebPage()
    {
        Thread thread = new Thread((new PrintingHelper(webEngine)));
        thread.start();
    }

    private void addHistoryTab(){
        TabPaneController tabPaneController = (TabPaneController)ReferencableManager.getInstance().get(TabPaneController.FXMLPATH);
        tabPaneController.addNewTab(true);
        popup.hide();
    }

    private void addInfoTab(){
        TabPaneController tabPaneController = (TabPaneController) ReferencableManager.getInstance().get(TabPaneController.FXMLPATH);
        tabPaneController.addNewInfoTab();
        popup.hide();
    }

    public WebEngine getWebEngine() {
        return webEngine;
    }

    private String getHostName(String urlInput) {
        try {
            URL url = new URL(urlInput);
            return url.getProtocol() + "://" + url.getHost() + "/";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void populateContextMenu(){
        contextMenu.getItems().clear();
        String url = addressBar.getText();
        contextMenu.getItems().add(new MenuItem(url));
        contextMenu.getItems()
                .addAll(WebProposal.getInstance(true).getWeb_proposal()
                        .stream().filter(string -> string.toLowerCase().contains(url.toLowerCase()))
                        .limit(5).map(MenuItem::new).collect(Collectors.toList()));
        contextMenu.getItems().forEach(item -> item.setOnAction(e->{
            addressBar.setText(item.getText().split("-")[0]);
            //addressBar.positionCaret(addressBar.getLength());
            LoadWithContextMenu(item.getText().split("-")[0]);
        }));

    }

    @Override
    public Object getController() {
        return this;
    }

    @Override
    public String getID() {
        return FXMLPATH;
    }
}
