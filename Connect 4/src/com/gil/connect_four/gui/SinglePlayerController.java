package com.gil.connect_four.gui;

import com.gil.connect_four.logic.*;
import javafx.fxml.FXML;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SinglePlayerController extends BaseController {

    private final ExecutorService executorService;

    /**
     * Override default constructor for the controller class - initialize states and variables not related to the GUI.
     */
    public SinglePlayerController(){
        super();
        myColor = Color.Yellow;
        executorService = Executors.newCachedThreadPool();
    }

    public void execute(){
        // start playing playback sound in loop
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(Objects.requireNonNull(getClass().getResourceAsStream("./assets/" + "playback_music_bicycle.wav")));
            playback = AudioSystem.getClip();
            playback.open(audioIn);
            playback.loop(Clip.LOOP_CONTINUOUSLY); // loop until the program is terminated (game is over)
            playback.start();
        } catch (Exception ignore){}

        _opponentLabel.setText("Opponent: " + Utils.opColor(myColor));
        _chatTextArea.appendText("You are playing Yellow.\nGood luck trying to beat the engine!\n");
        if (myColor == Color.Yellow){
            makeEngineMove();
        }
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
    }

    protected void onAnimationFinished(){
        // update imaginary circle after the animation is done
        if (game.isRedToMove() != (myColor == Color.Red)){
            checkForEnd();
            makeEngineMove();
        }
        else {
            updateImgCircle();
            checkForEnd();
        }
    }

    public void makeEngineMove(int move){
        if (move == -1)
            return;

        fallingAnimation(move, game.firstEmpty(move), game.isRedToMove());
        game.makeMove(move);
    }

    private void makeEngineMove(){
        executorService.execute(new MoveGenerator(this, new Game(game), Utils.opColor(myColor)));
    }

    public void terminate(){
        executorService.shutdown();
        super.terminate();
    }
}
