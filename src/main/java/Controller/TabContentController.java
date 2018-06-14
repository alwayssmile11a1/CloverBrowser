package Controller;

import Application.Main;
import Model.Download.DownloadHelper;
import Model.HTMLtoPDF.HTMLtoPDFHelper;
import Model.MonthToNum.MonthToNum;
import Model.Printing.PrintingHelper;
import Model.ReferencableInterface.IReferencable;
import Model.ReferencableInterface.ReferencableManager;
import Model.SqliteDatabase.SQLiteDatabase;
import Model.SqliteDatabase.SqliteDatabaseBookmarks;
import Model.WebProposal.WebProposal;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPopup;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import com.sun.istack.internal.NotNull;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.stage.PopupWindow;
import javafx.stage.WindowEvent;
import net.sf.image4j.codec.ico.ICODecoder;
import netscape.javascript.JSObject;
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
    private Button bookMarkButton;

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

    @FXML
    private HBox bookmarkTabsHBox;

    private String httpHeader = "https://";

    private JFXPopup popup;

    private JFXPopup bookMarkPopup;

    private final ContextMenu contextMenu = new ContextMenu();

    public static boolean loadDefault = true;

    public static String link="google.com";

    private ContextMenu webViewContextMenu;

    private ContextMenu tabContextMenu;
    //endregion

    private TextField nameTextField;

    private boolean urlChange = false;

    private String titleBookmark = "";

    private String oldTitle = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!ReferencableManager.getInstance().contain(this)) {
            ReferencableManager.getInstance().add(this);
        }

        //region getEngine + getHistory
        webEngine = webView.getEngine();

        webHistory = webEngine.getHistory();
        webHistory.getEntries().addListener(new ListChangeListener<WebHistory.Entry>() {
            @Override
            public void onChanged(Change<? extends WebHistory.Entry> c) {
                //int n = c.getAddedSize();
                ObservableList<WebHistory.Entry> listEntry = (ObservableList<WebHistory.Entry>)c.getList();
                String url = listEntry.get(listEntry.size()-1).getUrl();
                urlChange = true;
                addressBar.setText(url);
                contextMenu.hide();
                String title = webEngine.getTitle();
                String location = webEngine.getLocation();
                int a = 2;

                //
                //Kiểm tra url có trong bảng CSDL BOOKMARK không
                //Nếu có thì set image background của bookMarkButton màu hồng
                //Nếu không có thì set image background của bookMarkButton màu trắng
                //
                PreparedStatement preparedStatement = null;
                try {
                    preparedStatement = SqliteDatabaseBookmarks.getInstance().getConnection().prepareStatement("SELECT * FROM bookmarks WHERE url=?");
                    preparedStatement.setString(1, url);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()){
                        ChangeBookmarkImage(true);
                    }
                    else {
                        ChangeBookmarkImage(false);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                finally {
                    try {
                        preparedStatement.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        //endregion

        //region Thiết lập các tab bookmark trên tab content
        String title;
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = SqliteDatabaseBookmarks.getInstance().getConnection().prepareStatement("SELECT * FROM bookmarks");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                //Nếu có thì select tên bookmark từ CSDL lên theo url
                title = resultSet.getString("title");

                AddBookmarkButtonInTab(title);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        //endregion

        //region bookMarkButton - Thiết lập các thành phần, sự kiện của thành phần
        bookMarkPopup = new JFXPopup();

        JFXButton closeButton = new JFXButton("X");
        closeButton.setStyle("-fx-font: 15 arial;");
        closeButton.setPrefSize(20,20);
        HBox closeHBox = new HBox();
        closeHBox.getChildren().add(closeButton);
        closeHBox.setMargin(closeButton, new Insets(5, 0, 0, 300));

        Label titleLabel = new Label("Đã thêm dấu trang");
        titleLabel.setStyle("-fx-font: 20 arial;");
        HBox titleHBox = new HBox();
        titleHBox.getChildren().add(titleLabel);
        titleHBox.setMargin(titleLabel, new Insets(0, 0, 0, 10));

        Label nameLabel = new Label("Tên");
        nameLabel.setStyle("-fx-font: 15 arial;");
        nameTextField = new TextField();
        nameTextField.setPrefSize(250, 30);
        HBox nameHBox = new HBox();
        nameHBox.getChildren().addAll(nameLabel, nameTextField);
        nameHBox.setMargin(nameLabel, new Insets(25, 0, 0, 10));
        nameHBox.setMargin(nameTextField, new Insets(20, 10, 0, 30));

        JFXButton completeButton = new JFXButton("Hoàn tất");
        completeButton.setStyle("-fx-font: 14 arial; -fx-background-color: #ed577f; -fx-text-fill: #ffffff");
        completeButton.setPrefSize(80, 30);
        JFXButton deleteButton = new JFXButton("Xóa");
        deleteButton.setStyle("-fx-font: 14 arial; -fx-border-color: #d6cfd1; -fx-border-width: 1px;");
        deleteButton.setPrefSize(80, 30);

        HBox buttonHBox = new HBox();
        buttonHBox.getChildren().addAll(completeButton, deleteButton);
        buttonHBox.setMargin(completeButton, new Insets(20, 0, 20, 140));
        buttonHBox.setMargin(deleteButton, new Insets(20, 10, 20, 10));

        VBox bookMarkVBox = new VBox();
        bookMarkVBox.getChildren().addAll(closeHBox, titleHBox, nameHBox, buttonHBox);
        bookMarkPopup.setPopupContent(bookMarkVBox);


        //
        //Sự kiện của completeButton
        //Sửa tên bookmark thành text của nameTextField trong CSDL theo url
        //
        completeButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                String url = addressBar.getText();
                String newTitle = nameTextField.getText();

                PreparedStatement preparedStatement = null;
                try {
                    preparedStatement = SqliteDatabaseBookmarks.getInstance().getConnection().prepareStatement("SELECT * FROM bookmarks WHERE url=?");
                    preparedStatement.setString(1, url);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        oldTitle = resultSet.getString("title");
                    }

                    preparedStatement = SqliteDatabaseBookmarks.getInstance().getConnection().prepareStatement("UPDATE bookmarks SET title = ? WHERE url = ?");
                    preparedStatement.setString(1, newTitle);
                    preparedStatement.setString(2, url);
                    preparedStatement.executeUpdate();

                    bookmarkTabsHBox.getChildren().forEach(e->{
                        if(((Button) e).getText().equals(oldTitle)){
                            ((Button) e).setText(newTitle);
                        }
                    });

                } catch (SQLException e) {
                    e.printStackTrace();
                }
                finally {
                    try {
                        preparedStatement.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                bookMarkPopup.hide();
            }
        });

        //
        //Sự kiện của deleteButton
        //Xóa url của textfield trong CSDL
        //Đổi màu bookMarkButton thành màu trắng
        //
        deleteButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                String url = addressBar.getText();

                PreparedStatement preparedStatement = null;
                try {
                    preparedStatement = SqliteDatabaseBookmarks.getInstance().getConnection().prepareStatement("SELECT * FROM bookmarks WHERE url=?");
                    preparedStatement.setString(1, url);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        titleBookmark = resultSet.getString("title");
                    }

                    //Xóa bookmark theo url trong CSDL
                    preparedStatement = SqliteDatabaseBookmarks.getInstance().getConnection().prepareStatement("DELETE FROM bookmarks WHERE url=?");
                    preparedStatement.setString(1, url);
                    preparedStatement.executeUpdate();

                } catch (SQLException e) {
                    System.err.println(e.getMessage());
                } finally {
                    SqliteDatabaseBookmarks.getInstance().Disconnect();
                }

                ChangeBookmarkImage(false);

                DeleteBookmarkButtonInTab(titleBookmark);

                bookMarkPopup.hide();
            }
        });


        closeButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                bookMarkPopup.hide();
            }
        });

        bookMarkButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                bookMarkPopup.show(bookMarkButton, JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.RIGHT, event.getX() - 20, event.getY() + 20);

                ChangeBookmarkImage(true);

                String url = addressBar.getText();
                String title = webEngine.getTitle();

                PreparedStatement preparedStatement = null;
                try {
                    preparedStatement = SqliteDatabaseBookmarks.getInstance().getConnection().prepareStatement("SELECT * FROM bookmarks WHERE url=?");
                    preparedStatement.setString(1, url);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()){
                        //Nếu có
                        //Select tên bookmark từ CSDL lên theo url
                        nameTextField.setText(resultSet.getString("title"));
                    }
                    else {
                        //Ngược lại
                        //Thêm bookmark mới vào CSDL
                        preparedStatement = SqliteDatabaseBookmarks.getInstance().getConnection().prepareStatement("INSERT INTO 'bookmarks' values (?,?)");
                        preparedStatement.setString(1, title);
                        preparedStatement.setString(2, url);
                        preparedStatement.executeUpdate();
                        nameTextField.setText(title);

                        AddBookmarkButtonInTab(title);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                finally {
                    try {
                        preparedStatement.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
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
                }
                else {
                    if (newValue == State.FAILED) {
                        String url = getClass().getResource("/View/errorpage.html").toExternalForm();
                        webEngine.load(url);
                    }
                }
            }
        });
        worker.workDoneProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                String message = worker.getMessage();
                State s2 = worker.getState();
                double s3 = worker.getProgress();
                Object s4 = worker.getValue();
                double s5 = worker.getTotalWork();
                String s6 = worker.getTitle();
                boolean s7 = worker.isRunning();
                double s8 = worker.getWorkDone();
                String location = webEngine.getLocation();
                int a = 2;
                if(message.equals("Loading complete") && urlChange){
                    urlChange = false;
                    addressBar.setText(webEngine.getLocation());
                    contextMenu.hide();
                    //region store history
                    System.out.println("Finish!");
                    try {
                        String title = webEngine.getTitle();
                        //TabPaneController tabPaneController = (TabPaneController) (ReferencableManager.getInstance().get(TabPaneController.FXMLPATH));
                        progressLoad.setVisible(false);
                        //region add to file history
                        int i = webHistory.getEntries().size() - 1;
                        WebHistory.Entry entry = webHistory.getEntries().get(i);
                        //String url = entry.getUrl();
                        String url = webEngine.getLocation();
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        Date d = new Date();
                        String datetime = d.toString();

                        String[] temp = datetime.split(" ");
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
                        //endregion

                        //region add history to DB
                        //region add to DB
                        PreparedStatement preparedStatement = SQLiteDatabase.getInstance().getConnection().prepareStatement("INSERT INTO "+ SQLiteDatabase.getInstance().getTableName() +" values (?,?,?,?,?)");
                        preparedStatement.setString(1, url);
                        preparedStatement.setString(2, date);
                        preparedStatement.setString(3, time);
                        preparedStatement.setString(4, title);
                        preparedStatement.setString(5, domain);
                        preparedStatement.executeUpdate();
                        //endregion
                        //endregion



                    }
                    catch (Exception e){
                        System.out.println("tab content - worker changed");
                        System.err.println(e.getMessage());
                        e.printStackTrace();
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
                popup.show(taskButton, JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.RIGHT, event.getX() - 20, event.getY());
            }
        });


        toPDFButton.setOnMouseClicked(event -> convertToPDF());
        printButton.setOnMouseClicked(event -> printWebPage());
        historyButton.setOnMouseClicked(e->addHistoryTab());
        bookmarkButton.setOnMouseClicked(e->addBookmarksTab());
        infoButton.setOnMouseClicked(e->addInfoTab());
        //endregion

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

        //download
        webEngine.locationProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                System.out.println("location of engine: " + newValue);
                //Check can download
                HttpURLConnection connection = DownloadHelper.isDownloadable(webEngine.getLocation());
                if(connection!=null)
                {
                    try {
                        FXMLLoader fxmlLoader = new FXMLLoader();
                        fxmlLoader.load(getClass().getResource("/View/DownloadProgressButton.fxml").openStream());
                        DownloadProgressButtonController downloadController = fxmlLoader.getController();

                        Button downloadProgressButton = downloadController.getProgressButton();
                        downloadProgressButton.setText(webEngine.getTitle());
                        ProgressIndicator indicator =  downloadController.getProgressIndicator();



                        tabPaneController.getDownLoadPane().getChildren().add(downloadProgressButton);
                        tabPaneController.setDownloadPaneVisible(true);

                        DownloadHelper.startDownload(connection, webEngine.getTitle(), indicator);


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
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
            urlChange = true;
        });
    }

    private void OnForwardButtonClicked(MouseEvent e) {
        Platform.runLater(() -> {
            webEngine.executeScript("history.forward()");
            urlChange = true;
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
        if(!address.contains("http")){
            webEngine.load(httpHeader + address);
        }
        else {
            webEngine.load(address);
        }
    }
    private void LoadWithContextMenu(String url){
        addressBar.setFocusTraversable(false);
        /*if(url.contains("www") && !url.contains("http")){
            webEngine.load("https://" + url);
        }
        else if(!url.contains("www") && !url.contains("http")){
            try {
                webEngine.load(httpHeader + url);
            }
            catch (Exception e){
                webEngine.load(url);
            }
        }
        else {
            webEngine.load(url);
        }*/
        if(!url.contains("http")){
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
        tabPaneController.addNewTab(1);
        popup.hide();
    }

    private void addBookmarksTab(){
        TabPaneController tabPaneController = (TabPaneController)ReferencableManager.getInstance().get(TabPaneController.FXMLPATH);
        tabPaneController.addNewTab(2);
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

    private void AddBookmarkButtonInTab(String buttonText){
        Button insertBookmark = new Button();
        insertBookmark.setText(buttonText);
        insertBookmark.setPrefWidth(100);
        insertBookmark.setStyle("-fx-background-color: #ffd3e4");
        bookmarkTabsHBox.getChildren().add(insertBookmark);
        bookmarkTabsHBox.setMargin(insertBookmark, new Insets(0, 0, 0, 5));
        //
        //Tạo sự kiện khi click chuột thì
        //Tìm trong CSDL url có title == insertBookmark.getText()
        //Load lại tab hiện tại theo url tìm được
        //
        insertBookmark.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                String title = insertBookmark.getText();

                PreparedStatement preparedStatement = null;
                try {
                    preparedStatement = SqliteDatabaseBookmarks.getInstance().getConnection().prepareStatement("SELECT * FROM bookmarks WHERE title=?");
                    preparedStatement.setString(1, title);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        //Nếu có thì select tên bookmark từ CSDL lên theo url
                        String url = resultSet.getString("url");

                        //Load lại tab theo url
                        webEngine.load(url);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        preparedStatement.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void ChangeBookmarkImage(boolean b){
        Image image;
        if (b == true){
            //Đổi màu hồng
            image = (new Image("../resources/Drawable/icons8-bookmark-25.png"));
        }
        else{
            //Đổi màu trắng
            image = (new Image("../resources/Drawable/icons8-bookmark-25 (1).png"));
        }

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        bookMarkButton.setGraphic(imageView);
    }

    public void DeleteBookmarkButtonInTab(String buttonText){
        bookmarkTabsHBox.getChildren().removeIf(e->{
            if(((Button) e).getText().equals(buttonText))
                return true;
            return false;
        });
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
