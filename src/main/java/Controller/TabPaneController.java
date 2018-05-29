package Controller;

import Model.ReferencableInterface.IReferencable;
import Model.ReferencableInterface.ReferencableManager;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPopup;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.web.WebEngine;
import net.sf.image4j.codec.ico.ICODecoder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.Book;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class TabPaneController implements Initializable, IReferencable{

    public static final String FXMLPATH = "/View/tabpane.fxml";


    @FXML
    private TabPane tabPane;

    JFXButton Bookmarks;
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //IMPORTANT: add this to ReferencableManager to be able to access this class later
        ReferencableManager.getInstance().add(this);

        addNewTab();

        //
        tabPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(tabPane.getSelectionModel().isSelected(tabPane.getTabs().size()-1))
                {
                    onAddNewTabButtonClicked();
                }
            }
        });
    }

    //add a new tab
    public Tab addNewTab() {
        try {
            //Create a new tab
            Tab tab = new Tab();
            ImageView favicon = new ImageView(new Image("../resources/Drawable/loading.gif"));
            favicon.setFitWidth(25);
            favicon.setFitHeight(25);
            tab.setGraphic(favicon);
            //Set content to tabcontent.fxml
            //Note that in the tabcontent.fxml file, if you have maxHeight="-Infinity" and maxWidth="-Infinity",
            //it will prevent your tabcontent.fxml to fill the entire tab
                //Google.com by default
            tab.setText("Google.com");
            tab.setTooltip(new Tooltip("Google.com"));
            tabPane.getTabs().add(tabPane.getTabs().size()-1,tab);
            tabPane.getSelectionModel().select(tab);
            TabContentController.loadDefault=true;
            tab.setContent(FXMLLoader.load(getClass().getResource(TabContentController.FXMLPATH)));
            //tab.setContent(FXMLLoader.load(getClass().getResource(CloverInfoController.FXMLPATH)));
            return tab;

        } catch (IOException e) {
            System.err.println("add new tab - TabPaneController");
            e.printStackTrace();
        }
        return null;
    }
    public Tab addNewTab(String url){
        try {
            //Create a new tab
            Tab tab = new Tab();
            ImageView favicon = new ImageView(new Image("../resources/Drawable/loading.gif"));
            favicon.setFitWidth(25);
            favicon.setFitHeight(25);
            tab.setGraphic(favicon);
            //Set content to tabcontent.fxml
            //Note that in the tabcontent.fxml file, if you have maxHeight="-Infinity" and maxWidth="-Infinity",
            //it will prevent your tabcontent.fxml to fill the entire tab
            //Google.com by default
            tab.setText("Google.com");
            tabPane.getTabs().add(tabPane.getTabs().size()-1,tab);
            tabPane.getSelectionModel().select(tab);
            TabContentController.loadDefault=false;
            TabContentController.link=url;
            tab.setContent(FXMLLoader.load(getClass().getResource(TabContentController.FXMLPATH)));
            return tab;

        } catch (IOException e) {
            System.out.println("add new tab url - TabPaneController");
            e.printStackTrace();
        }

        return null;
    }
    public Tab addNewTab(boolean addHistory){
        try {
            //Create a new tab
            Tab tab = new Tab();

            //Set content to tabcontent.fxml
            //Note that in the tabcontent.fxml file, if you have maxHeight="-Infinity" and maxWidth="-Infinity",
            //it will prevent your tabcontent.fxml to fill the entire tab
            tabPane.getTabs().add(tabPane.getTabs().size()-1,tab);
            tabPane.getSelectionModel().select(tab);
            tab.setContent(FXMLLoader.load(getClass().getResource(HistoryController.FXMLPATH)));
            tab.setText("History");
            return tab;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
    public Tab addNewInfoTab(){
        try {
            //Create a new tab
            Tab tab = new Tab();

            //Set content to tabcontent.fxml
            //Note that in the tabcontent.fxml file, if you have maxHeight="-Infinity" and maxWidth="-Infinity",
            //it will prevent your tabcontent.fxml to fill the entire tab
            tabPane.getTabs().add(tabPane.getTabs().size()-1,tab);
            tabPane.getSelectionModel().select(tab);
            tab.setContent(FXMLLoader.load(getClass().getResource("/View/CloverInfo.fxml")));
            tab.setText("About us");
            return tab;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void onAddNewTabButtonClicked()
    {
        addNewTab();
    }

    public void changeTooltip(String text){
        tabPane.getSelectionModel().getSelectedItem().setTooltip(new Tooltip(text));
    }

    public TabPane getTabPane() {
        return tabPane;
    }

    public Tab getCurrentTab(){
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        return tab;
    }

    public void setFavicon(String favIconFullURL){
        ImageView favIconImageView = new ImageView();
        favIconImageView.setFitWidth(20);
        favIconImageView.setFitHeight(20);
        try {
            //Create HttpURLConnection
            HttpURLConnection httpcon = (HttpURLConnection) new URL(favIconFullURL).openConnection();
            httpcon.addRequestProperty("User-Agent", "Mozilla/5.0");
            List<BufferedImage> image = ICODecoder.read(httpcon.getInputStream());

            //Set the favicon
            favIconImageView.setImage(SwingFXUtils.toFXImage(image.get(0), null));
            tabPane.getSelectionModel().getSelectedItem().setGraphic(favIconImageView);

        } catch (Exception ex) {
            //ex.printStackTrace()
            favIconImageView.setImage(null);
        }
    }

    public void setFaviconLoading(){
        ImageView favicon = new ImageView(new Image("../resources/Drawable/loading.gif"));
        favicon.setFitWidth(25);
        favicon.setFitHeight(25);
        tabPane.getSelectionModel().getSelectedItem().setGraphic(favicon);
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
