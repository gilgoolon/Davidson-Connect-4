package com.gil.connect_four.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
        ClientController cont = loader.getController();


        Scene scene = new Scene(root);
        stage.setScene(scene);

        // set the title and finally show the application window
        stage.setTitle("Connect-4 (Client)");
        stage.show();
        stage.setResizable(false);
        ((ClientController)loader.getController()).init();
        stage.setOnCloseRequest((e) -> cont.terminate());
        cont.startClient();
    }
}
