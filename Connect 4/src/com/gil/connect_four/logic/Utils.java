package com.gil.connect_four.logic;

import java.util.*;

public class Utils {
    public static void main(String[] args) {
        System.out.println("Starting the game");
        playGame();
    }

    /**
     * Start playing a game against the engine in terminal
     */
    public static void playGame() {
        Game game = new Game(); // create the game
        while (!game.isWin()){
            if (game.isRedToMove()) // if red to move let the user choose input
                game.makeMove(getLegalMoveFromStdIn(game));
            else {
                int move = Engine.genBestMove(game,Color.Yellow);
                game.makeMove(move); // else make the engine move
                printPos(game);
                System.out.println("Opponent played: " + move);
            }
        }

        System.out.println("Game Over !");
    }

    /**
     * Print the current game position to the stdOut
     * @param game represents the current position
     */
    public static void printPos(Game game){
        for (int y = Game.ROWS-1; y >= 0; y--){
            for (int x = 0; x < Game.COLS; x++){
                System.out.print(format(game.get(x,y)) + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    /**
     * Generates the legal moves in a given position
     * @param game represents the current position
     * @return an arraylist containing all possible moves
     */
    public static List<Integer> genLegalMoves(Game game){
        // adding move order optimization
        if (game.isWin())
            return Collections.emptyList();
        ArrayList<Integer> moves = new ArrayList<>();
        if (game.isFreeCol(Game.COLS/2))
            moves.add(Game.COLS/2);
        for (int i = 1; i <= Game.COLS/2; i++) {
            if (game.isFreeCol(Game.COLS/2 + i))
                moves.add(Game.COLS/2 + i);
            if (game.isFreeCol(Game.COLS/2 - i))
                moves.add(Game.COLS/2 - i);
        }
        return moves;
    }

    /**
     * Get the opposite color (Red v Yellow)
     * @param color represents the given color
     * @return the opposite color of the given color
     */
    public static Color opColor(Color color){
        return color == Color.Red ? Color.Yellow : Color.Red;
    }

    /**
     * Get a legal move from standard input for the current position
     * @param game represents the current position
     * @return the column number of the selected move
     */
    public static int getLegalMoveFromStdIn(Game game){
        while (true){
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter a move: ");
            try {
                int col = scanner.nextInt();
                if (col >= 0 && col < Game.COLS && game.isFreeCol(col))
                    return col;
                else System.out.println("Illegal or wrong move. Try again.");
            } catch (InputMismatchException e){
                System.out.println("Please enter an integer as the move.");
            }
        }
    }

    /**
     * Format a given color (enum) to string format (for printPos)
     * @param c represents the given color
     * @return the string representation - Red("R"), Yellow("Y"), Empty("0")
     */
    private static String format(Color c){
        if (c == Color.Red)
            return "R";
        if (c == Color.Yellow)
            return "Y";
        return "0";
    }
}
