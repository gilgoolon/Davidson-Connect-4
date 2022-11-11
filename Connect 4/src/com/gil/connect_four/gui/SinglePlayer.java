package com.gil.connect_four.gui;

import javafx.stage.Stage;

import java.io.IOException;

public class SinglePlayer extends Launcher {
    public static void main(String[] args) {
        launch(args); // fxml function - launch application
    }

    public void start(Stage stage) throws IOException {
        super.start(stage, "fxml/single-player.fxml", "Connect-4 (SinglePlayer)");
    }
}