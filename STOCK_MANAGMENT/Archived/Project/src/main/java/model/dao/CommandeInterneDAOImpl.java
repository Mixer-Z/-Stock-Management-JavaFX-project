package model.dao;

import model.entities.CommandeInterne;
import model.entities.Magasinier;
import model.entities.Consommateur;
import model.entities.Local;
import model.entities.CommandeInterneArticle;
import model.entities.CommandeInterneLocal;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of CommandeInterneDAO for managing internal orders in the database.
 */
public class CommandeInterneDAOImpl implements CommandeInterneDAO {
    private static final DateTimeFormatter SQLITE_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final CommandeInterneArticleDAO articleDAO;
    private final CommandeInterneLocalDAO localDAO;

    public CommandeInterneDAOImpl() {
        this.articleDAO = new CommandeInterneArticleDAOImpl();
        this.localDAO = new CommandeInterneLocalDAOImpl();
    }

    @Override
    public void insert(CommandeInterne commandeInterne) {
        if (commandeInterne == null) {
            throw new IllegalArgumentException("CommandeInterne cannot be null");
        }

        String sql = "INSERT INTO commande_interne (created_at, confirmed_at, updated_at, statut, magasinier_id, consommateur_id, local_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (Statement fkStmt = connection.createStatement()) {
                    fkStmt.execute("PRAGMA foreign_keys = ON;");
                }

