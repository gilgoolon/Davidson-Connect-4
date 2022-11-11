package com.gil.connect_four.gui;

import com.gil.connect_four.logic.Color;
import com.gil.connect_four.logic.Game;
import com.gil.connect_four.logic.GameStatus;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.*;
import java.io.IOException;
import java.util.Objects;

public abstract class BaseController {
    // GUI - FXML components
    @FXML
    protected Label _opponentLabel; // label to display the opponent's color
    @FXML
    protected Pane _gamePane; // pane to deal with disks - placing, animations, design and more...
    @FXML
    protected TextArea _chatTextArea; // text area to display messages for the user

    // GUI based constants
    protected static double WIDTH; // width of the board pane - constant and short
    protected static double HEIGHT; // height of the board pane - constant and short
    protected static double xLeg; // calculate the width of each cell (disk cell) using fxml components at runtime
    protected static double yLeg; // calculate the height of each cell (disk cell) using fxml components at runtime
    protected static double padding; // calculate the padding (how many pixels to reduce from the radius)
    protected static double radius; // calculate the radius based on cell height and width (minimum/2)

    // calculations/instance variables
    protected final Game game; // logical component - connect 4 game object
    protected Color myColor; // keep track of the user's color (set in the beginning)
    protected int currentMouseCol; // keep track of the current column the mouse is in
    protected Circle imaginaryCircle; // marks the next move (imaginary - dynamic) where the user has his their mouse on
    protected static final double imaginaryCircleOpc = 0.3; // opacity of the imaginary circle
    protected Clip playback; // sound playback - end when the game is over (to not intercept with win\lose sound effects)


    /**
     * Override default constructor for the controller class - initialize states and variables not related to the GUI.
     */
    public BaseController(){
        game = new Game();
        currentMouseCol = 0;
        myColor = Color.Empty;
    }

    /**
     * Set up the GUI using variable set in the FXML file.
     * Certain things cannot be accessed in the constructor like the pane height and width (because it is still null
     * in runtime of construction) and therefore need to wait for the setup of the GUI that occurs behind automatically
     * behind the scenes using some information from the FXML file.
     */
    public void init(){
        _gamePane.getChildren().clear();
        // initialize board variables
        WIDTH = _gamePane.getWidth();
        HEIGHT = _gamePane.getHeight();
        xLeg = WIDTH/Game.COLS;
        yLeg = HEIGHT/Game.ROWS;
        padding =  5.0;
        radius = (Math.min(xLeg,yLeg))/2.0-padding;
        // draw white circles
        for (int x = 0; x < Game.COLS; x++){
            for (int y = 0; y < Game.ROWS; y++){
                javafx.scene.paint.Color curr = game.get(Game.COLS-1-x,Game.ROWS-1-y) == Color.Red ? javafx.scene.paint.Color.RED : game.get(Game.COLS-1-x,Game.ROWS-1-y) == Color.Yellow ? javafx.scene.paint.Color.YELLOW : javafx.scene.paint.Color.WHITE;
                Circle circle = new Circle((Game.COLS-1-x)*xLeg+xLeg/2.0,y*yLeg + yLeg/2.0,radius, curr);
                _gamePane.getChildren().add(circle);
            }
        }

        createImaginaryCircle();
    }

    protected void createImaginaryCircle(){
        imaginaryCircle = new Circle(currentMouseCol*xLeg+xLeg/2.0,(Game.ROWS-1-game.firstEmpty(currentMouseCol))*yLeg+yLeg/2.0,radius,myColor == Color.Red ? javafx.scene.paint.Color.RED : javafx.scene.paint.Color.YELLOW);
        imaginaryCircle.setOpacity(imaginaryCircleOpc);
        if (myColor == Color.Red != game.isRedToMove())
            imaginaryCircle.setOpacity(0);
        _gamePane.getChildren().add(imaginaryCircle);
        imaginaryCircle.toFront();
    }

    /**
     * This function gets called when the user presses the "quit" button, terminates the program.
     */
    @FXML
    protected void quitPressed() {
        terminate();
    }

    @FXML
    protected void mouseMovedBoard(@NotNull MouseEvent event){
        int newCol = (int)(event.getX()/xLeg);

        if (newCol == currentMouseCol || game.isRedToMove() != (myColor == Color.Red) || newCol >= Game.COLS)
            return;

        currentMouseCol = newCol;

        updateImgCircle();
    }

