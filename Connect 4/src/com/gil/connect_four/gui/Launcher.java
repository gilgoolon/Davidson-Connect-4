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

public abstract class Launcher extends Application {
    public static void main(String[] args) {
        launch(args); // fxml function - launch application
    }

    protected void start(Stage stage, String pathToFXML, String title) throws IOException {
        // load the FXML file and create an instance controller class
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(pathToFXML));
        Parent root = loader.load();
        BaseController controller = loader.getController();

        Scene scene = new Scene(root);
        stage.setScene(scene);

        setProperties(stage, controller);

        // set the title and finally show the application window
        stage.setTitle(title);
        stage.show();
        controller.init();
        Platform.runLater(controller::execute);
    }

    private void setProperties(Stage stage, BaseController controller){
        // add listeners for window resize
        stage.maximizedProperty().addListener((a,b,c) -> Platform.runLater(controller::init));
        stage.fullScreenProperty().addListener((a,b,c) -> Platform.runLater(controller::init));
        stage.widthProperty().addListener((a,b,c) -> controller.init());
        stage.heightProperty().addListener((a,b,c) -> controller.init());

        // set fullscreen toggle button to F11
        KeyCombination fullScreenCombo = new KeyCodeCombination(KeyCode.F11);
        stage.addEventHandler(KeyEvent.KEY_PRESSED, (event) -> {
            if (fullScreenCombo.match(event))
                stage.setFullScreen(!stage.isFullScreen());
        });

        stage.setFullScreenExitHint("Press F11 to toggle fullscreen mode");
        stage.setOnCloseRequest((e) -> controller.terminate());
    }
}
