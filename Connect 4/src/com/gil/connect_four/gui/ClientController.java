package com.gil.connect_four.gui;

import com.gil.connect_four.logic.Color;
import com.gil.connect_four.logic.Game;
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
    private Pane gamePane;
    @FXML
    private TextArea chatTextArea;

    // Game and GUI variable
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
    private Clip playback;

    // Networking variables
    private static final String serverIP = "192.168.1.134"; // Gil's laptop's IP address
    private Socket connection; // connection to server
    private Scanner input; // input from server
    private Formatter output; // output to server
    private final String hostname; // host name for server
    private Color myColor; // this client's mark


    public ClientController(){
        hostname = "localhost"; // set name of server
        game = new Game(); // create new game with starting conditions
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

        output.format("%d\n", currentMouseCol); // send location to server
        output.flush();
        fallingAnimation(currentMouseCol, game.firstEmpty(currentMouseCol), game.isRedToMove());
        game.makeMove(currentMouseCol);
        if (game.isWin()){
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
    private void processMessage(String message)
    {
        if (message.equals("Opponent moved")){
            int location = input.nextInt(); // get move location
            input.nextLine(); // skip newline after int location
            fallingAnimation(location, game.firstEmpty(location), game.isRedToMove());
            game.makeMove(location);
            displayMessage("Server>>> Opponent moved. Your turn.\n");

            // now check for win
            if (game.isWin()){
                try {
                    playback.stop();
                    playSound("lose_horn_sfx.wav");
                } catch (Exception ignore){}
                Platform.runLater(() -> {
                    imaginaryCircle.setOpacity(0);
                    Alert a = new Alert(Alert.AlertType.INFORMATION, "Game Over !");
                    a.setHeaderText((game.isRedToMove() ? "The yellow " : "The red ") + "player has won the game.");
                    a.showAndWait();
                    terminate();
                });
            }
        }
        else if (message.startsWith("Server>>>")){
            displayMessage(message + "\n");
        }
        else {
            displayMessage((myColor == Color.Red ? Color.Yellow : Color.Red) + ": " + message + "\n"); // display the message
        }
    }

    // manipulate displayArea in event-dispatch thread
    private void displayMessage(final String messageToDisplay)
    {
        Platform.runLater(() -> chatTextArea.appendText(messageToDisplay));
    }

    public void terminate(){
        Platform.exit();
        System.exit(0);
    }
}
