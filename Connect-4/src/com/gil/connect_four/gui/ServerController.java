package com.gil.connect_four.gui;

import com.gil.connect_four.logic.Color;
import com.gil.connect_four.logic.Game;
import com.gil.connect_four.logic.GameStatus;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.Formatter;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class ServerController
{
    @FXML
    private TextArea _outputArea;

    @FXML
    private TextField _inputTextField;

    private final Game game;
    private final Player[] players; // array of Players
    private ServerSocket server; // server socket to connect with clients
    private int currentPlayer; // keeps track of player with current move
    private final static int PLAYER_X = 0; // constant for first player
    private final static Color[] COLORS = {Color.Red, Color.Yellow}; // array of colors
    private final ExecutorService runGame; // will run players
    private final Lock gameLock; // to lock game for synchronization
    private final Condition otherPlayerConnected; // to wait for other player
    private final Condition otherPlayerTurn; // to wait for other player's turn

    // set up tic-tac-toe server and GUI that displays messages
    public ServerController()
    {
        // create ExecutorService with a thread for each player
        runGame = Executors.newFixedThreadPool(2);
        gameLock = new ReentrantLock(); // create lock for game

        // condition variable for both players being connected
        otherPlayerConnected = gameLock.newCondition();

        // condition variable for the other player's turn
        otherPlayerTurn = gameLock.newCondition();

        game = new Game(); // create new game with starting conditions
        players = new Player[2]; // create array of players
        currentPlayer = PLAYER_X; // set current player to first player

        try
        {
            server = new ServerSocket(12345, 2); // set up ServerSocket
        }
        catch (IOException ioException)
        {
            ioException.printStackTrace();
            terminate();
        }
    }

    @FXML
    void sendMessagePressed(){
        displayMessage("Server>>> " + _inputTextField.getText() + "\n");
        // need to send to both players

        _inputTextField.setText("");
    }

    // wait for two connections so game can be played
    public void execute()
    {
        // wait for each client to connect
        for (int i = 0; i < players.length; i++)
        {
            try // wait for connection, create Player, start runnable
            {
                players[i] = new Player(server.accept(), i);
                runGame.execute(players[i]); // execute player runnable
            }
            catch (IOException ioException)
            {
                ioException.printStackTrace();
                terminate();
            }
        }

        gameLock.lock(); // lock game to signal player X's thread

        try
        {
            players[PLAYER_X].setSuspended(false); // resume player X
            otherPlayerConnected.signal(); // wake up player X's thread
        }
        finally
        {
            gameLock.unlock(); // unlock game after signalling player X
        }
    }

    // display message in _outputArea
    private void displayMessage(final String messageToDisplay)
    {
        // display message from event-dispatch thread of execution
        Platform.runLater(() -> _outputArea.appendText(messageToDisplay));
    }

    // determine if move is valid
    public boolean validateAndMove(int location, int player)
    {
        // while not current player, must wait for turn
        while (player != currentPlayer)
        {
            gameLock.lock(); // lock game to wait for other player to go

            try
            {
                otherPlayerTurn.await(); // wait for player's turn
            }
            catch (InterruptedException exception)
            {
                exception.printStackTrace();
            }
            finally
            {
                gameLock.unlock(); // unlock game after waiting
            }
        }

        if (location == -1)
            return false;
        // if location not occupied, make move
        if (game.isFreeCol(location))
        {
            game.makeMove(location); // set move on board
            currentPlayer = (currentPlayer + 1) % 2; // change player

            // let new current player know that move occurred
            players[currentPlayer].otherPlayerMoved(location);

            gameLock.lock(); // lock game to signal other player to go

            try
            {
                otherPlayerTurn.signal(); // signal other player to continue
            }
            finally
            {
                gameLock.unlock(); // unlock game after signaling
            }

            return true; // notify player that move was valid
        }
        else // move was not valid
            return false; // notify player that move was invalid
    }

    private void otherPlayerMessage(Player p, String message){
        Player other;
        if (players[0] == p)
            other = players[1];
        else other = players[0];

        other.output.format(message + "\n");
        other.output.flush();
    }

    // private inner class Player manages each Player as a runnable
    private class Player implements Runnable
    {
        private final Socket connection; // connection to client
        private Scanner input; // input from client
        private Formatter output; // output to client
        private final int playerNumber; // tracks which player this is
        private final Color color; // color for this player
        private boolean suspended = true; // whether thread is suspended

        // set up Player thread
        public Player(Socket socket, int number)
        {
            playerNumber = number; // store this player's number
            color = COLORS[playerNumber]; // specify player's color
            connection = socket; // store socket for client

            try // obtain streams from Socket
            {
                input = new Scanner(connection.getInputStream());
                output = new Formatter(connection.getOutputStream());
            }
            catch (IOException ioException)
            {
                ioException.printStackTrace();
                terminate();
            }
        }

        // send message that other player moved
        public void otherPlayerMoved(int location)
        {
            output.format("Opponent moved\n");
            output.format("%d\n", location); // send location of move
            output.flush(); // flush output
        }

        // control thread's execution
        public void run()
        {
            // send client its color (Red or Yellow), process messages from client
            try
            {
                displayMessage("Player " + color + " connected\n");
                output.format("%s\n", color); // send player's color
                output.flush(); // flush output

                // if player X, wait for another player to arrive
                if (playerNumber == PLAYER_X)
                {
                    output.format("Server>>> %s\nServer>>> %s", "Player Red connected",
                            "Waiting for another player\n");
                    output.flush(); // flush output

                    gameLock.lock(); // lock game to  wait for second player

                    try
                    {
                        while(suspended)
                        {
                            otherPlayerConnected.await(); // wait for player O
                        }
                    }
                    catch (InterruptedException exception)
                    {
                        exception.printStackTrace();
                    }
                    finally
                    {
                        gameLock.unlock(); // unlock game after second player
                    }

                    // send message that other player connected
                    output.format("Server>>> Other player connected. Your move.\n");
                    output.flush(); // flush output
                }
                else
                {
                    output.format("Server>>> Player Yellow connected, please wait\n");
                    output.flush(); // flush output
                }

                // while game not over
                while (game.status() == GameStatus.ONGOING)
                {
                    int location = -1; // initialize move location

                    if (input.hasNext())
                        location = input.nextInt(); // get move location

                    if (location == -1){
                        input.nextLine();
                        otherPlayerMessage(this, input.nextLine());
                    }
                    else if (location == -2){
                        players[1-currentPlayer].output.format("Server>>> " + (game.isRedToMove() ? "The red " : "The yellow ") + "player has lost the game because they ran out of time.\n");
                        players[1-currentPlayer].output.flush();
                    }
                    else {
                        // check for valid move
                        if (validateAndMove(location, playerNumber))
                        {
                            displayMessage("\nlocation: " + location);
                            output.format("Server>>> Valid move.\n"); // notify client
                            output.flush(); // flush output
                        }
                        else // move was invalid
                        {
                            output.format("Server>>> Invalid move, try again\n");
                            output.flush(); // flush output
                        }
                    }
                }
            }
            finally
            {
                try
                {
                    connection.close(); // close connection to client
                }
                catch (IOException ioException)
                {
                    ioException.printStackTrace();
                }
                finally {
                    if (game.status() == GameStatus.ONGOING)
                        displayMessage("Server>>> " + (game.isRedToMove() ? "The red " : "The yellow ") + "player has lost the game because they ran out of time.");
                    else displayMessage("Server>>> The " + (game.isRedToMove() ? Color.Yellow : Color.Red) + " player has won the game!\n");
                    terminate();
                }
            }
        }

        // set whether the thread is suspended
        public void setSuspended(boolean status)
        {
            suspended = status; // set value of suspended
        }
    }

    public void terminate(){
        runGame.shutdown();
        System.exit(1);
        Platform.exit();
    }
}