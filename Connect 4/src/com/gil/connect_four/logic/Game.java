package com.gil.connect_four.logic;
public class Game {
    public static final int ROWS = 6; // constant variable for number of rows
    public static final int COLS = 7; // constant variable for number of columns
    private final Color[][] _board; // matrix storing a custom enum (Red 1, Yellow -1, Empty 0)
    private boolean _redToMove; // keep track of whose turn it is in a boolean

    /**
     * Default constructor - initialize the conditions of the start of a game
     */
    public Game(){
        _redToMove = true;
        _board = new Color[COLS][ROWS];
        for (int x = 0; x < COLS; x++)
            for (int y = 0; y < ROWS; y++)
                _board[x][y] = Color.Empty;
    }

    /**
     * Externally check which player's turn it is
     * @return true if it is red's turn, false otherwise (yellow's turn)
     */
    public boolean isRedToMove(){
        return _redToMove;
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
     * Check if the given column is free (has at least 1 free cell)
     * @param col represents the column number
     * @return true if the given column is free, false otherwise
     */
    public boolean isFreeCol(int col){
        return firstEmpty(col) != ROWS;
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

    /**
     * Check if the last move resulted in a win for the player who played it
     * @return true if the current board has a win (for the last player who moved), false otherwise
     */
    public boolean isWin(){
        // horizontal check
        for (int y = 0; y < ROWS; y++){
            for (int x = 0; x + 3 < COLS; x++){
                if (_board[x][y] != Color.Empty && _board[x][y] == _board[x+1][y] && _board[x][y] == _board[x+2][y] && _board[x][y] == _board[x+3][y])
                    return true;
            }
        }

        // vertical check
        for (int x = 0; x < COLS; x++){
            for (int y = 0; y + 3 < ROWS; y++){
                if (_board[x][y] != Color.Empty && _board[x][y] == _board[x][y+1] && _board[x][y] == _board[x][y+2] && _board[x][y] == _board[x][y+3])
                    return true;
            }
        }

        // diagonal (up-right) check
        for (int x = 0; x + 3 < COLS; x++){
            for (int y = 0; y + 3 < ROWS; y++){
                if (_board[x][y] != Color.Empty && _board[x][y] == _board[x+1][y+1] && _board[x][y] == _board[x+2][y+2] && _board[x][y] == _board[x+3][y+3])
                    return true;
            }
        }

        // diagonal (up-left) check
        for (int x = 0; x + 3 < COLS; x++){
            for (int y = ROWS-1; y > 0; y--){
                if (_board[x][y] != Color.Empty && _board[x][y] == _board[x+1][y-1] && _board[x][y] == _board[x+2][y-2] && _board[x][y] == _board[x+3][y-3])
                    return true;
            }
        }

        return false;
    }
}
