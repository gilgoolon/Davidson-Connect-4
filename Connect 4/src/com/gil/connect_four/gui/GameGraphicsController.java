package com.gil.connect_four.gui;

import com.gil.connect_four.logic.Game;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class GameGraphicsController {

    @FXML
    private TextField _chatTextField;
    @FXML
    private Label _opponentLabel;
    @FXML
    private Pane gamePane;

    protected static double width;
    protected static double height;
    protected static double xLeg;
    protected static double yLeg;
    protected double padding;
    protected double radius;
    protected static final double imaginaryCircleOpc = 0.4;

    private final Game game;
    private int currentMouseCol;
    private Circle imaginaryCircle;

    public GameGraphicsController(){
        game = new Game();
        currentMouseCol = 0;
    }
    @FXML
    void initialize(){
        // initialize board variables
        width = gamePane.getPrefWidth();
        height = gamePane.getPrefHeight();
        xLeg = width/Game.COLS;
        yLeg = height/Game.ROWS;
        padding =  5.0;
        radius = (Math.min(width/Game.COLS,height/Game.ROWS))/2.0-padding;
        // draw white circles
        for (int x = 0; x < Game.COLS; x++){
            for (int y = 0; y < Game.ROWS; y++){
                Circle circle = new Circle(x*xLeg+xLeg/2.0,y*yLeg + yLeg/2.0,radius, Color.WHITE);
                gamePane.getChildren().add(circle);
            }
        }

        imaginaryCircle = new Circle(xLeg/2.0,(Game.ROWS-1)*yLeg+yLeg/2.0,radius,game.isRedToMove()?Color.RED:Color.YELLOW);
        imaginaryCircle.setOpacity(imaginaryCircleOpc);
        gamePane.getChildren().add(imaginaryCircle);
        imaginaryCircle.toFront();
    }
    @FXML
    void quitPressed(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    void sendChatMessage(ActionEvent event) {

    }

    @FXML
    void mouseMovedBoard(MouseEvent event){
        int newCol = (int)(event.getX()/xLeg);

        if (newCol == currentMouseCol)
            return;
        currentMouseCol = newCol;
        if (game.isFreeCol(currentMouseCol)){
            imaginaryCircle.setOpacity(imaginaryCircleOpc);
            imaginaryCircle.setCenterX(newCol*xLeg + xLeg/2.0);
            imaginaryCircle.setCenterY((Game.ROWS-1-game.firstEmpty(newCol))*yLeg + yLeg/2.0);
        } else {
            imaginaryCircle.setOpacity(0);
            System.out.println("hello");
        }
    }

    @FXML
    void mousePressedBoard(MouseEvent event){
        if (!game.isFreeCol(currentMouseCol))
            return;

        fallingAnimation(currentMouseCol, game.firstEmpty(currentMouseCol), game.isRedToMove());
        game.makeMove(currentMouseCol);
        if (game.isWin()){
            Alert a = new Alert(Alert.AlertType.INFORMATION, "Game Over !");
            a.setHeaderText((game.isRedToMove() ? "The yellow " : "The red ") + "player has won the game.");
            a.showAndWait();
        }
    }

    /**
     * Place a new circle at the passed index and animate it
     * @param col represents the column index to place the circle in
     * @param row represents the row index to place the circle in
     * @param redColor represents the color of circle to place
     */
    private void fallingAnimation(int col, int row, boolean redColor){
        Circle circle = new Circle(col*xLeg + xLeg/2.0, 0, radius,redColor ? Color.RED : Color.YELLOW);
        gamePane.getChildren().add(circle);
        circle.toFront();
        TranslateTransition trans = new TranslateTransition(Duration.millis(300), circle);
        trans.setFromY(circle.getLayoutX());
        trans.setToY((Game.ROWS-1-row)*yLeg + yLeg/2.0);
        trans.setAutoReverse(false);
        trans.setCycleCount(1);
        trans.setInterpolator(new Interpolator() {
            @Override
            protected double curve(double v) {
                return v*v*v; // v^2*g/2 - like physics equation x(t)=a/2*t^2+vt+h
            }
        });
        FadeTransition ft = new FadeTransition(Duration.millis(300), circle);
        ft.setAutoReverse(false);
        ft.setCycleCount(1);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setOnFinished((event) -> {
            // update imaginary circle
            Platform.runLater(()->{
                imaginaryCircle.setFill(game.isRedToMove() ? Color.RED : Color.YELLOW);
                if (Game.ROWS-1-row-1 >= 0)
                    imaginaryCircle.setCenterY((Game.ROWS-1-row-1)*yLeg + yLeg/2.0);
                else imaginaryCircle.setDisable(true);
            });
        });
        // play the transitions
        ft.play();
        trans.play();
        try {
            playSound("disc_fall.wav");
        } catch (Exception ignore){}

    }

    /**
     * Play a sound from a file
     * @param soundFile represents the filename of the file (with no path to assets)
     * @throws LineUnavailableException when can't play the sound
     * @throws IOException when the output was interrupted
     * @throws UnsupportedAudioFileException when the file isn't of the right type
     */
    private void playSound(String soundFile) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        File f = new File("./" + soundFile);
        AudioInputStream audioIn = AudioSystem.getAudioInputStream(getClass().getResourceAsStream("./assets/" + soundFile));
        Clip clip = AudioSystem.getClip();
        clip.open(audioIn);
        clip.start();
    }
}
