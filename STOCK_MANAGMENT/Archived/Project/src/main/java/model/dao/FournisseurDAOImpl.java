package model.dao;

import model.entities.Fournisseur;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FournisseurDAOImpl implements FournisseurDAO {
    private static final String INSERT_SQL = "INSERT INTO fournisseur (nom, adresse, telephone, email, site_web, personne_contact, notes, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM fournisseur WHERE id = ?";
    private static final String SELECT_ALL = "SELECT * FROM fournisseur";
    private static final String UPDATE_SQL = "UPDATE fournisseur SET nom = ?, adresse = ?, telephone = ?, email = ?, site_web = ?, personne_contact = ?, notes = ?, updated_at = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM fournisseur WHERE id = ?";
    private static final DateTimeFormatter SQLITE_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public FournisseurDAOImpl() {
        // No single connection; each method will get a new connection
    }

    @Override
    public void create(Fournisseur fournisseur) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            // Enable foreign keys
            try (Statement pragmaStmt = conn.createStatement()) {
                pragmaStmt.execute("PRAGMA foreign_keys = ON;");
            }
            stmt.setString(1, fournisseur.getNom());
            stmt.setString(2, fournisseur.getAdresse());
            stmt.setString(3, fournisseur.getTelephone());
            stmt.setString(4, fournisseur.getEmail());
            stmt.setString(5, fournisseur.getSiteWeb());
            stmt.setString(6, fournisseur.getPersonneContact());
            stmt.setString(7, fournisseur.getNotes());
            stmt.setString(8, fournisseur.getCreatedAt() != null ? fournisseur.getCreatedAt().format(SQLITE_TIMESTAMP_FORMAT) : LocalDateTime.now().format(SQLITE_TIMESTAMP_FORMAT));
            stmt.setString(9, fournisseur.getUpdatedAt() != null ? fournisseur.getUpdatedAt().format(SQLITE_TIMESTAMP_FORMAT) : LocalDateTime.now().format(SQLITE_TIMESTAMP_FORMAT));

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    fournisseur.setId(generatedKeys.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'insertion du fournisseur: " + e.getMessage(), e);
        }
    }

    @Override
    public Fournisseur findById(Long id) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            // Enable foreign keys
            try (Statement pragmaStmt = conn.createStatement()) {
                pragmaStmt.execute("PRAGMA foreign_keys = ON;");
            }
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToFournisseur(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération du fournisseur par ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Fournisseur> findAll() throws Exception {
        List<Fournisseur> fournisseurs = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL)) {
            // Enable foreign keys
            try (Statement pragmaStmt = conn.createStatement()) {
                pragmaStmt.execute("PRAGMA foreign_keys = ON;");
            }
            while (rs.next()) {
                fournisseurs.add(mapResultSetToFournisseur(rs));
            }
            return fournisseurs;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des fournisseurs: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Fournisseur fournisseur) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            // Enable foreign keys
            try (Statement pragmaStmt = conn.createStatement()) {
                pragmaStmt.execute("PRAGMA foreign_keys = ON;");
            }
            stmt.setString(1, fournisseur.getNom());
            stmt.setString(2, fournisseur.getAdresse());
            stmt.setString(3, fournisseur.getTelephone());
            stmt.setString(4, fournisseur.getEmail());
            stmt.setString(5, fournisseur.getSiteWeb());
            stmt.setString(6, fournisseur.getPersonneContact());
            stmt.setString(7, fournisseur.getNotes());
            stmt.setString(8, LocalDateTime.now().format(SQLITE_TIMESTAMP_FORMAT));
            stmt.setLong(9, fournisseur.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour du fournisseur: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Long id) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {
            // Enable foreign keys
            try (Statement pragmaStmt = conn.createStatement()) {
                pragmaStmt.execute("PRAGMA foreign_keys = ON;");
            }
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du fournisseur: " + e.getMessage(), e);
        }
    }

    private Fournisseur mapResultSetToFournisseur(ResultSet rs) throws SQLException {
        Fournisseur fournisseur = new Fournisseur();
        fournisseur.setId(rs.getLong("id"));
        fournisseur.setNom(rs.getString("nom"));
        fournisseur.setAdresse(rs.getString("adresse"));
        fournisseur.setTelephone(rs.getString("telephone"));
        fournisseur.setEmail(rs.getString("email"));
        fournisseur.setSiteWeb(rs.getString("site_web"));
        fournisseur.setPersonneContact(rs.getString("personne_contact"));
        fournisseur.setNotes(rs.getString("notes"));

        // Handle created_at
        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null && !createdAtStr.isEmpty()) {
            try {
                LocalDateTime createdAt = LocalDateTime.parse(createdAtStr, SQLITE_TIMESTAMP_FORMAT);
                fournisseur.setCreatedAt(createdAt);
            } catch (Exception e) {
                throw new SQLException("Erreur lors du parsing de created_at: " + createdAtStr, e);
            }
        }

        // Handle updated_at
        String updatedAtStr = rs.getString("updated_at");
        if (updatedAtStr != null && !updatedAtStr.isEmpty()) {
            try {
                LocalDateTime updatedAt = LocalDateTime.parse(updatedAtStr, SQLITE_TIMESTAMP_FORMAT);
                fournisseur.setUpdatedAt(updatedAt);
            } catch (Exception e) {
                throw new SQLException("Erreur lors du parsing de updated_at: " + updatedAtStr, e);
            }
        }

        return fournisseur;
    }
}