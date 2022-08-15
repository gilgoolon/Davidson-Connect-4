package com.gil.connect_four.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Server extends Application {
    public static void main(String[] args) {
        launch(args); // fxml function - launch application
    }

    public void start(Stage stage) throws IOException {
        // load the FXML file and create an instance controller class
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("fxml/server.fxml"));
        Parent root = loader.load();
        ServerController cont = loader.getController();
        stage.setOnCloseRequest((e) -> cont.terminate());

        Scene scene = new Scene(root);
        stage.setScene(scene);

        // set the title and finally show the application window
        stage.setTitle("Connect-4 (Server)");
        stage.show();
        Platform.runLater(cont::execute);
    }
}