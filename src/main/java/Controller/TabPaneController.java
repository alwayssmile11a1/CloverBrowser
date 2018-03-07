package Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TabPaneController implements Initializable{

    public static final String FXMLPATH = "/View/tabpane.fxml";

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private TabPane tabPane;


    //FXMLLoader fxmlLoader;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //fxmlLoader = new FXMLLoader(getClass().getResource(TabContentController.FXMLPATH));

        addNewTab();

    }

    //add a new tab
    private void addNewTab()
    {
        try {
            //Create a new tab
            Tab tab = new Tab();

            //Set content to tabcontent.fxml
            //Note that in the tabcontent.fxml file, if you have maxHeight="-Infinity" and maxWidth="-Infinity",
            //it will prevent your tabcontent.fxml to fill the entire tab
            tab.setContent(FXMLLoader.load(getClass().getResource(TabContentController.FXMLPATH)));

            //
            tab.setText("Google.com");

            tabPane.getTabs().add(tabPane.getTabs().size()-1,tab);
            tabPane.getSelectionModel().select(tab);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }






}
