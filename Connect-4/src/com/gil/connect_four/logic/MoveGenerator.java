package com.gil.connect_four.logic;

import com.gil.connect_four.gui.SinglePlayerController;
import javafx.application.Platform;

public class MoveGenerator implements Runnable{
    private SinglePlayerController controller;
    private int move;
    private final Game game;
    private final Color color;

    public MoveGenerator(SinglePlayerController c,Game game, Color color){
        this.game = game;
        this.color = color;
        this.controller = c;
        move = -1;
    }

    @Override
    public void run() {
        move = Engine.genBestMove(game, color);
        Platform.runLater(() -> controller.makeEngineMove(move));
    }
}
