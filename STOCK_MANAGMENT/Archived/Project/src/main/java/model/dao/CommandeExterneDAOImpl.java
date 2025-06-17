package model.dao;

import model.entities.CommandeExterne;
import model.entities.Magasinier;
import model.entities.Fournisseur;
import model.entities.Local;
import model.entities.CommandeExterneArticle;
import model.entities.CommandeExterneLocal;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of CommandeExterneDAO for managing external orders in the database.
 */
public class CommandeExterneDAOImpl implements CommandeExterneDAO {
    private static final DateTimeFormatter SQLITE_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final CommandeExterneArticleDAO articleDAO;
    private final CommandeExterneLocalDAO localDAO;

    public CommandeExterneDAOImpl() {
        this.articleDAO = new CommandeExterneArticleDAOImpl();
        this.localDAO = new CommandeExterneLocalDAOImpl();
    }

    @Override
    public void insert(CommandeExterne commandeExterne) {
        if (commandeExterne == null) {
            throw new IllegalArgumentException("CommandeExterne cannot be null");
        }

        String sql = "INSERT INTO commande_externe (created_at, confirmed_at, updated_at, statut, magasinier_id, fournisseur_id, local_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (Statement fkStmt = connection.createStatement()) {
                    fkStmt.execute("PRAGMA foreign_keys = ON;");
                }

