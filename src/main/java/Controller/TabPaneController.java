package Controller;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TabPaneController implements Initializable{

    public static final String FXMLPATH = "/View/tabpane.fxml";


    @FXML
    private TabPane tabPane;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

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
            tab.setContent(FXMLLoader.load(getClass().getResource(TabContentController.FXMLPATH)));

            tabPane.getTabs().add(tabPane.getTabs().size()-1,tab);
            tabPane.getSelectionModel().select(tab);

            //Google.com by default
            tab.setText("Google.com");

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

    public TabPane getTabPane() {
        return tabPane;
    }
}
