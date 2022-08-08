package com.gil.connect_four.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Client extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("fxml/client.fxml"));
        Parent root = loader.load();
        ClientController cont = loader.getController();
        stage.setOnCloseRequest((e) -> cont.terminate());
        cont.startClient();

        Scene scene = new Scene(root);
        stage.setScene(scene);

        stage.setTitle("Connect-4");
        stage.show();
    }
}