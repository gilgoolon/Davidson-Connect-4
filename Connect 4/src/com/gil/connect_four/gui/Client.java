package com.gil.connect_four.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class Client extends Application {
    public static void main(String[] args) {
        launch(args); // fxml function - launch application
    }

    public void start(Stage stage) throws IOException {
        // load the FXML file and create an instance controller class
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("fxml/client.fxml"));
        Parent root = loader.load();
        ClientController controller = loader.getController();


        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(true);
        // add listeners for window resize
        stage.maximizedProperty().addListener((a,b,c) ->{
            Platform.runLater(controller::init);
        });
        stage.fullScreenProperty().addListener((a,b,c) ->{
            Platform.runLater(controller::init);
        });
        stage.widthProperty().addListener((a,b,c) ->{
            controller.init();
        });
        stage.heightProperty().addListener((a,b,c) ->{
            controller.init();
        });
        KeyCombination fullScreenCombo = new KeyCodeCombination(KeyCode.F11);
        stage.addEventHandler(KeyEvent.KEY_PRESSED, (event) -> {
            if (fullScreenCombo.match(event)){
                stage.setFullScreen(!stage.isFullScreen());
            }
        });
        stage.setFullScreenExitHint("Press F11 to toggle fullscreen mode");

        // set the title and finally show the application window
        stage.setTitle("Connect-4 (Client)");
        stage.show();
        ((ClientController)loader.getController()).init();
        stage.setOnCloseRequest((e) -> controller.terminate());
        controller.startClient();
    }
}
