package model.dao;

import model.entities.Magasinier;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MagasinierDAOImpl implements MagasinierDAO {

    private final Connection connection;
    private static final DateTimeFormatter SQLITE_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public MagasinierDAOImpl() {
        try {
            this.connection = DatabaseConnection.getConnection();
            System.out.println("Connected to database: " + connection.getMetaData().getURL());
        } catch (SQLException e) {
            throw new RuntimeException("Impossible d'obtenir la connexion à la base de données", e);
        }
    }

    @Override
    public void insert(Magasinier magasinier) {
        // Modified query to match database schema
        String sql = "INSERT INTO magasinier (nom, nom_utilisateur, hashed_password, actif, dernier_connexion, created_at) " +
                "VALUES (?, ?, ?, ?, ?, datetime('now'))";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, magasinier.getNom());
            stmt.setString(2, magasinier.getNomUtilisateur());
            stmt.setString(3, magasinier.getHashedPassword());
            stmt.setInt(4, magasinier.isActif() ? 1 : 0);
            if (magasinier.getDernierConnexion() != null) {
                stmt.setString(5, magasinier.getDernierConnexion().format(SQLITE_DATETIME_FORMATTER));
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }

            System.out.println("Executing insert for magasinier: " + magasinier.getNomUtilisateur());
            stmt.executeUpdate();

            // Retrieve generated key
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                magasinier.setId(rs.getLong(1));
            }

        } catch (SQLException e) {
            System.err.println("SQLException during insert: " + e.getMessage());
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("ErrorCode: " + e.getErrorCode());
            throw new RuntimeException("Erreur lors de l'insertion du magasinier: " + e.getMessage(), e);
        }
    }

    @Override
    public Magasinier getById(Long id) {
        String sql = "SELECT * FROM magasinier WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractMagasinierFromResultSet(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération du magasinier par ID", e);
        }
        return null;
    }

    @Override
    public Magasinier getByNomUtilisateur(String nomUtilisateur) {
        String sql = "SELECT * FROM magasinier WHERE nom_utilisateur = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nomUtilisateur);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractMagasinierFromResultSet(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération du magasinier par nom d'utilisateur", e);
        }
        return null;
    }

    private Magasinier extractMagasinierFromResultSet(ResultSet rs) throws SQLException {
        Magasinier magasinier = new Magasinier();
        magasinier.setId(rs.getLong("id"));
        magasinier.setNom(rs.getString("nom"));
        magasinier.setNomUtilisateur(rs.getString("nom_utilisateur"));
        magasinier.setHashedPassword(rs.getString("hashed_password"));
        magasinier.setActif(rs.getInt("actif") == 1);

        String dernierConnexionStr = rs.getString("dernier_connexion");
        if (dernierConnexionStr != null && !rs.wasNull()) {
            try {
                magasinier.setDernierConnexion(LocalDateTime.parse(dernierConnexionStr, SQLITE_DATETIME_FORMATTER));
            } catch (Exception e) {
                // Try alternative parsing if the standard format fails
                try {
                    magasinier.setDernierConnexion(LocalDateTime.parse(dernierConnexionStr.replace(" ", "T")));
                } catch (Exception ex) {
                    System.err.println("Failed to parse dernier_connexion: " + dernierConnexionStr);
                    // Don't throw, just leave as null
                }
            }
        }

        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null && !rs.wasNull()) {
            try {
                magasinier.setCreatedAt(LocalDateTime.parse(createdAtStr, SQLITE_DATETIME_FORMATTER));
            } catch (Exception e) {
                // Try alternative parsing if the standard format fails
                try {
                    magasinier.setCreatedAt(LocalDateTime.parse(createdAtStr.replace(" ", "T")));
                } catch (Exception ex) {
                    System.err.println("Failed to parse created_at: " + createdAtStr);
                    // Use current time as fallback
                    magasinier.setCreatedAt(LocalDateTime.now());
                }
            }
        }

        // Handle updated_at with careful null checking since this field might be causing issues
        try {
            int columnIndex = rs.findColumn("updated_at");
            String updatedAtStr = rs.getString(columnIndex);
            if (updatedAtStr != null && !rs.wasNull()) {
                try {
                    magasinier.setUpdatedAt(LocalDateTime.parse(updatedAtStr, SQLITE_DATETIME_FORMATTER));
                } catch (Exception e) {
                    try {
                        magasinier.setUpdatedAt(LocalDateTime.parse(updatedAtStr.replace(" ", "T")));
                    } catch (Exception ex) {
                        System.err.println("Failed to parse updated_at: " + updatedAtStr);
                        // Use current time as fallback
                        magasinier.setUpdatedAt(LocalDateTime.now());
                    }
                }
            } else {
                // If updated_at is null, use same as created_at
                magasinier.setUpdatedAt(magasinier.getCreatedAt());
            }
        } catch (SQLException e) {
            // Column doesn't exist, use createdAt
            System.err.println("updated_at column not found, using current time");
            magasinier.setUpdatedAt(LocalDateTime.now());
        }

        return magasinier;
    }

    @Override
    public List<Magasinier> getAll() {
        List<Magasinier> magasiniers = new ArrayList<>();
        String sql = "SELECT * FROM magasinier";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                magasiniers.add(extractMagasinierFromResultSet(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des magasiniers", e);
        }
        return magasiniers;
    }

    @Override
    public void update(Magasinier magasinier) {
        // Modified query to handle updated_at column that might not exist
        String sql;
        try {
            // First check if the updated_at column exists
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet columns = meta.getColumns(null, null, "magasinier", "updated_at");
            boolean updatedAtExists = columns.next();
            columns.close();

            if (updatedAtExists) {
                sql = "UPDATE magasinier SET nom = ?, nom_utilisateur = ?, hashed_password = ?, actif = ?, " +
                        "dernier_connexion = ?, updated_at = datetime('now') WHERE id = ?";
            } else {
                sql = "UPDATE magasinier SET nom = ?, nom_utilisateur = ?, hashed_password = ?, actif = ?, " +
                        "dernier_connexion = ? WHERE id = ?";
            }
        } catch (SQLException e) {
            // Fall back to safer SQL without updated_at
            sql = "UPDATE magasinier SET nom = ?, nom_utilisateur = ?, hashed_password = ?, actif = ?, " +
                    "dernier_connexion = ? WHERE id = ?";
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, magasinier.getNom());
            stmt.setString(2, magasinier.getNomUtilisateur());
            stmt.setString(3, magasinier.getHashedPassword());
            stmt.setInt(4, magasinier.isActif() ? 1 : 0);
            if (magasinier.getDernierConnexion() != null) {
                stmt.setString(5, magasinier.getDernierConnexion().format(SQLITE_DATETIME_FORMATTER));
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }
            stmt.setLong(6, magasinier.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("SQLException during update: " + e.getMessage());
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("ErrorCode: " + e.getErrorCode());
            throw new RuntimeException("Erreur lors de la mise à jour du magasinier: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Long id) {
        // Simple delete query that only relies on ID column
        String sql = "DELETE FROM magasinier WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            System.out.println("Executing delete for magasinier with ID: " + id);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Delete operation affected " + rowsAffected + " rows");
        } catch (SQLException e) {
            System.err.println("SQLException during delete: " + e.getMessage());
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("ErrorCode: " + e.getErrorCode());
            throw new RuntimeException("Erreur lors de la suppression du magasinier: " + e.getMessage(), e);
        }
    }
}