    /**
     * Place a new circle at the passed index and animate it
     * @param col represents the column index to place the circle in
     * @param row represents the row index to place the circle in
     * @param redColor represents the color of circle to place
     */
    protected void fallingAnimation(int col, int row, boolean redColor){
        Platform.runLater(() ->{
            Circle circle = new Circle(col*xLeg + xLeg/2.0, 0, radius,redColor ? javafx.scene.paint.Color.RED : javafx.scene.paint.Color.YELLOW);
            _gamePane.getChildren().add(circle);
            circle.toFront();
            TranslateTransition trans = new TranslateTransition(Duration.millis(300), circle);
            trans.setFromY(circle.getLayoutX());
            trans.setToY((Game.ROWS-1-row)*yLeg + yLeg/2.0);
            trans.setAutoReverse(false);
            trans.setCycleCount(1);
            trans.setInterpolator(new Interpolator() {
                @Override
                protected double curve(double v) {
                    // v^2*g/2 - like physics equation x(t)=a/2*t^2+vt+h
                    double val = v*v*2;
                    if (val >= 1)
                        return 1;
                    return val;
                }
            });
            FadeTransition ft = new FadeTransition(Duration.millis(300), circle);
            ft.setAutoReverse(false);
            ft.setCycleCount(1);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.setOnFinished((event) -> onAnimationFinished());
            // play the transitions
            ft.play();
            trans.play();
        });

        try {
            playSound("disc_fall_sfx.wav");
        } catch (Exception ignore){}
    }

    abstract protected void onAnimationFinished();
    protected void updateImgCircle(){
        if (game.isFreeCol(currentMouseCol)){
            imaginaryCircle.setOpacity(imaginaryCircleOpc);
            imaginaryCircle.setCenterX(currentMouseCol*xLeg + xLeg/2.0);
            imaginaryCircle.setCenterY((Game.ROWS-1-game.firstEmpty(currentMouseCol))*yLeg + yLeg/2.0);
        } else {
            imaginaryCircle.setOpacity(0);
        }
    }

    protected void checkForEnd(){
        GameStatus status = game.status();
        if (status != GameStatus.ONGOING)
            Platform.runLater(() -> {
                gameOver(status);
                terminate();
            });
    }

    /**
     * This function gets called every time the mouse is pressed on the board.
     * as a result of the press, if the move is valid it would be played, otherwise nothing happens
     */
    @FXML
    protected void mousePressedBoard(){
        if (!game.isFreeCol(currentMouseCol) || (game.isRedToMove() ? Color.Red : Color.Yellow) != myColor)
            return;

//        fallingAnimation(currentMouseCol, game.firstEmpty(currentMouseCol), game.isRedToMove());
        game.makeMove(currentMouseCol);
    }

    /**
     * Play a sound from a file
     * @param soundFile represents the filename of the file (with no path to assets)
     * @throws LineUnavailableException when can't play the sound
     * @throws IOException when the output was interrupted
     * @throws UnsupportedAudioFileException when the file isn't of the right type
     */
    protected void playSound(String soundFile) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        AudioInputStream audioIn = AudioSystem.getAudioInputStream(Objects.requireNonNull(getClass().getResourceAsStream("./assets/" + soundFile)));
        Clip clip = AudioSystem.getClip();
        clip.open(audioIn);
        clip.start();
    }

    /**
     * End the game including prompting who won the game
     */
    protected void gameOver(GameStatus status){
        try {
            imaginaryCircle.setOpacity(0);
            playback.stop();
            if (status == GameStatus.WIN)
                if (!game.isRedToMove() == (myColor == Color.Red))
                    playSound("win_horn_sfx.wav");
                else playSound("lose_horn_sfx.wav");
            else playSound("tie_horn_sfx.wav");
        } catch (Exception ignore){}

        Alert a = new Alert(Alert.AlertType.INFORMATION, "Game Over !");
        if (status == GameStatus.WIN)
            a.setHeaderText((game.isRedToMove() ? "The yellow " : "The red ") + "player has won the game.");
        else a.setHeaderText("The game has ended in a tie.");
        a.showAndWait();
    }

    /**
     * Terminate the program
     */
    protected void terminate(){
        Platform.exit();
        System.exit(0);
    }
}
