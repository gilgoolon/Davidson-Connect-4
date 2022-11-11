package com.gil.connect_four.logic;

public class MoveGenerator implements Runnable{
    private int move;
    private final Game game;
    private final Color color;

    public MoveGenerator(Game game, Color color){
        this.game = game;
        this.color = color;
        move = -1;
    }

    @Override
    public void run() {
        move = Engine.genBestMove(game,color);
        synchronized (this){
            notifyAll();
        }
    }

    public synchronized int getMove(){
        if (move == -1) {
            try {
                wait();
            } catch(Exception ignore){}
        }
        return move;
    }
}
