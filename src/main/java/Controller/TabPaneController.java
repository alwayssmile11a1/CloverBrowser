package Controller;

import Model.ReferencableInterface.IReferencable;
import Model.ReferencableInterface.ReferencableManager;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPopup;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.awt.print.Book;
import java.io.IOException;
import java.net.URL;
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
    public Tab addNewTab()
    {
        try {
            //Create a new tab
            Tab tab = new Tab();

            //Set content to tabcontent.fxml
            //Note that in the tabcontent.fxml file, if you have maxHeight="-Infinity" and maxWidth="-Infinity",
            //it will prevent your tabcontent.fxml to fill the entire tab
                TabContentController.loadDefault=true;

                tab.setContent(FXMLLoader.load(getClass().getResource(TabContentController.FXMLPATH)));
                //Google.com by default
                tab.setText("Google.com");
                tab.setTooltip(new Tooltip("Google.com"));

            tabPane.getTabs().add(tabPane.getTabs().size()-1,tab);
            tabPane.getSelectionModel().select(tab);
            return tab;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Tab addNewTab(String url)
    {
        try {
            //Create a new tab
            Tab tab = new Tab();

            //Set content to tabcontent.fxml
            //Note that in the tabcontent.fxml file, if you have maxHeight="-Infinity" and maxWidth="-Infinity",
            //it will prevent your tabcontent.fxml to fill the entire tab
            TabContentController.loadDefault=false;
            TabContentController.link = url.substring(4);
            tab.setContent(FXMLLoader.load(getClass().getResource(TabContentController.FXMLPATH)));
            //Google.com by default
            //tab.setText("Google.com");
            //tab.setTooltip(new Tooltip("Google.com"));

            tabPane.getTabs().add(tabPane.getTabs().size()-1,tab);
            tabPane.getSelectionModel().select(tab);
            return tab;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
    public Tab addNewTab(boolean addHistory)
    {
        try {
            //Create a new tab
            Tab tab = new Tab();

            //Set content to tabcontent.fxml
            //Note that in the tabcontent.fxml file, if you have maxHeight="-Infinity" and maxWidth="-Infinity",
            //it will prevent your tabcontent.fxml to fill the entire tab
                tab.setContent(FXMLLoader.load(getClass().getResource(HistoryController.FXMLPATH)));
                tab.setText("History");

            tabPane.getTabs().add(tabPane.getTabs().size()-1,tab);
            tabPane.getSelectionModel().select(tab);
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

    public void changeTabText(String text){
        tabPane.getSelectionModel().getSelectedItem().setText(text);
        tabPane.getSelectionModel().getSelectedItem().setTooltip(new Tooltip(text));
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
