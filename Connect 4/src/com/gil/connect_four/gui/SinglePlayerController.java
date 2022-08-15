package com.gil.connect_four.gui;

import com.gil.connect_four.logic.Color;
import com.gil.connect_four.logic.Engine;
import com.gil.connect_four.logic.Game;
import com.gil.connect_four.logic.Utils;
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

public class SinglePlayerController {

    // GUI - FXML components
    @FXML
    private Label _opponentLabel; // label to display the opponent's color
    @FXML
    private Pane gamePane; // pane to deal with disks - placing, animations, design and more...
    @FXML
    private TextArea chatTextArea; // text area to display messages for the user

    // GUI based constants
    protected static double WIDTH; // width of the board pane - constant and short
    protected static double HEIGHT; // height of the board pane - constant and short
    protected static double xLeg; // calculate the width of each cell (disk cell) using fxml components at runtime
    protected static double yLeg; // calculate the height of each cell (disk cell) using fxml components at runtime
    protected static double padding; // calculate the padding (how many pixels to reduce from the radius)
    protected static double radius; // calculate the radius based on cell height and width (minimum/2)

    // calculations/instance variables
    private final Game game; // logical component - connect 4 game object
    private final Color myColor; // keep track of the user's color (set in the beginning)
    private int currentMouseCol; // keep track of the current column the mouse is in
    private Circle imaginaryCircle; // marks the next move (imaginary - dynamic) where the user has his their mouse on
    private static final double imaginaryCircleOpc = 0.3; // opacity of the imaginary circle
    private Clip playback; // sound playback - end when the game is over (to not intercept with win\lose sound effects)

    /**
     * Override default constructor for the controller class - initialize states and variables not related to the GUI.
     */
    public SinglePlayerController(){
        game = new Game();
        currentMouseCol = 0;
        myColor = Color.Red;
    }

    /**
     * This function is a special javafx function that gets called immediately after the "behind the scenes"
     * setup of the GUI. We use this function to set up the GUI using variable set in the FXML file.
     * Certain things cannot be accessed in the constructor like the pane height and width (because it is still null
     * in runtime of construction) and therefore need to wait for the setup of the GUI that occurs behind automatically
     * behind the scenes using some information from the FXML file.
     */
    @FXML
    void initialize(){
        // initialize board variables
        WIDTH = gamePane.getPrefWidth();
        HEIGHT = gamePane.getPrefHeight();
        xLeg = WIDTH/Game.COLS;
        yLeg = HEIGHT/Game.ROWS;
        padding =  5.0;
        radius = (Math.min(xLeg,yLeg))/2.0-padding;
        // draw white circles
        for (int x = 0; x < Game.COLS; x++){
            for (int y = 0; y < Game.ROWS; y++){
                Circle circle = new Circle(x*xLeg+xLeg/2.0,y*yLeg + yLeg/2.0,radius, javafx.scene.paint.Color.WHITE);
                gamePane.getChildren().add(circle);
            }
        }

        imaginaryCircle = new Circle(xLeg/2.0,(Game.ROWS-1)*yLeg+yLeg/2.0,radius,game.isRedToMove() ? javafx.scene.paint.Color.RED : javafx.scene.paint.Color.YELLOW);
        imaginaryCircle.setOpacity(imaginaryCircleOpc);
        gamePane.getChildren().add(imaginaryCircle);
        imaginaryCircle.toFront();

        // start playing playback sound in loop
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(Objects.requireNonNull(getClass().getResourceAsStream("./assets/" + "playback_music_bicycle.wav")));
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.loop(Clip.LOOP_CONTINUOUSLY); // loop until the program is terminated (game is over)
            clip.start();
            playback = clip; // save the clip for later
        } catch (Exception ignore){}

