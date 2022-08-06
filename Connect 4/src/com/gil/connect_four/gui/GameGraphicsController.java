package com.gil.connect_four.gui;

import com.gil.connect_four.logic.Game;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

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
    protected static final double imaginaryCircleOpc = 1;

    private Game game;
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
        double padding =  5.0;
        double radius = (Math.min(width/Game.COLS,height/Game.ROWS))/2.0-padding;
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
            imaginaryCircle.setDisable(false);
            imaginaryCircle.setCenterX(newCol*xLeg + xLeg/2.0);
            imaginaryCircle.setCenterY((Game.ROWS-1-game.firstEmpty(newCol))*yLeg + yLeg/2.0);
        } else imaginaryCircle.setDisable(true);
    }

    @FXML
    void mousePressedBoard(MouseEvent event){

    }
}
