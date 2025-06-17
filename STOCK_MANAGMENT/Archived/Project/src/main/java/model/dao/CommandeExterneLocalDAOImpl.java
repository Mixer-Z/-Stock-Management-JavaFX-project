package model.dao;

import model.entities.CommandeExterneLocal;
import model.entities.CommandeExterne;
import model.entities.Local;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of CommandeExterneLocalDAO for managing local associations with external orders.
 */
public class CommandeExterneLocalDAOImpl implements CommandeExterneLocalDAO {
    private static final DateTimeFormatter SQLITE_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void insert(CommandeExterneLocal commandeExterneLocal) {
        if (commandeExterneLocal == null || commandeExterneLocal.getCommandeExterne() == null || commandeExterneLocal.getLocal() == null) {
            throw new IllegalArgumentException("CommandeExterneLocal, its CommandeExterne, and Local cannot be null");
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            insert(commandeExterneLocal, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting CommandeExterneLocal: " + e.getMessage(), e);
        }
    }

    @Override
    public void insert(CommandeExterneLocal commandeExterneLocal, Connection conn) throws SQLException {
        if (commandeExterneLocal == null || commandeExterneLocal.getCommandeExterne() == null || commandeExterneLocal.getLocal() == null) {
            throw new IllegalArgumentException("CommandeExterneLocal, its CommandeExterne, and Local cannot be null");
        }

        String sql = "INSERT INTO commande_externe_local (commande_externe_id, local_id, created_at, updated_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            try (Statement fkStmt = conn.createStatement()) {
                fkStmt.execute("PRAGMA foreign_keys = ON;");
            }
            stmt.setLong(1, commandeExterneLocal.getCommandeExterne().getId());
            stmt.setLong(2, commandeExterneLocal.getLocal().getId());
            stmt.setString(3, commandeExterneLocal.getCreatedAt() != null ?
                    commandeExterneLocal.getCreatedAt().format(SQLITE_DATETIME_FORMATTER) :
                    LocalDateTime.now().format(SQLITE_DATETIME_FORMATTER));
            stmt.setString(4, commandeExterneLocal.getUpdatedAt() != null ?
                    commandeExterneLocal.getUpdatedAt().format(SQLITE_DATETIME_FORMATTER) :
                    LocalDateTime.now().format(SQLITE_DATETIME_FORMATTER));

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    commandeExterneLocal.setId(rs.getLong(1));
                }
            }
        }
    }

    @Override
    public CommandeExterneLocal getById(Long id) {
        if (id == null || id <= 0) {
            return null;
        }

        String sql = "SELECT cel.id, cel.commande_externe_id, cel.local_id, cel.created_at, cel.updated_at, " +
                "l.nom AS local_nom " +
                "FROM commande_externe_local cel " +
                "JOIN local l ON cel.local_id = l.id " +
                "WHERE cel.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (Statement fkStmt = conn.createStatement()) {
                fkStmt.execute("PRAGMA foreign_keys = ON;");
            }
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractCommandeExterneLocalFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving CommandeExterneLocal by ID: " + id + ", Message: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<CommandeExterneLocal> getByCommandeExterneId(Long commandeExterneId) {
        if (commandeExterneId == null || commandeExterneId <= 0) {
            return new ArrayList<>();
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            return getByCommandeExterneId(commandeExterneId, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving CommandeExterneLocals for commande_externe_id: " + commandeExterneId + ", Message: " + e.getMessage(), e);
        }
    }

    @Override
    public List<CommandeExterneLocal> getByCommandeExterneId(Long commandeExterneId, Connection conn) throws SQLException {
        List<CommandeExterneLocal> locals = new ArrayList<>();
        String sql = "SELECT cel.id, cel.commande_externe_id, cel.local_id, cel.created_at, cel.updated_at, " +
                "l.nom AS local_nom " +
                "FROM commande_externe_local cel " +
                "JOIN local l ON cel.local_id = l.id " +
                "WHERE cel.commande_externe_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (Statement fkStmt = conn.createStatement()) {
                fkStmt.execute("PRAGMA foreign_keys = ON;");
            }
            stmt.setLong(1, commandeExterneId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CommandeExterneLocal local = extractCommandeExterneLocalFromResultSet(rs);
                    locals.add(local);
                }
            }
        }
        return locals;
    }

    @Override
    public void update(CommandeExterneLocal commandeExterneLocal) {
        if (commandeExterneLocal == null || commandeExterneLocal.getId() == null || commandeExterneLocal.getCommandeExterne() == null || commandeExterneLocal.getLocal() == null) {
            throw new IllegalArgumentException("CommandeExterneLocal, its ID, CommandeExterne, and Local cannot be null");
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            update(commandeExterneLocal, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error updating CommandeExterneLocal ID: " + commandeExterneLocal.getId() + ", Message: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(CommandeExterneLocal commandeExterneLocal, Connection conn) throws SQLException {
        String sql = "UPDATE commande_externe_local SET commande_externe_id = ?, local_id = ?, created_at = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, commandeExterneLocal.getCommandeExterne().getId());
            stmt.setLong(2, commandeExterneLocal.getLocal().getId());
            stmt.setString(3, commandeExterneLocal.getCreatedAt() != null ?
                    commandeExterneLocal.getCreatedAt().format(SQLITE_DATETIME_FORMATTER) :
                    LocalDateTime.now().format(SQLITE_DATETIME_FORMATTER));
            stmt.setString(4, LocalDateTime.now().format(SQLITE_DATETIME_FORMATTER));
            stmt.setLong(5, commandeExterneLocal.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid CommandeExterneLocal ID: " + id);
        }

        String sql = "DELETE FROM commande_externe_local WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (Statement fkStmt = conn.createStatement()) {
                fkStmt.execute("PRAGMA foreign_keys = ON;");
            }
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting CommandeExterneLocal ID: " + id + ", Message: " + e.getMessage(), e);
        }
    }

    private CommandeExterneLocal extractCommandeExterneLocalFromResultSet(ResultSet rs) throws SQLException {
        CommandeExterneLocal commandeExterneLocal = new CommandeExterneLocal();
        commandeExterneLocal.setId(rs.getLong("id"));

        CommandeExterne commandeExterne = new CommandeExterne();
        commandeExterne.setId(rs.getLong("commande_externe_id"));
        commandeExterneLocal.setCommandeExterne(commandeExterne);

        Local local = new Local();
        local.setId(rs.getLong("local_id"));
        local.setNom(rs.getString("local_nom"));
        commandeExterneLocal.setLocal(local);

        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null && !rs.wasNull()) {
            try {
                commandeExterneLocal.setCreatedAt(LocalDateTime.parse(createdAtStr, SQLITE_DATETIME_FORMATTER));
            } catch (Exception e) {
                throw new SQLException("Error parsing created_at: " + createdAtStr + ", Message: " + e.getMessage(), e);
            }
        }

        String updatedAtStr = rs.getString("updated_at");
        if (updatedAtStr != null && !rs.wasNull()) {
            try {
                commandeExterneLocal.setUpdatedAt(LocalDateTime.parse(updatedAtStr, SQLITE_DATETIME_FORMATTER));
            } catch (Exception e) {
                throw new SQLException("Error parsing updated_at: " + updatedAtStr + ", Message: " + e.getMessage(), e);
            }
        }

        return commandeExterneLocal;
    }
}