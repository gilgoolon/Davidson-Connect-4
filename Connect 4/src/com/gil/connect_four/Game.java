package com.gil.connect_four;
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
        _board = new Color[ROWS][COLS];
        for (int x = 0; x < ROWS; x++)
            for (int y = 0; y < COLS; y++)
                _board[x][y] = Color.Empty;
    }

    /**
     * Getter function for the color of a specific cell
     * @param x represents the x index of the cell
     * @param y represents the y index of the cell
     * @return the color of the given cell (Color enum - Red(1), Yellow(-1), Empty(0))
     */
    public Color get(int x, int y){
        return _board[x][y];
    }

    /**
     * Externally check which player's turn it is
     * @return true if it is red's turn, false otherwise (yellow's turn)
     */
    public boolean isRedToMove(){
        return _redToMove;
    }

    /**
     * Set function to update a certain cell
     * @param c represents the new color
     * @param x represents the x index to update
     * @param y represents the y index to update
     */
    private void set(Color c, int x, int y){
        _board[x][y] = c;
    }

    /**
     * Make a move on the board - place a new disk and flip the turn
     * @param col represents the column to make the move in (assuming the move is legal)
     */
    public void makeMove(int col){
        int firstEmpty = firstEmpty(col);
        _board[firstEmpty][col] = _redToMove ? Color.Red : Color.Yellow;
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
        while (_board[i][col] == Color.Empty && i < ROWS)
            i++;

        return i;
    }

    /**
     * Check if the last move resulted in a win for the player who played it
     * @param lastX represents the x index of the last move
     * @param lastY represents the y index of the last move
     * @return true if the current board has a win (for the last player who moved), false otherwise
     */
    public boolean isWin(int lastX, int lastY){
        /* NEED TO CHECK AND TEST */
        // vertical check
        for (int i = 0; i < ROWS-4; i++)
            if (_board[i][lastY] == _board[i+1][lastY] && _board[i+2][lastY] == _board[i+3][lastY])
                return true;

        // horizontal check
        for (int i = 0; i < COLS-4; i++)
            if (_board[lastX][i] == _board[lastX][i+1] && _board[lastX][i] == _board[lastX][i+2] && _board[lastX][i] == _board[lastX][i+3])
                return true;

        // diagonal (left-down to right-up) check
        for (int i = Math.min(3, Math.min(lastX,lastY)); i >= 0 && lastX - i + 3 < ROWS && lastY - i + 3 < COLS; i--)
            if (_board[lastX-i][lastY-i] == _board[lastX-i+1][lastY-i+1] && _board[lastX-i][lastY-i] == _board[lastX-i+2][lastY-i+2] && _board[lastX-i][lastY-i] == _board[lastX-i+3][lastY-i+3])
                return true;

        // diagonal (left-down to right-up) check
        for (int i = Math.min(3, Math.min(ROWS-lastX,lastY)); i >= 0 && lastX + i - 3 < ROWS && lastY - i + 3 < COLS; i--)
            if (_board[lastX+i][lastY-i] == _board[lastX+i-1][lastY-i-1] && _board[lastX+i][lastY-i] == _board[lastX+i-2][lastY-i+2] && _board[lastX-i][lastY-i] == _board[lastX+i-3][lastY-i+3])
                return true;

        return false;
    }
}