                try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    setCommandeInterneFields(stmt, commandeInterne);
                    stmt.executeUpdate();

                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            commandeInterne.setId(rs.getLong(1));
                        }
                    }
                }

                insertArticles(commandeInterne, connection);
                insertLocals(commandeInterne, connection);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException("Error inserting CommandeInterne: " + e.getMessage(), e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error managing database connection: " + e.getMessage(), e);
        }
    }

    @Override
    public CommandeInterne getById(Long id) {
        if (id == null || id <= 0) {
            return null;
        }

        String sql = "SELECT ci.id, ci.created_at, ci.confirmed_at, ci.updated_at, ci.statut, " +
                "m.id AS magasinier_id, m.nom AS magasinier_nom, " +
                "c.id AS consommateur_id, c.nom AS consommateur_nom, " +
                "l.id AS local_id, l.nom AS local_nom " +
                "FROM commande_interne ci " +
                "LEFT JOIN magasinier m ON ci.magasinier_id = m.id " +
                "LEFT JOIN consommateur c ON ci.consommateur_id = c.id " +
                "LEFT JOIN local l ON ci.local_id = l.id " +
                "WHERE ci.id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (Statement fkStmt = connection.createStatement()) {
                fkStmt.execute("PRAGMA foreign_keys = ON;");
            }
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    CommandeInterne commandeInterne = extractCommandeInterneFromResultSet(rs);
                    commandeInterne.setCommandeInterneArticles(articleDAO.getByCommandeInterneId(id, connection));
                    commandeInterne.setCommandeInterneLocals(localDAO.getByCommandeInterneId(id, connection));
                    return commandeInterne;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving CommandeInterne by ID: " + id + ", Message: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<CommandeInterne> getAll() {
        List<CommandeInterne> commandes = new ArrayList<>();
        String sql = "SELECT ci.id, ci.created_at, ci.confirmed_at, ci.updated_at, ci.statut, " +
                "m.id AS magasinier_id, m.nom AS magasinier_nom, " +
                "c.id AS consommateur_id, c.nom AS consommateur_nom, " +
                "l.id AS local_id, l.nom AS local_nom " +
                "FROM commande_interne ci " +
                "LEFT JOIN magasinier m ON ci.magasinier_id = m.id " +
                "LEFT JOIN consommateur c ON ci.consommateur_id = c.id " +
                "LEFT JOIN local l ON ci.local_id = l.id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                CommandeInterne commandeInterne = extractCommandeInterneFromResultSet(rs);
                commandes.add(commandeInterne);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all CommandeInterne records: " + e.getMessage(), e);
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            for (CommandeInterne commandeInterne : commandes) {
                commandeInterne.setCommandeInterneArticles(articleDAO.getByCommandeInterneId(commandeInterne.getId(), connection));
                commandeInterne.setCommandeInterneLocals(localDAO.getByCommandeInterneId(commandeInterne.getId(), connection));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading articles or locals for CommandeInterne records: " + e.getMessage(), e);
        }

        return commandes;
    }

    @Override
    public void update(CommandeInterne commandeInterne) {
        String sql = "UPDATE commande_interne SET statut = ?, magasinier_id = ?, consommateur_id = ?, created_at = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, commandeInterne.getStatut());
                pstmt.setLong(2, commandeInterne.getMagasinier().getId());
                pstmt.setObject(3, commandeInterne.getConsommateur() != null ? commandeInterne.getConsommateur().getId() : null, Types.INTEGER);
                pstmt.setString(4, commandeInterne.getCreerA() != null ? commandeInterne.getCreerA().format(SQLITE_DATETIME_FORMATTER) : null);
                pstmt.setLong(5, commandeInterne.getId());
                pstmt.executeUpdate();
            }
            synchronizeLocals(commandeInterne, connection);
            synchronizeArticles(commandeInterne, connection);
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating commande: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid CommandeInterne ID: " + id);
        }

        String sql = "DELETE FROM commande_interne WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (Statement fkStmt = connection.createStatement()) {
                fkStmt.execute("PRAGMA foreign_keys = ON;");
            }
            stmt.setLong(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Deleted CommandeInterne ID: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting CommandeInterne ID: " + id + ", Message: " + e.getMessage(), e);
        }
    }

    private void setCommandeInterneFields(PreparedStatement stmt, CommandeInterne commandeInterne) throws SQLException {
        stmt.setString(1, commandeInterne.getCreerA() != null ? commandeInterne.getCreerA().format(SQLITE_DATETIME_FORMATTER) : LocalDateTime.now().format(SQLITE_DATETIME_FORMATTER));
        if (commandeInterne.getConfirmerA() != null) {
            stmt.setString(2, commandeInterne.getConfirmerA().format(SQLITE_DATETIME_FORMATTER));
        } else {
            stmt.setNull(2, Types.VARCHAR);
        }
        stmt.setString(3, LocalDateTime.now().format(SQLITE_DATETIME_FORMATTER));
        stmt.setString(4, commandeInterne.getStatut() != null ? commandeInterne.getStatut() : "En attente");

        Long magasinierId = commandeInterne.getMagasinier() != null ? commandeInterne.getMagasinier().getId() : null;
        stmt.setObject(5, magasinierId, Types.INTEGER);
        System.out.println("Setting magasinier_id: " + magasinierId);

        Long consommateurId = commandeInterne.getConsommateur() != null ? commandeInterne.getConsommateur().getId() : null;
        stmt.setObject(6, consommateurId, Types.INTEGER);
        System.out.println("Setting consommateur_id: " + consommateurId);

        Long localId = commandeInterne.getLocal() != null ? commandeInterne.getLocal().getId() : null;
        stmt.setObject(7, localId, Types.INTEGER);
        System.out.println("Setting local_id: " + localId);
    }

    private void insertArticles(CommandeInterne commandeInterne, Connection connection) throws SQLException {
        for (CommandeInterneArticle article : commandeInterne.getCommandeInterneArticles()) {
            if (article.getArticle() == null || !isArticleIdValid(article.getArticle().getId(), connection)) {
                throw new IllegalArgumentException("Invalid or non-existent Article ID: " + (article.getArticle() != null ? article.getArticle().getId() : "null"));
            }
            article.setCommandeInterne(commandeInterne);
            articleDAO.insert(article, connection);
        }
    }

    private void insertLocals(CommandeInterne commandeInterne, Connection connection) throws SQLException {
        for (CommandeInterneLocal local : commandeInterne.getCommandeInterneLocals()) {
            if (local.getLocal() == null || !isLocalIdValid(local.getLocal().getId(), connection)) {
                throw new IllegalArgumentException("Invalid or non-existent Local ID: " + (local.getLocal() != null ? local.getLocal().getId() : "null"));
            }
            local.setCommandeInterne(commandeInterne);
            localDAO.insert(local, connection);
        }
    }

    private void synchronizeArticles(CommandeInterne commandeInterne, Connection connection) throws SQLException {
        List<CommandeInterneArticle> existingArticles = articleDAO.getByCommandeInterneId(commandeInterne.getId(), connection);
        Map<Long, CommandeInterneArticle> existingArticleMap = new HashMap<>();
        for (CommandeInterneArticle existing : existingArticles) {
            existingArticleMap.put(existing.getArticle().getId(), existing);
        }

        for (CommandeInterneArticle newArticle : commandeInterne.getCommandeInterneArticles()) {
            if (newArticle.getArticle() == null || !isArticleIdValid(newArticle.getArticle().getId(), connection)) {
                throw new IllegalArgumentException("Invalid or non-existent Article ID: " + (newArticle.getArticle() != null ? newArticle.getArticle().getId() : "null"));
            }
            newArticle.setCommandeInterne(commandeInterne);
            CommandeInterneArticle existing = existingArticleMap.get(newArticle.getArticle().getId());
            if (existing == null) {
                articleDAO.insert(newArticle, connection);
            } else {
                existing.setQuantite(newArticle.getQuantite());
                existing.setEtat(newArticle.getEtat());
                existing.setNotes(newArticle.getNotes());
                existing.setCreatedAt(newArticle.getCreatedAt());
                articleDAO.update(existing, connection);
                existingArticleMap.remove(newArticle.getArticle().getId());
            }
        }

        for (CommandeInterneArticle toDelete : existingArticleMap.values()) {
            articleDAO.delete(toDelete.getId());
        }
    }

    private void synchronizeLocals(CommandeInterne commandeInterne, Connection connection) throws SQLException {
        List<CommandeInterneLocal> existingLocals = localDAO.getByCommandeInterneId(commandeInterne.getId(), connection);
        Map<Long, CommandeInterneLocal> existingLocalMap = new HashMap<>();
        for (CommandeInterneLocal existing : existingLocals) {
            existingLocalMap.put(existing.getLocal().getId(), existing);
        }

        for (CommandeInterneLocal newLocal : commandeInterne.getCommandeInterneLocals()) {
            if (newLocal.getLocal() == null || !isLocalIdValid(newLocal.getLocal().getId(), connection)) {
                throw new IllegalArgumentException("Invalid or non-existent Local ID: " + (newLocal.getLocal() != null ? newLocal.getLocal().getId() : "null"));
            }
            newLocal.setCommandeInterne(commandeInterne);
            CommandeInterneLocal existing = existingLocalMap.get(newLocal.getLocal().getId());
            if (existing == null) {
                localDAO.insert(newLocal, connection);
            } else {
                existing.setNotes(newLocal.getNotes());
                existing.setCreatedAt(newLocal.getCreatedAt());
                localDAO.update(existing, connection);
                existingLocalMap.remove(newLocal.getLocal().getId());
            }
        }

        for (CommandeInterneLocal toDelete : existingLocalMap.values()) {
            localDAO.delete(toDelete.getId());
        }
    }

    private CommandeInterne extractCommandeInterneFromResultSet(ResultSet rs) throws SQLException {
        CommandeInterne commandeInterne = new CommandeInterne();
        commandeInterne.setId(rs.getLong("id"));

        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null && !rs.wasNull()) {
            try {
                commandeInterne.setCreerA(LocalDateTime.parse(createdAtStr, SQLITE_DATETIME_FORMATTER));
            } catch (Exception e) {
                throw new SQLException("Error parsing created_at: " + createdAtStr + ", Message: " + e.getMessage(), e);
            }
        }

        String confirmedAtStr = rs.getString("confirmed_at");
        if (confirmedAtStr != null && !rs.wasNull()) {
            try {
                commandeInterne.setConfirmerA(LocalDateTime.parse(confirmedAtStr, SQLITE_DATETIME_FORMATTER));
            } catch (Exception e) {
                throw new SQLException("Error parsing confirmed_at: " + confirmedAtStr + ", Message: " + e.getMessage(), e);
            }
        }

        String updatedAtStr = rs.getString("updated_at");
        if (updatedAtStr != null && !rs.wasNull()) {
            try {
                commandeInterne.setMiseAjourA(LocalDateTime.parse(updatedAtStr, SQLITE_DATETIME_FORMATTER));
            } catch (Exception e) {
                throw new SQLException("Error parsing updated_at: " + updatedAtStr + ", Message: " + e.getMessage(), e);
            }
        }

        commandeInterne.setStatut(rs.getString("statut"));

        Long magasinierId = rs.getLong("magasinier_id");
        System.out.println("Retrieved magasinier_id: " + magasinierId + ", wasNull: " + rs.wasNull());
        if (!rs.wasNull()) {
            Magasinier magasinier = new Magasinier();
            magasinier.setId(magasinierId);
            magasinier.setNom(rs.getString("magasinier_nom"));
            commandeInterne.setMagasinier(magasinier);
        }

        Long consommateurId = rs.getLong("consommateur_id");
        System.out.println("Retrieved consommateur_id: " + consommateurId + ", wasNull: " + rs.wasNull());
        if (!rs.wasNull()) {
            Consommateur consommateur = new Consommateur();
            consommateur.setId(consommateurId);
            consommateur.setNom(rs.getString("consommateur_nom"));
            commandeInterne.setConsommateur(consommateur);
        }

        Long localId = rs.getLong("local_id");
        System.out.println("Retrieved local_id: " + localId + ", wasNull: " + rs.wasNull());
        if (!rs.wasNull()) {
            Local local = new Local();
            local.setId(localId);
            local.setNom(rs.getString("local_nom"));
            commandeInterne.setLocal(local);
        }

        return commandeInterne;
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
        if (connection.isClosed()) {
            throw new IllegalStateException("Connection is closed before executing isLocalIdValid");
        }
        String sql = "SELECT 1 FROM local WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, localId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}