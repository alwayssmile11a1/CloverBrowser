package Controller;

import Model.Download.DownloadHelper;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DownloadProgressButtonController implements Initializable{

    public static final String FXMLPATH = "/View/DownloadProgressButton.fxml";

    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Button progressButton;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        progressButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                try {
                    Desktop.getDesktop().open(new File(DownloadHelper.downloadFolder()));
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });


    }


    public ProgressIndicator getProgressIndicator()
    {
        return progressIndicator;
    }

    public Button getProgressButton()
    {
        return progressButton;
    }


}
