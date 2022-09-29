package com.gil.connect_four.gui;

import com.gil.connect_four.logic.Color;
import com.gil.connect_four.logic.Game;
import com.gil.connect_four.logic.GameStatus;
import com.gil.connect_four.logic.Utils;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Formatter;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientController implements Runnable{

    @FXML
    private TextField _chatTextField;
    @FXML
    private Label _opponentLabel;
    @FXML
    private Pane _gamePane;
    @FXML
    private TextArea _chatTextArea;

    // GUI based constants
    protected static double WIDTH; // width of the board pane - constant and short
    protected static double HEIGHT; // height of the board pane - constant and short
    protected static double xLeg; // calculate the width of each cell (disk cell) using fxml components at runtime
    protected static double yLeg; // calculate the height of each cell (disk cell) using fxml components at runtime
    protected static double padding; // calculate the padding (how many pixels to reduce from the radius)
    protected static double radius; // calculate the radius based on cell HEIGHT and WIDTH (minimum/2)

    // calculations/instance variables
    private final Game game; // logical component - connect 4 game object
    private Color myColor; // keep track of the user's color (set in the beginning)
    private int currentMouseCol; // keep track of the current column the mouse is in
    private Circle imaginaryCircle; // marks the next move (imaginary - dynamic) where the user has his their mouse on
    private static final double imaginaryCircleOpc = 0.3; // opacity of the imaginary circle
    private Clip playback; // sound playback - end when the game is over (to not intercept with win\lose sound effects)

    // Networking variables
    private static final String serverIP = "192.168.1.134"; // Gil's laptop's IP address
    private Socket connection; // connection to server
    private Scanner input; // input from server
    private Formatter output; // output to server
    private final String hostname; // host name for server


    public ClientController(){
        hostname = "localhost"; // set name of server
        game = new Game(); // create new game with starting conditions
        currentMouseCol = 0;
    }

    public void init(){
        // initialize board variables
        WIDTH = _gamePane.getWidth();
        HEIGHT = _gamePane.getHeight();
        xLeg = WIDTH/Game.COLS;
        yLeg = HEIGHT/Game.ROWS;
        padding =  5.0;
        radius = (Math.min(WIDTH/Game.COLS,HEIGHT/Game.ROWS))/2.0-padding;
        // draw white circles
        for (int x = 0; x < Game.COLS; x++){
            for (int y = 0; y < Game.ROWS; y++){
                Circle circle = new Circle(x*xLeg+xLeg/2.0,y*yLeg + yLeg/2.0,radius, javafx.scene.paint.Color.WHITE);
                _gamePane.getChildren().add(circle);
            }
        }

        imaginaryCircle = new Circle(xLeg/2.0,(Game.ROWS-1)*yLeg+yLeg/2.0,radius,game.isRedToMove() ? javafx.scene.paint.Color.RED : javafx.scene.paint.Color.YELLOW);
        imaginaryCircle.setOpacity(imaginaryCircleOpc);
        _gamePane.getChildren().add(imaginaryCircle);
        imaginaryCircle.toFront();
    }

    @FXML
    void quitPressed() {
        terminate();
    }

    @FXML
    void sendChatMessage() {
        displayMessage("You: " + _chatTextField.getText() + "\n");

        // need to send to other player

        _chatTextField.setText("");
    }

    @FXML
    void mouseMovedBoard(@NotNull MouseEvent event){
        int newCol = (int)(event.getX()/xLeg);

        if (newCol == currentMouseCol || game.isRedToMove() != (myColor == Color.Red) || newCol == Game.COLS)
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

        output.format("%d\n", currentMouseCol); // send location to server
        output.flush();
        fallingAnimation(currentMouseCol, game.firstEmpty(currentMouseCol), game.isRedToMove());
        game.makeMove(currentMouseCol);

        GameStatus status = game.status();
        if (status == GameStatus.WIN){
            try {
                imaginaryCircle.setOpacity(0);
                playback.stop();
                playSound("win_horn_sfx.wav");
            } catch (Exception ignore){}
            Alert a = new Alert(Alert.AlertType.INFORMATION, "Game Over !");
            a.setHeaderText((game.isRedToMove() ? "The yellow " : "The red ") + "player has won the game.");
            a.showAndWait();
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
            ft.setOnFinished((event) -> {
                // update imaginary circle
                Platform.runLater(()->{
                    if (game.isRedToMove() != (myColor == Color.Red)){
                        imaginaryCircle.setOpacity(0);
                    }
                    else {
                        imaginaryCircle.setCenterX(col*xLeg + xLeg/2.0);
                        if (row + 1 != Game.ROWS)
                            imaginaryCircle.setCenterY((Game.ROWS-2-row)*yLeg + yLeg/2.0);
                        imaginaryCircle.setOpacity(imaginaryCircleOpc);
                    }

                });
            });
            // play the transitions
            ft.play();
            trans.play();
        });

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

    // start the client thread
    public void startClient()
    {
        try // connect to server and get streams
        {
            // make connection to server
            connection = new Socket(InetAddress.getByName(hostname), 12345);

            // get streams for input and output
            input = new Scanner(connection.getInputStream());
            output = new Formatter(connection.getOutputStream());
        }
        catch (IOException ioException)
        {
            Alert a = new Alert(Alert.AlertType.ERROR, "Connection Error");
            a.setHeaderText("Please wait for the server to be up and try again.");
            a.showAndWait();
            terminate();
        }

        // create and start worker thread for this client
        ExecutorService worker = Executors.newFixedThreadPool(1);
        worker.execute(this); // execute client

        // start playing playback sound in loop
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(Objects.requireNonNull(getClass().getResourceAsStream("./assets/" + "playback_music_bicycle.wav")));
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.loop(Clip.LOOP_CONTINUOUSLY); // loop until the program is terminated (game is over)
            clip.start();
            playback = clip; // save the clip for later
        } catch (Exception ignore){}
    }

    // control thread that allows continuous update of displayArea
    public void run()
    {
        myColor = input.nextLine().equals(Color.Red.toString()) ? Color.Red : Color.Yellow; // get player's mark (X or O)
        imaginaryCircle.setFill(myColor == Color.Red ? javafx.scene.paint.Color.RED : javafx.scene.paint.Color.YELLOW);
        if (myColor == Color.Yellow)
            imaginaryCircle.setOpacity(0);
        Platform.runLater(() -> _opponentLabel.setText("You are player \"" + myColor + "\""));

        // receive messages sent to client and output them
        while (true)
        {
            if (input.hasNextLine())
                processMessage(input.nextLine());
        }
    }

    // process messages received by client
    private void processMessage(String message) {
        if (message.equals("Opponent moved")){
            int location = input.nextInt(); // get move location
            input.nextLine(); // skip newline after int location
            fallingAnimation(location, game.firstEmpty(location), game.isRedToMove());
            game.makeMove(location);
            displayMessage("Server>>> Opponent moved. Your turn.\n");

            // now check for win
            GameStatus status = game.status();
            if (status != GameStatus.ONGOING)
                Platform.runLater(() -> {
                    gameOver(status);
                    terminate();
                });
        }
        else if (message.startsWith("Server>>>")){
            displayMessage(message + "\n");
        }
        else {
            displayMessage(Utils.opColor(myColor) + ": " + message + "\n"); // display the message
        }
    }

    // manipulate displayArea in event-dispatch thread
    private void displayMessage(final String messageToDisplay) {
        Platform.runLater(() -> _chatTextArea.appendText(messageToDisplay));
    }

    private void gameOver(GameStatus status){
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

    public void terminate(){
        Platform.exit();
        System.exit(0);
    }
}
