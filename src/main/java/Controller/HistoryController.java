package Controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXRippler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

public class HistoryController implements Initializable {
    public static final String FXMLPATH = "/View/History.fxml";

    @FXML
    JFXListView<Label> lsvHistory;
    @FXML
    JFXButton btnExpand;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> items = FXCollections.observableArrayList (
                "Single", "Double", "Suite", "Family App");
        //lsvHistory.setItems(items);
        for (int i = 0 ; i < 5; i++){
            Label lb = new Label();
            lb.setText("asdsdasdasd");
            lsvHistory.getItems().add(lb);
        }


        btnExpand.setOnMouseClicked(e->{
            if(!lsvHistory.isExpanded()){
                lsvHistory.setExpanded(true);
                lsvHistory.depthProperty().set(5);
            }
            else {
                lsvHistory.setExpanded(false);
                lsvHistory.depthProperty().set(0);
            }
        });
    }
}
