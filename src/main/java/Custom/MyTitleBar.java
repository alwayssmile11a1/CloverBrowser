package Custom;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.stage.Window;

public class MyTitleBar extends FlowPane {
    Image imgOn, imgOff, imgmaximize, imgMaximizeHover;
    ImageView imvClose, imvMaximize;
    boolean isMaximize = false;


    boolean isPressed = false;

    public MyTitleBar(){
        setAlignment(Pos.TOP_RIGHT);
        setStyle("-fx-background-color:#FFFFFF");
        //region image
        imgOn = new Image(getClass().getResourceAsStream("../drawable/icons8-close-window-50.png"));
        imgOff = new Image(getClass().getResourceAsStream("../drawable/icons8-close-window-50 - Copy.png"));
        imgmaximize = new Image((getClass().getResourceAsStream("../Drawable/icons8-full-screen-50.png")));
        imgMaximizeHover = new Image(getClass().getResourceAsStream("../Drawable/icons8-full-screen-50-hover.png"));
        //endregion

        //region image maximize
        imvMaximize = new ImageView(imgmaximize);
        getChildren().add(imvMaximize);
        imvMaximize.getStyleClass().add("image");
        imvMaximize.setOnMouseClicked(e->{
            Stage stage = (Stage) getScene().getWindow();
            isMaximize = !isMaximize;
            stage.setMaximized(isMaximize);
        });
        imvMaximize.setOnMouseEntered(e ->{
            imvMaximize.setImage(imgMaximizeHover);
        });
        imvMaximize.setOnMouseExited(e -> imvMaximize.setImage(imgmaximize));
        // some thing is really wrong here
        // nếu không đổi hình khác trong hàm setOnMouseEntered thì nó ko gọi hàm setOnMouseClicked
        // đậu
        //endregion

        // region image close
        imvClose = new ImageView(imgOn);
        getChildren().add(imvClose);
        imvClose.getStyleClass().add("image");
        imvClose.setOnMouseClicked(e -> {
            System.out.print("exit");
            Stage stage = (Stage) getScene().getWindow();
            stage.close();
        });
        imvClose.setOnMouseEntered(e ->{
            imvClose.setImage(imgOff);
        });
        imvClose.setOnMouseExited(e -> imvClose.setImage(imgOn));
        //endregion

        //region mouse event
        setOnMousePressed(this::OnMousePressed);
        setOnMouseDragged(this::OnMouseDrag);
        setOnMouseReleased(this::OnMouseRelease);
        //endregion
    }

    public void OnMousePressed(MouseEvent e){
        isPressed = true;
        Window window = getScene().getWindow();
        offset = new Point2D(e.getScreenX() - window.getX(), e.getScreenY() - window.getY());
    }
    Point2D offset;
    public void OnMouseDrag(MouseEvent e){
        if (isPressed){
            Window window = getScene().getWindow();
            window.setX(e.getScreenX() - offset.getX());
            window.setY(e.getScreenY() - offset.getY());
        }
    }

    public void OnMouseRelease(MouseEvent e){
        isPressed = false;
    }
}