        _opponentLabel.setText("Opponent: " + Utils.opColor(myColor));
        chatTextArea.appendText("You are playing Red.\nGood luck trying to beat the engine!\n");
    }

    /**
     * This function gets called when the user presses the "quit" button, terminates the program.
     */
    @FXML
    void quitPressed() {
        terminate();
    }

    /**
     * This function gets called every time the mouse is moved on the board. Using the mouse
     * coordinates, we calculate the current column that the mouse is in and update the
     * "imaginary circle" if needed.
     * @param event represents the event of the moving mouse (used to get current mouse coordinates).
     */
    @FXML
    void mouseMovedBoard(@NotNull MouseEvent event){
        int newCol = (int)(event.getX()/xLeg);

        if (newCol == currentMouseCol || game.isRedToMove() != (myColor == Color.Red))
            return;

        currentMouseCol = newCol;
        if (game.isFreeCol(currentMouseCol)){
            imaginaryCircle.setOpacity(imaginaryCircleOpc);
            imaginaryCircle.setCenterX(newCol*xLeg + xLeg/2.0);
            imaginaryCircle.setCenterY((Game.ROWS-1-game.firstEmpty(newCol))*yLeg + yLeg/2.0);
        } else {
            imaginaryCircle.setOpacity(0);
        }
    }

    /**
     * This function gets called every time the mouse is pressed on the board.
     * as a result of the press, if the move is valid it would be played, otherwise nothing happens
     */
    @FXML
    void mousePressedBoard(){
        if (!game.isFreeCol(currentMouseCol) || (game.isRedToMove() ? Color.Red : Color.Yellow) != myColor)
            return;

        fallingAnimation(currentMouseCol, game.firstEmpty(currentMouseCol), game.isRedToMove());
        game.makeMove(currentMouseCol);
        if (game.isWin()){
            try {
                imaginaryCircle.setOpacity(0);
                playback.stop();
                playSound("win_horn_sfx.wav");
            } catch (Exception ignore){}
            gameOver();
            terminate();
        }
    }

    /**
     * Place a new circle at the passed index and animate it
     * @param col represents the column index to place the circle in
     * @param row represents the row index to place the circle in
     * @param redColor represents the color of circle to place
     */
    private void fallingAnimation(int col, int row, boolean redColor){
        Circle circle = new Circle(col*xLeg + xLeg/2.0, 0, radius,redColor ? javafx.scene.paint.Color.RED : javafx.scene.paint.Color.YELLOW);
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
            // update imaginary circle after the animation is done
            if (game.isRedToMove() != (myColor == Color.Red)){
                imaginaryCircle.setOpacity(0);
                int engineMove = Engine.genBestMove(game);
                fallingAnimation(engineMove, game.firstEmpty(engineMove), game.isRedToMove());
                game.makeMove(engineMove);
                if (engineMove == col) {
                    if (row + 2 != Game.ROWS) {
                        imaginaryCircle.setCenterY((Game.ROWS - 3 - row) * yLeg + yLeg / 2.0);
                    }
                }
                else if (row + 1 != Game.ROWS) {
                        imaginaryCircle.setCenterY((Game.ROWS-2-row)*yLeg + yLeg/2.0);
                }
                imaginaryCircle.setOpacity(imaginaryCircleOpc);
                if (game.isWin()){
                    try {
                        imaginaryCircle.setOpacity(0);
                        playback.stop();
                        playSound("win_horn_sfx.wav");
                    } catch (Exception ignore){}
                    gameOver();
                }
            }
        });
        // play the transitions
        ft.play();
        trans.play();

        try {
            playSound("disc_fall_sfx.wav");
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
        AudioInputStream audioIn = AudioSystem.getAudioInputStream(Objects.requireNonNull(getClass().getResourceAsStream("./assets/" + soundFile)));
        Clip clip = AudioSystem.getClip();
        clip.open(audioIn);
        clip.start();
    }

    /**
     * End the game including prompting who won the game
     */
    private void gameOver(){
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION, "Game Over !");
            a.setHeaderText((game.isRedToMove() ? "The yellow " : "The red ") + "player has won the game.");
            a.showAndWait();
            terminate();
        });
    }

    /**
     * Terminate the program
     */
    public void terminate(){
        Platform.exit();
        System.exit(0);
    }
}
