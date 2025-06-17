package model.dao;

import model.entities.Consommateur;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ConsommateurDAOImpl implements ConsommateurDAO {
    private static final DateTimeFormatter SQLITE_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ConsommateurDAOImpl() {
        // No single connection; each method will get a new connection
    }

    @Override
    public void insert(Consommateur consommateur) {
        String sql = "INSERT INTO consommateur (nom, email, telephone, type, description, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Enable foreign keys
            try (Statement pragmaStmt = conn.createStatement()) {
                pragmaStmt.execute("PRAGMA foreign_keys = ON;");
            }
            stmt.setString(1, consommateur.getNom());
            stmt.setString(2, consommateur.getEmail());
            stmt.setString(3, consommateur.getTelephone());
            stmt.setString(4, consommateur.getType());
            stmt.setString(5, consommateur.getDescription());
            stmt.setString(6, consommateur.getCreatedAt() != null ? consommateur.getCreatedAt().format(SQLITE_DATETIME_FORMATTER) : LocalDateTime.now().format(SQLITE_DATETIME_FORMATTER));
            stmt.setString(7, consommateur.getUpdatedAt() != null ? consommateur.getUpdatedAt().format(SQLITE_DATETIME_FORMATTER) : LocalDateTime.now().format(SQLITE_DATETIME_FORMATTER));

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    consommateur.setId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'insertion du consommateur: " + e.getMessage(), e);
        }
    }

    @Override
    public Consommateur getById(Long id) {
        String sql = "SELECT * FROM consommateur WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Enable foreign keys
            try (Statement pragmaStmt = conn.createStatement()) {
                pragmaStmt.execute("PRAGMA foreign_keys = ON;");
            }
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractConsommateurFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération du consommateur par ID: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Consommateur> getAll() {
        List<Consommateur> consommateurs = new ArrayList<>();
        String sql = "SELECT * FROM consommateur";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            // Enable foreign keys
            try (Statement pragmaStmt = conn.createStatement()) {
                pragmaStmt.execute("PRAGMA foreign_keys = ON;");
            }
            while (rs.next()) {
                consommateurs.add(extractConsommateurFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des consommateurs: " + e.getMessage(), e);
        }
        return consommateurs;
    }

    @Override
    public void update(Consommateur consommateur) {
        String sql = "UPDATE consommateur SET nom = ?, email = ?, telephone = ?, type = ?, description = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Enable foreign keys
            try (Statement pragmaStmt = conn.createStatement()) {
                pragmaStmt.execute("PRAGMA foreign_keys = ON;");
            }
            stmt.setString(1, consommateur.getNom());
            stmt.setString(2, consommateur.getEmail());
            stmt.setString(3, consommateur.getTelephone());
            stmt.setString(4, consommateur.getType());
            stmt.setString(5, consommateur.getDescription());
            stmt.setString(6, LocalDateTime.now().format(SQLITE_DATETIME_FORMATTER));
            stmt.setLong(7, consommateur.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour du consommateur: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM consommateur WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Enable foreign keys
            try (Statement pragmaStmt = conn.createStatement()) {
                pragmaStmt.execute("PRAGMA foreign_keys = ON;");
            }
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du consommateur: " + e.getMessage(), e);
        }
    }

    private Consommateur extractConsommateurFromResultSet(ResultSet rs) throws SQLException {
        Consommateur consommateur = new Consommateur();
        consommateur.setId(rs.getLong("id"));
        consommateur.setNom(rs.getString("nom"));
        consommateur.setEmail(rs.getString("email"));
        consommateur.setTelephone(rs.getString("telephone"));
        consommateur.setType(rs.getString("type"));
        consommateur.setDescription(rs.getString("description"));

        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null && !rs.wasNull()) {
            try {
                consommateur.setCreatedAt(LocalDateTime.parse(createdAtStr, SQLITE_DATETIME_FORMATTER));
            } catch (Exception e) {
                throw new SQLException("Erreur lors du parsing de created_at: " + createdAtStr, e);
            }
        }

        String updatedAtStr = rs.getString("updated_at");
        if (updatedAtStr != null && !rs.wasNull()) {
            try {
                consommateur.setUpdatedAt(LocalDateTime.parse(updatedAtStr, SQLITE_DATETIME_FORMATTER));
            } catch (Exception e) {
                throw new SQLException("Erreur lors du parsing de updated_at: " + updatedAtStr, e);
            }
        }

        return consommateur;
    }
}