package com.gil.connect_four.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

        // set the title and finally show the application window
        stage.setTitle("Connect-4 (SinglePlayer)");
        stage.show();
        stage.setFullScreen(true);
        stage.setResizable(false);
        controller.init();
        stage.widthProperty().addListener((obs,oldVal,newVal) -> {
            controller.init();
        });

        stage.heightProperty().addListener((obs,oldVal,newVal) -> {
            controller.init();
        });
    }
}
