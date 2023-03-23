package com.gil.connect_four.logic;

import java.util.Map;

public class MoveEval extends Thread{
    private final Map<Integer, Double> evaluations;
    private final int move;
    private final Game game;
    private final Color curr;

    private static final int DEPTH = 10;

    public MoveEval(Game g, int m, Color c, Map<Integer, Double> map){
        evaluations = map;
        game = g;
        curr = c;
        move = m;
    }

    @Override
    public void run() {
        game.makeMove(move);
        double eval = Engine.alphaBetaPruning(game, DEPTH, -Double.MAX_VALUE, Double.MAX_VALUE, curr);
        game.unMakeMove(move);
        synchronized (evaluations){
            evaluations.put(move, eval);
        }
    }
}
