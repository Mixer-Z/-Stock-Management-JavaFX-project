package com.example.project;

import controller.DatabaseController;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDatabaseController {

    public static void main(String[] args) {
        DatabaseController dbController = new DatabaseController();

        try {
            Connection conn = dbController.getConnection();
            if (conn != null) {
                System.out.println("✅ Connection established successfully.");

                // Test query
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT 1 AS test");

                if (rs.next()) {
                    System.out.println("Test query result: " + rs.getInt("test")); // Should print 1
                }

                rs.close();
                stmt.close();
            } else {
                System.out.println("❌ Failed to establish connection.");
            }
        } catch (Exception e) {
            System.err.println("❌ Error during test: " + e.getMessage());
        } finally {
            dbController.closeConnection();
            System.out.println("✅ Connection closed.");
        }
    }
}

