package com.gil.connect_four.gui;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;

public class Choose extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        // Ask the user weather to play multiplayer or single-player and then start
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Connect 4");
        alert.setContentText("Which version would you like to play ?");
        ButtonType multiplayerButton = new ButtonType("Multiplayer", ButtonBar.ButtonData.YES);
        ButtonType single_playerButton = new ButtonType("Single-player", ButtonBar.ButtonData.NO);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(multiplayerButton, single_playerButton, cancelButton);
        alert.showAndWait().ifPresent(type -> {
            if (type == multiplayerButton){
                try {
                    new Client().start(stage);
                } catch(IOException e){
                    e.printStackTrace();
                }
            } else if (type == single_playerButton) {
                try {
                    new SinglePlayer().start(stage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
