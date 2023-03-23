package com.gil.connect_four.gui;

import com.gil.connect_four.logic.Color;
import com.gil.connect_four.logic.GameStatus;
import com.gil.connect_four.logic.Utils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Formatter;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientController extends BaseController implements Runnable{

    @FXML
    private TextField _chatTextField;
    @FXML
    private Label _timeLabel;

    // Networking variables
    private static final String SERVER_IP = "localhost"; //host IP address
    private Scanner input; // input from server
    private Formatter output; // output to server
    private final String hostname; // host name for server

    // Timer variables
    private Timeline timeline;
    private int seconds = 30;
    private int minutes = 1;

    public ClientController(){
        super();
        hostname = SERVER_IP; // set name of server
    }

    public void init(){
        super.init();
        setUpTimer();
    }

    @FXML
    void sendChatMessage() {
        displayMessage("You: " + _chatTextField.getText() + "\n");

        // need to send to other player
        output.format("-1\n" + _chatTextField.getText() + "\n");
        output.flush();
        _chatTextField.setText("");
    }

    @FXML
    void leaderboardButtonPressed() {
        output.format("-3\n"); // db request with code 1, which is to get the leaderboard
        output.flush();
    }

    /**
     * This function gets called every time the mouse is pressed on the board.
     * as a result of the press, if the move is valid it would be played, otherwise nothing happens
     */
    @FXML
    protected void mousePressedBoard(){
        if (!game.isFreeCol(currentMouseCol) || (game.isRedToMove() ? Color.Red : Color.Yellow) != myColor)
            return;

        fallingAnimation(currentMouseCol, game.firstEmpty(currentMouseCol), game.isRedToMove());
        game.makeMove(currentMouseCol);

        output.format("%d\n", currentMouseCol); // send location to server
        output.flush();

        GameStatus status = game.status();
        if (status != GameStatus.ONGOING)
            Platform.runLater(() -> {
                gameOver(status);
                terminate();
            });
    }

    protected void onAnimationFinished(){
        // update imaginary circle
        if (game.isRedToMove() == (myColor == Color.Red)){
            updateImgCircle();
        }
        checkForEnd();
    }
    // start the client thread
    public void execute()
    {
        Socket connection = null;
        try // connect to server and get streams
        {
            // make connection to server
            // connection to server
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
            if (connection != null){
                try {
                    connection.close();
                } catch(IOException ignore){}
            }
            terminate();
        }

        // create and start worker thread for this client
        ExecutorService worker = Executors.newFixedThreadPool(1);
        worker.execute(this); // execute client

        // start playing playback sound in loop
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(Objects.requireNonNull(getClass().getResourceAsStream("./assets/" + "playback_music_bicycle.wav")));
            Clip clip = AudioSystem.getClip();
//            clip.open(audioIn);
//            clip.loop(Clip.LOOP_CONTINUOUSLY); // loop until the program is terminated (game is over)
//            clip.start();
//            playback = clip; // save the clip for later
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
            else break;
        }
    }

    private void setUpTimer(){
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        KeyFrame keyframe = new KeyFrame(Duration.seconds(1), (e) -> {
            if (game.isRedToMove() != (myColor == Color.Red)) { // if not my turn, don't do anything
                _timeLabel.setText(timerToString());
                return;
            }

            if (seconds == 0 && minutes == 0)
                timesUp();
            else if (seconds == 0) {
                seconds = 59;
                minutes--;
            } else seconds--;
            _timeLabel.setText(timerToString());
        });
        timeline.getKeyFrames().add(keyframe);
    }

    private String timerToString(){
        String result = "";

        if (minutes/10 > 0)
            result += minutes;
        else result += "0" + minutes;

        result += ":";

        if (seconds/10 > 0)
            result += seconds;
        else result += "0" + seconds;

        return result;
    }

    private void timesUp(){
        output.format("-2\n");
        output.flush();
        Platform.runLater(() -> {
            timeline.stop();
            gameOver(GameStatus.TIMES_UP);
            terminate();
        });
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
        else if (message.startsWith("Server>>> ")) {
            if (message.equals("Server>>> Other player connected. Your move.") || message.equals("Server>>> Player Yellow connected, please wait"))
                timeline.playFromStart();
            else if (message.equals("Server>>> " + (game.isRedToMove() ? "The red " : "The yellow ") + "player has lost the game because they ran out of time.")){
                timeline.stop();
                Platform.runLater(() -> {
                    gameOver(GameStatus.TIMES_UP);
                    terminate();
                });
            }
            else if (message.startsWith("Server>>> Leaderboard: ")) {
                // leaderboard is in the format: "ip1 wins1 losses1 ties1 percentage1 name2 ..."
                String[] leaderboardArr = message.substring("Server>>> Leaderboard: ".length()).split(" ");
                StringBuilder leaderboardStr = new StringBuilder();
                leaderboardStr.append("------------------------------------\n");
                leaderboardStr.append("|                 Leaderboard             |\n");
                leaderboardStr.append("------------------------------------\n");
                leaderboardStr.append(String.format("| %-17s | %-9s | %-5s |", "IP", "W/L/T", "Win %"));
                for (int i = 0; i < leaderboardArr.length; i += 5) {
                    String ip = leaderboardArr[i];
                    String wins = leaderboardArr[i + 1];
                    String losses = leaderboardArr[i + 2];
                    String ties = leaderboardArr[i + 3];
                    String percentage = leaderboardArr[i + 4];
                    leaderboardStr.append(String.format("%n| %-15s | %-10s | %-5s |", ip, wins + "/" + losses + "/" + ties, Math.round(Double.parseDouble(percentage)*1000)/10.0 + "%"));
                }
                leaderboardStr.append("\n------------------------------------\n");

                // display the leaderboard
                Platform.runLater(() -> {
                    Alert a = new Alert(Alert.AlertType.INFORMATION, leaderboardStr.toString());
                    a.setHeaderText("Leaderboard");
                    a.showAndWait();
                });
                return; // don't display the message
            }
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
}
