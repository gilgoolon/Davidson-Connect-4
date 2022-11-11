package com.gil.connect_four.logic;

import com.sun.scenario.effect.impl.sw.java.JSWBlend_BLUEPeer;

import java.util.Arrays;
import java.util.Random;

public class Game {

    public static final int ROWS = 6; // constant variable for number of rows
    public static final int COLS = 7; // constant variable for number of columns
    private final Color[][] _board; // matrix storing a custom enum (Red 1, Yellow -1, Empty 0)
    private boolean _redToMove; // keep track of whose turn it is in a boolean

    // Zobrist Hashing
    private static int ZHtable[][] = new int[6*7][2];
    private static boolean ZHhashed = false;
    private static int ZHyellowToMove;
    /**
     * Default constructor - initialize the conditions of the start of a game
     */

    public Game(){
        // Zobrist Hashing initialization
        if (!ZHhashed){
            Random r = new Random();
            for (int i = 0; i < ZHtable.length; i++){
                ZHtable[i][0] = r.nextInt();
                ZHtable[i][1] = r.nextInt();
            }
            ZHyellowToMove = r.nextInt();
            ZHhashed = true;
        }

        _redToMove = true;
        _board = new Color[COLS][ROWS];
        for (int x = 0; x < COLS; x++)
            for (int y = 0; y < ROWS; y++)
                _board[x][y] = Color.Empty;
    }

    public Game(Game g){
        _board = new Color[g._board.length][g._board[0].length];
        for (int i = 0; i < g._board.length; i++)
            for (int j = 0; j < g._board[0].length; j++)
                _board[i][j] = g.get(i, j);
        _redToMove = g._redToMove; // keep track of whose turn it is in a boolean
    }

    /**
     * Externally check which player's turn it is
     * @return true if it is red's turn, false otherwise (yellow's turn)
     */
    public boolean isRedToMove(){
        return _redToMove;
    }

    /**
     * Simple getter function to allow externally checking the value at a specific coordinate
     * @param x represents the column index
     * @param y represents the row index
     * @return the current Color (enum) in the given coordinate
     */
    public Color get(int x, int y){
        return _board[x][y];
    }

    /**
     * Simple setter function to allow externally setting the value at a specific coordinate
     * @param x represents the column index
     * @param y represents the row index
     * @param c represents the new color to set in the coordinate
     */
    public void set(int x, int y, Color c){
        _board[x][y] = c;
    }

    /**
     * Make a move on the board - place a new disk and flip the turn
     * @param col represents the column to make the move in (assuming the move is legal)
     */
    public void makeMove(int col){
        int firstEmpty = firstEmpty(col);
        _board[col][firstEmpty] = _redToMove ? Color.Red : Color.Yellow;
        _redToMove = !_redToMove;
    }

    /**
     * Unmake the move at the specified column. Assumes the column is not empty
     * @param col represents the column index of the move to unmake
     */
    public void unMakeMove(int col){
        _board[col][firstEmpty(col)-1] = Color.Empty;
        _redToMove = ! _redToMove;
    }

    /**
     * Check if the given column is free (has at least 1 free cell)
     * @param col represents the column number
     * @return true if the given column is free, false otherwise
     */
    public boolean isFreeCol(int col){
        return _board[col][Game.ROWS-1] == Color.Empty;
    }

    /**
     * Calculate the row index of the first free cell in the given column.
     * @param col represents the column number being checked
     * @return the row index of the first free cell in the given column, ROWS if the column isn't empty
     */
    public int firstEmpty(int col){
        int i = 0;
        while (i < ROWS && _board[col][i] != Color.Empty)
            i++;

        return i;
    }

    public GameStatus status(){
        if (isWin())
            return GameStatus.WIN;
        for (int i = 0; i < COLS; i++)
            if (isFreeCol(i))
                return GameStatus.ONGOING;
        return GameStatus.TIE;
    }
    /**
     * Check if the last move resulted in a win for the player who played it
     * @return true if the current board has a win (for the last player who moved), false otherwise
     */
    public boolean isWin(){
        // horizontal check
        for (int y = 0; y < ROWS; y++){
            for (int x = 0; x + 3 < COLS; x++){
                int countR = count(x,y, x+3, y, Color.Red);
                int countY = count(x,y, x+3, y, Color.Yellow);
                if (countR == 4 || countY == 4)
                    return true;
            }
        }

        // vertical check
        for (int x = 0; x < COLS; x++){
            for (int y = 0; y + 3 < ROWS; y++){
                int countR = count(x,y, x, y+3, Color.Red);
                int countY = count(x,y, x, y+3, Color.Yellow);
                if (countR == 4 || countY == 4)
                    return true;
            }
        }

        // diagonal (up-right) check
        for (int x = 0; x + 3 < COLS; x++){
            for (int y = 0; y + 3 < ROWS; y++){
                int countR = count(x,y, x+3, y+3, Color.Red);
                int countY = count(x,y, x+3, y+3, Color.Yellow);
                if (countR == 4 || countY == 4)
                    return true;
            }
        }

        // diagonal (up-left) check
        for (int x = 0; x + 3 < COLS; x++){
            for (int y = ROWS-1; y - 3 >= 0; y--){
                int countR = count(x,y, x+3, y-3, Color.Red);
                int countY = count(x,y, x+3, y-3, Color.Yellow);
                if (countR == 4 || countY == 4)
                    return true;
            }
        }

        return false;
    }

    public int count(int x1, int y1, int x2, int y2, Color c){
        int incX = Integer.compare(x2, x1);
        int incY = Integer.compare(y2, y1);

        Color[] toCount = new Color[4];
        for (int i = 0; i < toCount.length; i++)
            toCount[i] = _board[x1 + incX * i][y1 + incY * i];
        return count(toCount, c);
    }

    private static int count(Color [] arr, Color c){
        int sum = 0;
        for (Color x : arr)
            if (x == c)
                sum++;
        return sum;
    }

    public int hash(){
        int hash = 0;
        if (!_redToMove)
            hash ^= ZHyellowToMove;
        for (int i = 0; i < _board.length; i++){
            for (int j = 0; j < _board[0].length; j++){
                if (_board[i][j] != Color.Empty){
                    int k = _board[i][j] == Color.Red ? 0 : 1;
                    hash ^= ZHtable[i*_board[0].length + j][k];
                }
            }
        }
        return hash;
    }
}