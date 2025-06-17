package model.dao;

import model.entities.Article;
import model.entities.CommandeInterne;
import model.entities.CommandeInterneArticle;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of CommandeInterneArticleDAO for managing article associations with internal orders.
 */
public class CommandeInterneArticleDAOImpl implements CommandeInterneArticleDAO {
    private static final DateTimeFormatter SQLITE_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void insert(CommandeInterneArticle commandeInterneArticle) {
        if (commandeInterneArticle == null || commandeInterneArticle.getCommandeInterne() == null || commandeInterneArticle.getArticle() == null) {
            throw new IllegalArgumentException("CommandeInterneArticle, its CommandeInterne, and Article cannot be null");
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            insert(commandeInterneArticle, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting CommandeInterneArticle: " + e.getMessage(), e);
        }
    }

    public void insert(CommandeInterneArticle commandeInterneArticle, Connection conn) throws SQLException {
        if (commandeInterneArticle == null || commandeInterneArticle.getCommandeInterne() == null || commandeInterneArticle.getArticle() == null) {
            throw new IllegalArgumentException("CommandeInterneArticle, its CommandeInterne, and Article cannot be null");
        }

        String sql = "INSERT INTO commande_interne_article (commande_interne_id, article_id, quantite, etat, notes, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            try (Statement fkStmt = conn.createStatement()) {
                fkStmt.execute("PRAGMA foreign_keys = ON;");
            }
            stmt.setLong(1, commandeInterneArticle.getCommandeInterne().getId());
            stmt.setLong(2, commandeInterneArticle.getArticle().getId());
            stmt.setInt(3, commandeInterneArticle.getQuantite());
            stmt.setString(4, commandeInterneArticle.getEtat());
            stmt.setString(5, commandeInterneArticle.getNotes());
            stmt.setString(6, commandeInterneArticle.getCreatedAt() != null ?
                    commandeInterneArticle.getCreatedAt().format(SQLITE_DATETIME_FORMATTER) :
                    LocalDateTime.now().format(SQLITE_DATETIME_FORMATTER));

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    commandeInterneArticle.setId(rs.getLong(1));
                }
            }
        }
    }

    @Override
    public CommandeInterneArticle getById(Long id) {
        if (id == null || id <= 0) {
            return null;
        }

        String sql = "SELECT cia.id, cia.commande_interne_id, cia.article_id, cia.quantite, cia.etat, cia.notes, cia.created_at, " +
                "a.nom AS article_nom " +
                "FROM commande_interne_article cia " +
                "JOIN article a ON cia.article_id = a.id " +
                "WHERE cia.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (Statement fkStmt = conn.createStatement()) {
                fkStmt.execute("PRAGMA foreign_keys = ON;");
            }
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractCommandeInterneArticleFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving CommandeInterneArticle by ID: " + id + ", Message: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<CommandeInterneArticle> getByCommandeInterneId(Long commandeInterneId) {
        if (commandeInterneId == null || commandeInterneId <= 0) {
            return new ArrayList<>();
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            return getByCommandeInterneId(commandeInterneId, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving CommandeInterneArticles for commande_interne_id: " + commandeInterneId + ", Message: " + e.getMessage(), e);
        }
    }

    public List<CommandeInterneArticle> getByCommandeInterneId(Long commandeInterneId, Connection conn) throws SQLException {
        List<CommandeInterneArticle> articles = new ArrayList<>();
        String sql = "SELECT cia.id, cia.commande_interne_id, cia.article_id, cia.quantite, cia.etat, cia.notes, cia.created_at, " +
                "a.nom AS article_nom " +
                "FROM commande_interne_article cia " +
                "JOIN article a ON cia.article_id = a.id " +
                "WHERE cia.commande_interne_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (Statement fkStmt = conn.createStatement()) {
                fkStmt.execute("PRAGMA foreign_keys = ON;");
            }
            stmt.setLong(1, commandeInterneId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CommandeInterneArticle article = extractCommandeInterneArticleFromResultSet(rs);
                    articles.add(article);
                }
            }
        }
        return articles;
    }

    @Override
    public void update(CommandeInterneArticle commandeInterneArticle) {
        if (commandeInterneArticle == null || commandeInterneArticle.getId() == null || commandeInterneArticle.getCommandeInterne() == null || commandeInterneArticle.getArticle() == null) {
            throw new IllegalArgumentException("CommandeInterneArticle, its ID, CommandeInterne, and Article cannot be null");
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            update(commandeInterneArticle, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error updating CommandeInterneArticle ID: " + commandeInterneArticle.getId() + ", Message: " + e.getMessage(), e);
        }
    }

    public void update(CommandeInterneArticle commandeInterneArticle, Connection conn) throws SQLException {
        String sql = "UPDATE commande_interne_article SET commande_interne_id = ?, article_id = ?, quantite = ?, etat = ?, notes = ?, created_at = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, commandeInterneArticle.getCommandeInterne().getId());
            stmt.setLong(2, commandeInterneArticle.getArticle().getId());
            stmt.setInt(3, commandeInterneArticle.getQuantite());
            stmt.setString(4, commandeInterneArticle.getEtat());
            stmt.setString(5, commandeInterneArticle.getNotes());
            stmt.setString(6, commandeInterneArticle.getCreatedAt() != null ?
                    commandeInterneArticle.getCreatedAt().format(SQLITE_DATETIME_FORMATTER) :
                    LocalDateTime.now().format(SQLITE_DATETIME_FORMATTER));
            stmt.setLong(7, commandeInterneArticle.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid CommandeInterneArticle ID: " + id);
        }

        String sql = "DELETE FROM commande_interne_article WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (Statement fkStmt = conn.createStatement()) {
                fkStmt.execute("PRAGMA foreign_keys = ON;");
            }
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting CommandeInterneArticle ID: " + id + ", Message: " + e.getMessage(), e);
        }
    }

    private CommandeInterneArticle extractCommandeInterneArticleFromResultSet(ResultSet rs) throws SQLException {
        CommandeInterneArticle commandeInterneArticle = new CommandeInterneArticle();
        commandeInterneArticle.setId(rs.getLong("id"));

        CommandeInterne commandeInterne = new CommandeInterne();
        commandeInterne.setId(rs.getLong("commande_interne_id"));
        commandeInterneArticle.setCommandeInterne(commandeInterne);

        Article article = new Article();
        article.setId(rs.getLong("article_id"));
        article.setNom(rs.getString("article_nom"));
        commandeInterneArticle.setArticle(article);

        commandeInterneArticle.setQuantite(rs.getInt("quantite"));
        commandeInterneArticle.setEtat(rs.getString("etat"));
        commandeInterneArticle.setNotes(rs.getString("notes"));

        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null && !rs.wasNull()) {
            try {
                commandeInterneArticle.setCreatedAt(LocalDateTime.parse(createdAtStr, SQLITE_DATETIME_FORMATTER));
            } catch (Exception e) {
                throw new SQLException("Error parsing created_at: " + createdAtStr + ", Message: " + e.getMessage(), e);
            }
        }

        return commandeInterneArticle;
    }
}