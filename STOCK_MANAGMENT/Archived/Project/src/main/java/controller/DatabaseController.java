package controller;

import java.sql.Connection;
import java.sql.SQLException;

import utils.DatabaseConnection;

public class DatabaseController {

    private Connection connection;

    public DatabaseController() {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'obtention de la connexion à la base de données : " + e.getMessage());
        }
    }

    public Connection getConnection() {
        return this.connection;
    }

    public void closeConnection() {
        DatabaseConnection.closeConnection();
    }
}
