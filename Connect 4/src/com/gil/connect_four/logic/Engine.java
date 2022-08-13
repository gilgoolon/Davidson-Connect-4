package com.gil.connect_four.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Engine {

    /**
     * Generate the best move "naively" - using brute force approach
     * @param game represents the current position to generate a move to/from
     * @return the column number of the generated move
     */
    public static int genBestMove(Game game){
        ArrayList<Integer> legal_moves = Utils.genLegalMoves(game); // generating the legal moves
        HashMap<Integer,Double> evaluations = new HashMap<>(); // store the evaluation of each move in a map
        for (int move : legal_moves) { // calculate and store the eval of each move
            game.makeMove(move);
            evaluations.put(move, evaluate(game, move));
            game.unMakeMove(move);
        }

        int best_move = -1;
        if (game.isRedToMove()) {
            double best_eval = Double.NEGATIVE_INFINITY;
            for (int move : legal_moves) {
                if (evaluations.get(move) >= best_eval){
                    best_move = move;
                    best_eval = evaluations.get(move);
                }
            }
        }
        else {
            double best_eval = Double.POSITIVE_INFINITY; // the best for yellow is more negative
            for (int move : legal_moves) {
                if (evaluations.get(move) <= best_eval){
                    best_move = move;
                    best_eval = evaluations.get(move);
                }
            }
        }

        return best_move;
    }

    /**
     * Evaluate a certain (legal) move from a certain position (negative - better for yellow, positive - better for red)
     * Assumes the move has been made already (makeMove(col) has been called before the function)
     * @param game represents the position to evaluate
     * @param col represents the move to evaluate (column number)
     * @return a double representing the evaluation of the given position
     */
    private static double evaluate(Game game, int col){
        // good characteristics
        final double CONNECT_2 = 2;
        final double CONNECT_3 = 5;
        final double CENTER = 4;
        final double WIN = Integer.MAX_VALUE;

        // bad characteristics
        final double OPP_CONNECT_2 = -3;
        final double OPP_CONNECT_3 = -10;
        final double OPP_WIN = -WIN;

        // initializations
        final int SIGN = game.isRedToMove() ? -1 : 1;
        final int row = game.firstEmpty(col) - 1;
        double eval = 0;

        // evaluating all possibilities and giving score

        // center col
        if (col == Game.COLS/2)
            eval += CENTER;

        // checking rows
        int consRow = getConsecutive(game,col,row,0,1,0);
        if (consRow >= 4)
            return WIN*SIGN;
        else if (consRow == 3)
            eval+=CONNECT_3;
        else if (consRow == 2)
            eval+=CONNECT_2;

        // checking columns
        int consCol = getConsecutive(game,col,row,0,0,1);
        if (consCol >= 4)
            return WIN*SIGN;
        else if (consCol == 3)
            eval+=CONNECT_3;
        else if (consCol == 2)
            eval+=CONNECT_2;

        // checking up-right diagonal
        int consUR = getConsecutive(game,col,row,0,1,1);
        if (consUR >= 4)
            return WIN*SIGN;
        else if (consUR == 3)
            eval+=CONNECT_3;
        else if (consUR == 2)
            eval+=CONNECT_2;

        // checking up-left diagonal
        int consUL = getConsecutive(game,col,row,0,1,-1);
        if (consUL >= 4)
            return WIN*SIGN;
        else if (consUL == 3)
            eval+=CONNECT_3;
        else if (consUL == 2)
            eval+=CONNECT_2;

        
        ArrayList<Integer> op_moves = Utils.genLegalMoves(game);
        for (int move : op_moves) {
            game.makeMove(move);
            // checking opposite winnable connect 3 (opponent)
            if (game.isWin())
                eval += OPP_WIN;
            if (getConsecutive(game,move,game.firstEmpty(move)-1,0,1,0) == 3)
                eval+= OPP_CONNECT_3;
            if(getConsecutive(game,move,game.firstEmpty(move)-1,0,0,1) == 3)
                eval+= OPP_CONNECT_3;
            if (getConsecutive(game,move,game.firstEmpty(move)-1,0,1,1) == 3)
                eval+= OPP_CONNECT_3;
            if(getConsecutive(game,move,game.firstEmpty(move)-1,0,-1,1) == 3)
                eval+= OPP_CONNECT_3;
            if (getConsecutive(game,move,game.firstEmpty(move)-1,0,1,0) == 2)
                eval+= OPP_CONNECT_2;
            if(getConsecutive(game,move,game.firstEmpty(move)-1,0,0,1) == 2)
                eval+= OPP_CONNECT_2;
            if (getConsecutive(game,move,game.firstEmpty(move)-1,0,1,1) == 2)
                eval+= OPP_CONNECT_2;
            if(getConsecutive(game,move,game.firstEmpty(move)-1,0,-1,1) == 2)
                eval+= OPP_CONNECT_2;
            game.unMakeMove(move);
        }

        return eval*SIGN; // because the move has been make and turn was changed
    }

    /**
     * Calculate the amount of consecutive disks in a given direction from a given point
     * @param game represents the current position (after the move was made)
     * @param col represents the column index of the point
     * @param row represents the row index of the point
     * @param count represents the amount found previously in the given direction (recursively updated - needs to begin with 0)
     * @param offsetX represents the value to increment in X in each "jump"
     * @param offsetY represents the value to increment in Y in each "jump"
     * @return the amount of consecutive disks (empty doesn't increment but doesn't interrupt)
     */
    private static int getConsecutive(Game game, int col, int row, int count, final int offsetX, final int offsetY){
        if (col < 0 || col >= Game.COLS || row < 0 || row >= Game.ROWS || game.get(col,row) == (game.isRedToMove() ? Color.Red : Color
                .Yellow))
            return count;

        boolean empty = game.get(col,row) == Color.Empty;
        // calculate recursively left and right values (treat as an array)
        game.set(col,row,game.isRedToMove() ? Color.Red : Color.Yellow);
        int right = getConsecutive(game, col+offsetX, row+offsetY, count, offsetX, offsetY);
        int left = getConsecutive(game, col-offsetX, row-offsetY, count, offsetX, offsetY);
        if (empty)
            game.set(col,row,Color.Empty);
        else game.set(col,row,game.isRedToMove() ? Color.Yellow : Color.Red);

        if (empty) // empty - continue but do not increment
            return count + left + right;
        else return count + left + right + 1; // the current is our color
    }

    /**
     * Function to generate a random (legal) move
     * @param game represents the current position to generate from
     * @return the column number of the move
     */
    public static int genRandomMove(Game game){
        ArrayList<Integer> moves = Utils.genLegalMoves(game);
        Random r = new Random();
        return moves.get(r.nextInt(moves.size()));
    }
}
