package com.gil.connect_four.logic;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Engine {

    // position characteristics
    private final static double CONNECT_2 = 2;
    private final static double CONNECT_3 = 10;
    private final static double WIN = Integer.MAX_VALUE;
    private final static double[][] CENTER_MAP = {
            {0,1,2,4,2,1,0},
            {0,1,2,4,2,1,0},
            {0,1,4,5,4,1,0},
            {0,2,4,5,4,2,0},
            {0,1,2,4,2,1,0},
            {0,0,0,0,0,0,0}
    };

    /**
     * Generate the best move "naively" - using brute force approach
     * @param game represents the current position to generate a move to/from
     * @return the column number of the generated move
     */
    public static int genBestMove(Game game, Color curr){
        List<Integer> legal_moves = Utils.genLegalMoves(game); // generating the legal moves
        HashMap<Integer,Double> evaluations = new HashMap<>(); // store the evaluation of each move in a map
        for (int move : legal_moves) { // calculate and store the eval of each move
            game.makeMove(move);
            evaluations.put(move, alphaBetaPruning(game, move, 10,curr,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY));
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

    private static double alphaBetaPruning(Game game, int lastMove, int depth, Color current, double alpha, double beta){
        List<Integer> legal_moves = Utils.genLegalMoves(game);

        if (legal_moves.isEmpty() || depth == 0) // leaf node
            return evaluate(game, lastMove);

        double bestVal;
        if (current == Color.Yellow){
            bestVal = Double.NEGATIVE_INFINITY;
            for (int move : legal_moves){
                game.makeMove(move);
                double value = alphaBetaPruning(game, move,depth-1, Utils.opColor(current),alpha,beta);
                game.unMakeMove(move);
                bestVal = Math.max(bestVal,value);
                alpha = Math.max(alpha,bestVal);
                if (beta <= alpha)
                    break;
            }
        }
        else { // yellow - minimize value
            bestVal = Double.POSITIVE_INFINITY;
            for (int move : legal_moves){
                game.makeMove(move);
                double value = alphaBetaPruning(game, lastMove, depth-1, Utils.opColor(current),alpha,beta);
                game.unMakeMove(move);
                bestVal = Math.min(bestVal,value);
                beta = Math.min(beta,bestVal);
                if (beta <= alpha)
                    break;
            }
        }
        return bestVal;
    }

    /**
     * Evaluate a certain (legal) move from a certain position (negative - better for yellow, positive - better for red)
     * Assumes the move has been made already (makeMove(col) has been called before the function)
     * @param game represents the position to evaluate
     * @param col represents the move to evaluate (column number)
     * @return a double representing the evaluation of the given position
     */
    private static double evaluate(Game game, int col){
        // initializations
        Color curr = game.isRedToMove() ? Color.Yellow : Color.Red;
        int sign = game.isRedToMove() ? -1 : 1;
        final int row = game.firstEmpty(col)-1;
        System.out.println(row);
        double eval = 0.0;

        eval += CENTER_MAP[row][col];
        // horizontal check
        for (int y = 0; y < Game.ROWS; y++){
            for (int x = 0; x + 3 < Game.COLS; x++){
                int countG = game.count(x,y, x+3, y, curr);
                int countE = game.count(x,y, x+3, y, Color.Empty);
                if (countG == 4)
                    return WIN*sign;
                else if (countG == 3 && countE == 1)
                    eval += CONNECT_3;
                else if (countG == 2 && countE == 2)
                    eval += CONNECT_2;
                else if (countG == 0){
                    if (countE == 0)
                        return -WIN*sign;
                    else if (countE == 1)
                        eval -= CONNECT_3;
                    else if (countE == 2)
                        eval -= CONNECT_2;
                }
            }
        }

        // vertical check
        for (int x = 0; x < Game.COLS; x++){
            for (int y = 0; y + 3 < Game.ROWS; y++){
                int countG = game.count(x,y,x,y+3, curr);
                int countE = game.count(x,y,x,y+3, Color.Empty);
                if (countG == 4)
                    return WIN*sign;
                else if (countG == 3 && countE == 1)
                    eval += CONNECT_3;
                else if (countG == 2 && countE == 2)
                    eval += CONNECT_2;
                else if (countG == 0){
                    if (countE == 0)
                        return -WIN*sign;
                    else if (countE == 1)
                        eval -= CONNECT_3;
                    else if (countE == 2)
                        eval -= CONNECT_2;
                }
            }
        }

        // diagonal (up-right) check
        for (int x = 0; x + 3 < Game.COLS; x++){
            for (int y = 0; y + 3 < Game.ROWS; y++){
                int countG = game.count(x,y, x+3, y+3, curr);
                int countE = game.count(x,y, x+3, y+3, Color.Empty);
                if (countG == 4)
                    return WIN*sign;
                else if (countG == 3 && countE == 1)
                    eval += CONNECT_3;
                else if (countG == 2 && countE == 2)
                    eval += CONNECT_2;
                else if (countG == 0){
                    if (countE == 0)
                        return -WIN*sign;
                    else if (countE == 1)
                        eval -= CONNECT_3;
                    else if (countE == 2)
                        eval -= CONNECT_2;
                }
            }
        }

        // diagonal (up-left) check
        for (int x = 0; x + 3 < Game.COLS; x++){
            for (int y = Game.ROWS-1; y - 3 >= 0; y--){
                int countG = game.count(x,y, x+3, y-3, curr);
                int countE = game.count(x,y, x+3, y-3, Color.Empty);
                if (countG == 4)
                    return WIN*sign;
                else if (countG == 3 && countE == 1)
                    eval += CONNECT_3;
                else if (countG == 2 && countE == 2)
                    eval += CONNECT_2;
                else if (countG == 0){
                    if (countE == 0)
                        return -WIN*sign;
                    else if (countE == 1)
                        eval -= CONNECT_3;
                    else if (countE == 2)
                        eval -= CONNECT_2;
                }
            }
        }

        return eval*sign;
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
        List<Integer> moves = Utils.genLegalMoves(game);
        Random r = new Random();
        return moves.get(r.nextInt(moves.size()));
    }
}