                try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    setCommandeExterneFields(stmt, commandeExterne);
                    stmt.executeUpdate();

                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            commandeExterne.setId(rs.getLong(1));
                        }
                    }
                }

                insertArticles(commandeExterne, connection);
                insertLocals(commandeExterne, connection);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException("Error inserting CommandeExterne: " + e.getMessage(), e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error managing database connection: " + e.getMessage(), e);
        }
    }

    @Override
    public CommandeExterne getById(Long id) {
        if (id == null || id <= 0) {
            return null;
        }

        String sql = "SELECT ce.id, ce.created_at, ce.confirmed_at, ce.updated_at, ce.statut, " +
                "m.id AS magasinier_id, m.nom AS magasinier_nom, " +
                "f.id AS fournisseur_id, f.nom AS fournisseur_nom, " +
                "l.id AS local_id, l.nom AS local_nom " +
                "FROM commande_externe ce " +
                "LEFT JOIN magasinier m ON ce.magasinier_id = m.id " +
                "LEFT JOIN fournisseur f ON ce.fournisseur_id = f.id " +
                "LEFT JOIN local l ON ce.local_id = l.id " +
                "WHERE ce.id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (Statement fkStmt = connection.createStatement()) {
                fkStmt.execute("PRAGMA foreign_keys = ON;");
            }
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    CommandeExterne commandeExterne = extractCommandeExterneFromResultSet(rs);
                    commandeExterne.setCommandeExterneArticles(articleDAO.getByCommandeExterneId(id, connection));
                    commandeExterne.setCommandeExterneLocals(localDAO.getByCommandeExterneId(id, connection));
                    return commandeExterne;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving CommandeExterne by ID: " + id + ", Message: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<CommandeExterne> getAll() {
        List<CommandeExterne> commandes = new ArrayList<>();
        String sql = "SELECT ce.id, ce.created_at, ce.confirmed_at, ce.updated_at, ce.statut, " +
                "m.id AS magasinier_id, m.nom AS magasinier_nom, " +
                "f.id AS fournisseur_id, f.nom AS fournisseur_nom, " +
                "l.id AS local_id, l.nom AS local_nom " +
                "FROM commande_externe ce " +
                "LEFT JOIN magasinier m ON ce.magasinier_id = m.id " +
                "LEFT JOIN fournisseur f ON ce.fournisseur_id = f.id " +
                "LEFT JOIN local l ON ce.local_id = l.id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            try (Statement fkStmt = connection.createStatement()) {
                fkStmt.execute("PRAGMA foreign_keys = ON;");
            }
            while (rs.next()) {
                CommandeExterne commandeExterne = extractCommandeExterneFromResultSet(rs);
                commandeExterne.setCommandeExterneArticles(articleDAO.getByCommandeExterneId(commandeExterne.getId(), connection));
                commandeExterne.setCommandeExterneLocals(localDAO.getByCommandeExterneId(commandeExterne.getId(), connection));
                commandes.add(commandeExterne);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all CommandeExterne records: " + e.getMessage(), e);
        }
        return commandes;
    }

    @Override
    public void update(CommandeExterne commandeExterne) {
        if (commandeExterne == null || commandeExterne.getId() == null) {
            throw new IllegalArgumentException("CommandeExterne and its ID cannot be null");
        }

        String sql = "UPDATE commande_externe SET created_at = ?, confirmed_at = ?, updated_at = ?, statut = ?, magasinier_id = ?, fournisseur_id = ?, local_id = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (Statement fkStmt = connection.createStatement()) {
                    fkStmt.execute("PRAGMA foreign_keys = ON;");
                }
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    setCommandeExterneFields(stmt, commandeExterne);
                    stmt.setLong(8, commandeExterne.getId());
                    stmt.executeUpdate();
                }
                synchronizeArticles(commandeExterne, connection);
                synchronizeLocals(commandeExterne, connection);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException("Error updating CommandeExterne ID: " + commandeExterne.getId() + ", Message: " + e.getMessage(), e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error managing database connection: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid CommandeExterne ID: " + id);
        }

        String sql = "DELETE FROM commande_externe WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (Statement fkStmt = connection.createStatement()) {
                fkStmt.execute("PRAGMA foreign_keys = ON;");
            }
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting CommandeExterne ID: " + id + ", Message: " + e.getMessage(), e);
        }
    }

    private void setCommandeExterneFields(PreparedStatement stmt, CommandeExterne commandeExterne) throws SQLException {
        stmt.setString(1, commandeExterne.getCreerA() != null ? commandeExterne.getCreerA().format(SQLITE_DATETIME_FORMATTER) : LocalDateTime.now().format(SQLITE_DATETIME_FORMATTER));
        if (commandeExterne.getConfirmerA() != null) {
            stmt.setString(2, commandeExterne.getConfirmerA().format(SQLITE_DATETIME_FORMATTER));
        } else {
            stmt.setNull(2, Types.VARCHAR);
        }
        stmt.setString(3, LocalDateTime.now().format(SQLITE_DATETIME_FORMATTER));
        stmt.setString(4, commandeExterne.getStatut() != null ? commandeExterne.getStatut() : "En attente");
        stmt.setObject(5, commandeExterne.getMagasinier() != null ? commandeExterne.getMagasinier().getId() : null, Types.INTEGER);
        stmt.setObject(6, commandeExterne.getFournisseur() != null ? commandeExterne.getFournisseur().getId() : null, Types.INTEGER);
        stmt.setObject(7, commandeExterne.getLocal() != null ? commandeExterne.getLocal().getId() : null, Types.INTEGER);
    }

    private void insertArticles(CommandeExterne commandeExterne, Connection connection) throws SQLException {
        for (CommandeExterneArticle article : commandeExterne.getCommandeExterneArticles()) {
            if (article.getArticle() == null || !isArticleIdValid(article.getArticle().getId(), connection)) {
                throw new IllegalArgumentException("Invalid or non-existent Article ID: " + (article.getArticle() != null ? article.getArticle().getId() : "null"));
            }
            article.setCommandeExterne(commandeExterne);
            articleDAO.insert(article, connection);
        }
    }

    private void insertLocals(CommandeExterne commandeExterne, Connection connection) throws SQLException {
        for (CommandeExterneLocal local : commandeExterne.getCommandeExterneLocals()) {
            if (local.getLocal() == null || !isLocalIdValid(local.getLocal().getId(), connection)) {
                throw new IllegalArgumentException("Invalid or non-existent Local ID: " + (local.getLocal() != null ? local.getLocal().getId() : "null"));
            }
            local.setCommandeExterne(commandeExterne);
            localDAO.insert(local, connection);
        }
    }

    private void synchronizeArticles(CommandeExterne commandeExterne, Connection connection) throws SQLException {
        List<CommandeExterneArticle> existingArticles = articleDAO.getByCommandeExterneId(commandeExterne.getId(), connection);
        Map<Long, CommandeExterneArticle> existingArticleMap = new HashMap<>();
        for (CommandeExterneArticle existing : existingArticles) {
            existingArticleMap.put(existing.getArticle().getId(), existing);
        }

        for (CommandeExterneArticle newArticle : commandeExterne.getCommandeExterneArticles()) {
            if (newArticle.getArticle() == null || !isArticleIdValid(newArticle.getArticle().getId(), connection)) {
                throw new IllegalArgumentException("Invalid or non-existent Article ID: " + (newArticle.getArticle() != null ? newArticle.getArticle().getId() : "null"));
            }
            newArticle.setCommandeExterne(commandeExterne);
            CommandeExterneArticle existing = existingArticleMap.get(newArticle.getArticle().getId());
            if (existing == null) {
                articleDAO.insert(newArticle, connection);
            } else {
                existing.setQuantite(newArticle.getQuantite());
                existing.setCreatedAt(newArticle.getCreatedAt());
                existing.setUpdatedAt(LocalDateTime.now());
                articleDAO.update(existing, connection);
                existingArticleMap.remove(newArticle.getArticle().getId());
            }
        }

        for (CommandeExterneArticle toDelete : existingArticleMap.values()) {
            articleDAO.delete(toDelete.getId());
        }
    }

    private void synchronizeLocals(CommandeExterne commandeExterne, Connection connection) throws SQLException {
        List<CommandeExterneLocal> existingLocals = localDAO.getByCommandeExterneId(commandeExterne.getId(), connection);
        Map<Long, CommandeExterneLocal> existingLocalMap = new HashMap<>();
        for (CommandeExterneLocal existing : existingLocals) {
            existingLocalMap.put(existing.getLocal().getId(), existing);
        }

        for (CommandeExterneLocal newLocal : commandeExterne.getCommandeExterneLocals()) {
            if (newLocal.getLocal() == null || !isLocalIdValid(newLocal.getLocal().getId(), connection)) {
                throw new IllegalArgumentException("Invalid or non-existent Local ID: " + (newLocal.getLocal() != null ? newLocal.getLocal().getId() : "null"));
            }
            newLocal.setCommandeExterne(commandeExterne);
            CommandeExterneLocal existing = existingLocalMap.get(newLocal.getLocal().getId());
            if (existing == null) {
                localDAO.insert(newLocal, connection);
            } else {
                existing.setCreatedAt(newLocal.getCreatedAt());
                localDAO.update(existing, connection);
                existingLocalMap.remove(newLocal.getLocal().getId());
            }
        }

        for (CommandeExterneLocal toDelete : existingLocalMap.values()) {
            localDAO.delete(toDelete.getId());
        }
    }

    private CommandeExterne extractCommandeExterneFromResultSet(ResultSet rs) throws SQLException {
        CommandeExterne commandeExterne = new CommandeExterne();
        commandeExterne.setId(rs.getLong("id"));

        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null && !rs.wasNull()) {
            try {
                commandeExterne.setCreerA(LocalDateTime.parse(createdAtStr, SQLITE_DATETIME_FORMATTER));
            } catch (Exception e) {
                throw new SQLException("Error parsing created_at: " + createdAtStr + ", Message: " + e.getMessage(), e);
            }
        }

        String confirmedAtStr = rs.getString("confirmed_at");
        if (confirmedAtStr != null && !rs.wasNull()) {
            try {
                commandeExterne.setConfirmerA(LocalDateTime.parse(confirmedAtStr, SQLITE_DATETIME_FORMATTER));
            } catch (Exception e) {
                throw new SQLException("Error parsing confirmed_at: " + confirmedAtStr + ", Message: " + e.getMessage(), e);
            }
        }

        String updatedAtStr = rs.getString("updated_at");
        if (updatedAtStr != null && !rs.wasNull()) {
            try {
                commandeExterne.setMiseAjourA(LocalDateTime.parse(updatedAtStr, SQLITE_DATETIME_FORMATTER));
            } catch (Exception e) {
                throw new SQLException("Error parsing updated_at: " + updatedAtStr + ", Message: " + e.getMessage(), e);
            }
        }

        commandeExterne.setStatut(rs.getString("statut"));

        Long magasinierId = rs.getLong("magasinier_id");
        if (!rs.wasNull()) {
            Magasinier magasinier = new Magasinier();
            magasinier.setId(magasinierId);
            magasinier.setNom(rs.getString("magasinier_nom"));
            commandeExterne.setMagasinier(magasinier);
        }

        Long fournisseurId = rs.getLong("fournisseur_id");
        if (!rs.wasNull()) {
            Fournisseur fournisseur = new Fournisseur();
            fournisseur.setId(fournisseurId);
            fournisseur.setNom(rs.getString("fournisseur_nom"));
            commandeExterne.setFournisseur(fournisseur);
        }

        Long localId = rs.getLong("local_id");
        if (!rs.wasNull()) {
            Local local = new Local();
            local.setId(localId);
            local.setNom(rs.getString("local_nom"));
            commandeExterne.setLocal(local);
        }

        return commandeExterne;
    }

    private boolean isArticleIdValid(Long articleId, Connection connection) throws SQLException {
        if (articleId == null) return false;
        String sql = "SELECT 1 FROM article WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, articleId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean isLocalIdValid(Long localId, Connection connection) throws SQLException {
        if (localId == null) return false;
        String sql = "SELECT 1 FROM local WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, localId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}