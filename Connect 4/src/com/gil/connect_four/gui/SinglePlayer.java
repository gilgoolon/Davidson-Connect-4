package com.gil.connect_four.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Stage;

import java.io.IOException;

public class SinglePlayer extends Application {
    public static void main(String[] args) {
        launch(args); // fxml function - launch application
    }

    public void start(Stage stage) throws IOException {
        // load the FXML file and create an instance controller class
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("fxml/single-player.fxml"));
        Parent root = loader.load();
        SinglePlayerController controller = loader.getController();

        Scene scene = new Scene(root);
        stage.setScene(scene);

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
        // set the title and finally show the application window
        stage.setTitle("Connect-4 (SinglePlayer)");
        stage.show();
        controller.init();
        Platform.runLater(controller::execute);
    }
}
