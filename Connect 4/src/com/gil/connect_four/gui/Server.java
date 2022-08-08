package com.gil.connect_four.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Server extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("fxml/server.fxml"));
        Parent root = loader.load();
        ServerController cont = loader.getController();
        stage.setOnCloseRequest((e) -> cont.terminate());
        cont.execute(); // start the server

        Scene scene = new Scene(root);
        stage.setScene(scene);

        stage.setTitle("Connect-4 (Server)");
        stage.show();
    }
}
