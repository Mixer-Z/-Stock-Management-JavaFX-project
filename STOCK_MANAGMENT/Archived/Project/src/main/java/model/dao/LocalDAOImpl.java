package model.dao;

import model.entities.Local;
import model.entities.Consommateur;
import utils.DatabaseConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LocalDAOImpl implements LocalDAO {
    private static final DateTimeFormatter SQLITE_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public LocalDAOImpl() {
    }

    @Override
    public void insert(Local local) {
        String sql = "INSERT INTO local (nom, emplacement, type, created_at, updated_at, consommateur_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            try (Statement pragmaStmt = connection.createStatement()) {
                pragmaStmt.execute("PRAGMA foreign_keys = ON;");
            }
            stmt.setString(1, local.getNom());
            stmt.setString(2, local.getEmplacement());
            stmt.setString(3, local.getType());
            stmt.setString(4, local.getCreatedAt() != null ? local.getCreatedAt().format(SQLITE_DATETIME_FORMATTER) : LocalDateTime.now().format(SQLITE_DATETIME_FORMATTER));
            stmt.setString(5, local.getUpdatedAt() != null ? local.getUpdatedAt().format(SQLITE_DATETIME_FORMATTER) : LocalDateTime.now().format(SQLITE_DATETIME_FORMATTER));
            stmt.setObject(6, local.getConsommateur() != null ? local.getConsommateur().getId() : null, Types.INTEGER);

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    local.setId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'insertion du local: " + e.getMessage(), e);
        }
    }

    @Override
    public Local getById(Long id) {
        String sql = "SELECT * FROM local WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (Statement pragmaStmt = connection.createStatement()) {
                pragmaStmt.execute("PRAGMA foreign_keys = ON;");
            }
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractLocalFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération du local par ID: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Local> getAll() {
        List<Local> locals = new ArrayList<>();
        String sql = "SELECT * FROM local";
        try (Connection connection = DatabaseConnection.getConnection();
             Statement stmt = connection.createStatement()) {
            try (Statement pragmaStmt = connection.createStatement()) {
                pragmaStmt.execute("PRAGMA foreign_keys = ON;");
            }
            try (ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    locals.add(extractLocalFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des locaux: " + e.getMessage(), e);
        }
        return locals;
    }

    @Override
    public void update(Local local) {
        String sql = "UPDATE local SET nom = ?, emplacement = ?, type = ?, created_at = ?, updated_at = ?, consommateur_id = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (Statement pragmaStmt = connection.createStatement()) {
                pragmaStmt.execute("PRAGMA foreign_keys = ON;");
            }
            stmt.setString(1, local.getNom());
            stmt.setString(2, local.getEmplacement());
            stmt.setString(3, local.getType());
            stmt.setString(4, local.getCreatedAt() != null ? local.getCreatedAt().format(SQLITE_DATETIME_FORMATTER) : LocalDateTime.now().format(SQLITE_DATETIME_FORMATTER));
            stmt.setString(5, LocalDateTime.now().format(SQLITE_DATETIME_FORMATTER));
            stmt.setObject(6, local.getConsommateur() != null ? local.getConsommateur().getId() : null, Types.INTEGER);
            stmt.setLong(7, local.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour du local: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Long id) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            try (Statement pragmaStmt = connection.createStatement()) {
                pragmaStmt.execute("PRAGMA foreign_keys = ON;");
            }
            // Explicitly delete dependent records to avoid trigger issues
            String deleteCommandeInterneLocalSql = "DELETE FROM commande_interne_local WHERE local_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deleteCommandeInterneLocalSql)) {
                stmt.setLong(1, id);
                stmt.executeUpdate();
            } catch (SQLException e) {
                if (!e.getMessage().contains("no such table")) {
                    throw e;
                }
            }

            String deleteCommandeExterneLocalSql = "DELETE FROM commande_externe_local WHERE local_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deleteCommandeExterneLocalSql)) {
                stmt.setLong(1, id);
                stmt.executeUpdate();
            } catch (SQLException e) {
                if (!e.getMessage().contains("no such table")) {
                    throw e;
                }
            }

            String deleteStockSql = "DELETE FROM stock WHERE local_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deleteStockSql)) {
                stmt.setLong(1, id);
                stmt.executeUpdate();
            } catch (SQLException e) {
                if (!e.getMessage().contains("no such table")) {
                    throw e;
                }
            }

            // Delete the local
            String sql = "DELETE FROM local WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                System.out.println("Executing DELETE with id: " + id);
                stmt.setLong(1, id);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du local: " + e.getMessage(), e);
        }
    }

    private Local extractLocalFromResultSet(ResultSet rs) throws SQLException {
        Local local = new Local();
        local.setId(rs.getLong("id"));
        local.setNom(rs.getString("nom"));
        local.setEmplacement(rs.getString("emplacement"));
        local.setType(rs.getString("type"));

        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null && !rs.wasNull()) {
            try {
                local.setCreatedAt(LocalDateTime.parse(createdAtStr, SQLITE_DATETIME_FORMATTER));
            } catch (Exception e) {
                throw new SQLException("Erreur lors du parsing de created_at: " + createdAtStr, e);
            }
        }

        String updatedAtStr = rs.getString("updated_at");
        if (updatedAtStr != null && !rs.wasNull()) {
            try {
                local.setUpdatedAt(LocalDateTime.parse(updatedAtStr, SQLITE_DATETIME_FORMATTER));
            } catch (Exception e) {
                throw new SQLException("Erreur lors du parsing de updated_at: " + updatedAtStr, e);
            }
        }

        Long consommateurId = rs.getLong("consommateur_id");
        if (!rs.wasNull()) {
            Consommateur consommateur = new Consommateur();
            consommateur.setId(consommateurId);
            local.setConsommateur(consommateur);
        }

        return local;
    }
}