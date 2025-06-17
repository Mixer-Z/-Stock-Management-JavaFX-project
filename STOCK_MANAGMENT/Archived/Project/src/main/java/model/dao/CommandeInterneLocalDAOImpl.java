package model.dao;

import model.entities.CommandeInterneLocal;
import model.entities.CommandeInterne;
import model.entities.Local;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CommandeInterneLocalDAOImpl implements CommandeInterneLocalDAO {
    private static final DateTimeFormatter SQLITE_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CommandeInterneLocalDAOImpl() {
    }

    @Override
    public void insert(CommandeInterneLocal local) {
        throw new UnsupportedOperationException("Use insert(CommandeInterneLocal, Connection) instead");
    }

    public void insert(CommandeInterneLocal local, Connection connection) throws SQLException {
        String sql = "INSERT INTO commande_interne_local (commande_interne_id, local_id, notes, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            try (Statement pragmaStmt = connection.createStatement()) {
                pragmaStmt.execute("PRAGMA foreign_keys = ON;");
            }
            stmt.setLong(1, local.getCommandeInterne().getId());
            stmt.setLong(2, local.getLocal().getId());
            stmt.setString(3, local.getNotes() != null ? local.getNotes() : "");
            stmt.setString(4, local.getCreatedAt() != null ? local.getCreatedAt().format(SQLITE_DATETIME_FORMATTER) : LocalDateTime.now().format(SQLITE_DATETIME_FORMATTER));
            stmt.setString(5, local.getUpdatedAt() != null ? local.getUpdatedAt().format(SQLITE_DATETIME_FORMATTER) : LocalDateTime.now().format(SQLITE_DATETIME_FORMATTER));

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    local.setId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting CommandeInterneLocal: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public CommandeInterneLocal getById(Long id) {
        String sql = "SELECT cil.*, l.nom AS local_nom " +
                "FROM commande_interne_local cil " +
                "LEFT JOIN local l ON cil.local_id = l.id " +
                "WHERE cil.id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (Statement pragmaStmt = connection.createStatement()) {
                pragmaStmt.execute("PRAGMA foreign_keys = ON;");
            }
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractCommandeInterneLocalFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving CommandeInterneLocal by ID " + id + ": " + e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération du local de commande interne par ID", e);
        }
        return null;
    }

    @Override
    public List<CommandeInterneLocal> getByCommandeInterneId(Long commandeInterneId) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return getByCommandeInterneId(commandeInterneId, connection);
        } catch (SQLException e) {
            System.err.println("Error retrieving CommandeInterneLocal for commande_interne_id " + commandeInterneId + ": " + e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des locaux de commande interne", e);
        }
    }

    public List<CommandeInterneLocal> getByCommandeInterneId(Long commandeInterneId, Connection connection) throws SQLException {
        List<CommandeInterneLocal> locals = new ArrayList<>();
        String sql = "SELECT cil.*, l.nom AS local_nom " +
                "FROM commande_interne_local cil " +
                "LEFT JOIN local l ON cil.local_id = l.id " +
                "WHERE cil.commande_interne_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (Statement pragmaStmt = connection.createStatement()) {
                pragmaStmt.execute("PRAGMA foreign_keys = ON;");
            }
            stmt.setLong(1, commandeInterneId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    locals.add(extractCommandeInterneLocalFromResultSet(rs));
                }
            }
            System.out.println("Loaded " + locals.size() + " locals for commande_interne_id: " + commandeInterneId);
        }
        return locals;
    }

    @Override
    public void update(CommandeInterneLocal local) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            update(local, connection);
        } catch (SQLException e) {
            System.err.println("Error updating CommandeInterneLocal ID " + local.getId() + ": " + e.getMessage());
            throw new RuntimeException("Erreur lors de la mise à jour du local de commande interne", e);
        }
    }

    public void update(CommandeInterneLocal local, Connection connection) throws SQLException {
        String sql = "UPDATE commande_interne_local SET commande_interne_id = ?, local_id = ?, notes = ?, created_at = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, local.getCommandeInterne().getId());
            stmt.setLong(2, local.getLocal().getId());
            stmt.setString(3, local.getNotes() != null ? local.getNotes() : "");
            stmt.setString(4, local.getCreatedAt() != null ? local.getCreatedAt().format(SQLITE_DATETIME_FORMATTER) : LocalDateTime.now().format(SQLITE_DATETIME_FORMATTER));
            stmt.setString(5, LocalDateTime.now().format(SQLITE_DATETIME_FORMATTER));
            stmt.setLong(6, local.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM commande_interne_local WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (Statement pragmaStmt = connection.createStatement()) {
                pragmaStmt.execute("PRAGMA foreign_keys = ON;");
            }
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting CommandeInterneLocal ID " + id + ": " + e.getMessage());
            throw new RuntimeException("Erreur lors de la suppression du local de commande interne", e);
        }
    }

    private CommandeInterneLocal extractCommandeInterneLocalFromResultSet(ResultSet rs) throws SQLException {
        CommandeInterneLocal local = new CommandeInterneLocal();
        local.setId(rs.getLong("id"));
        local.setNotes(rs.getString("notes"));

        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null && !rs.wasNull()) {
            try {
                local.setCreatedAt(LocalDateTime.parse(createdAtStr, SQLITE_DATETIME_FORMATTER));
            } catch (Exception e) {
                System.err.println("Error parsing created_at: " + createdAtStr + ", Message: " + e.getMessage());
                throw new SQLException("Erreur lors du parsing de created_at: " + createdAtStr, e);
            }
        }

        String updatedAtStr = rs.getString("updated_at");
        if (updatedAtStr != null && !rs.wasNull()) {
            try {
                local.setUpdatedAt(LocalDateTime.parse(updatedAtStr, SQLITE_DATETIME_FORMATTER));
            } catch (Exception e) {
                System.err.println("Error parsing updated_at: " + updatedAtStr + ", Message: " + e.getMessage());
                throw new SQLException("Erreur lors du parsing de updated_at: " + updatedAtStr, e);
            }
        }

        Long commandeInterneId = rs.getLong("commande_interne_id");
        if (!rs.wasNull()) {
            CommandeInterne commandeInterne = new CommandeInterne();
            commandeInterne.setId(commandeInterneId);
            local.setCommandeInterne(commandeInterne);
        }

        Long localId = rs.getLong("local_id");
        if (!rs.wasNull()) {
            Local l = new Local();
            l.setId(localId);
            l.setNom(rs.getString("local_nom"));
            local.setLocal(l);
        }

        return local;
    }
}