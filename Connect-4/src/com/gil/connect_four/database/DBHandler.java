package com.gil.connect_four.database;

import java.sql.*;

/**
 * Handles the connection to the database, singleton class
 */
public class DBHandler {
    public static void main(String[] args) {
        DBHandler dbHandler = DBHandler.getInstance();
        System.out.println(dbHandler.getLeaderboard());
    }

    private static DBHandler instance = null;

    private final static String url = "jdbc:mysql://localhost:3306/connect4leaderboard";
    private final static String user = "root";
    private final static String password = "**********"; // redacted

    private Connection connection;
    private boolean isConnected;

    /**
     * Private constructor for the DBHandler
     */
    private DBHandler() {
        isConnected = false;
    }

    /**
     * Gets the instance of the DBHandler
     * @return the instance of the DBHandler
     */
    public static DBHandler getInstance() {
        if (instance == null)
            instance = new DBHandler();
        return instance;
    }

    /**
     * Connects to the database, if not already connected
     */
    private void connect() {
        if (isConnected) return;
        try {
            connection = DriverManager.getConnection(url, user, password);
            isConnected = true;
        } catch (Exception e) {
            isConnected = false;
            e.printStackTrace();
        }
    }

    /**
     * Gets the leaderboard from the database, up to 10 players
     * @return the leaderboard as a string
     */
    public String getLeaderboard() {
        connect();
        String query = """
                SELECT ip, wins, losses, ties,\s
                    (WINS / (WINS + LOSSES + TIES)) AS win_percentage
                FROM leaderboard
                ORDER BY win_percentage DESC
                LIMIT 10;""";
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            StringBuilder sb = new StringBuilder();
            while (resultSet.next()) {
                sb.append(resultSet.getString("ip")).append(" ")
                        .append(resultSet.getInt("wins")).append(" ")
                        .append(resultSet.getInt("losses")).append(" ")
                        .append(resultSet.getInt("ties")).append(" ")
                        .append(resultSet.getDouble("win_percentage")).append(" ");
            }
            return sb.substring(0, sb.length() - 1);
        } catch (SQLException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Updates the player's score in the database
     * @param data - the data to update the player with (W/L/T)
     * @param ip - the player's IP address
     */
    public void updatePlayer(String ip, String data) {
        switch (data) {
            case "W" -> incrementAttr(ip, "wins");
            case "L" -> incrementAttr(ip, "losses");
            case "T" -> incrementAttr(ip, "ties");
        }
    }

    /**
     * Increments the value of an attribute of the player in the database
     * @param ip - the player's IP address
     * @param attr - the attribute to increment
     */
    public void incrementAttr(String ip, String attr) {
        connect();
        String checkQuery = "SELECT * FROM leaderboard WHERE ip = '" + ip + "';";
        String query = "UPDATE leaderboard SET " + attr + " = " + attr + " + 1 WHERE ip = '" + ip + "';";
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(checkQuery);
            if (!resultSet.next()) {
                statement.executeUpdate("INSERT INTO leaderboard (ip, wins, losses, ties) VALUES ('" + ip + "', 0, 0, 0);");
                statement.executeUpdate(query);
            }
            else statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the connection to the database
     */
    public void disconnect() {
        if (!isConnected) return;
        try {
            connection.close();
            isConnected = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
