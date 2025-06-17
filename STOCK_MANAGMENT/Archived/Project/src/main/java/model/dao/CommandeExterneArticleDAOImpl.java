package model.dao;

import model.entities.CommandeExterne;
import model.entities.CommandeExterneArticle;
import model.entities.Article;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of CommandeExterneArticleDAO for managing article associations with external orders.
 */
public class CommandeExterneArticleDAOImpl implements CommandeExterneArticleDAO {
    private static final DateTimeFormatter SQLITE_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void insert(CommandeExterneArticle commandeExterneArticle) {
        if (commandeExterneArticle == null || commandeExterneArticle.getCommandeExterne() == null || commandeExterneArticle.getArticle() == null) {
            throw new IllegalArgumentException("CommandeExterneArticle, its CommandeExterne, and Article cannot be null");
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            insert(commandeExterneArticle, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting CommandeExterneArticle: " + e.getMessage(), e);
        }
    }

    @Override
    public void insert(CommandeExterneArticle commandeExterneArticle, Connection conn) throws SQLException {
        if (commandeExterneArticle == null || commandeExterneArticle.getCommandeExterne() == null || commandeExterneArticle.getArticle() == null) {
            throw new IllegalArgumentException("CommandeExterneArticle, its CommandeExterne, and Article cannot be null");
        }

        String sql = "INSERT INTO commande_externe_article (commande_externe_id, article_id, quantite, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            try (Statement fkStmt = conn.createStatement()) {
                fkStmt.execute("PRAGMA foreign_keys = ON;");
            }
            stmt.setLong(1, commandeExterneArticle.getCommandeExterne().getId());
            stmt.setLong(2, commandeExterneArticle.getArticle().getId());
            stmt.setInt(3, commandeExterneArticle.getQuantite());
            stmt.setString(4, commandeExterneArticle.getCreatedAt() != null ?
                    commandeExterneArticle.getCreatedAt().format(SQLITE_DATETIME_FORMATTER) :
                    LocalDateTime.now().format(SQLITE_DATETIME_FORMATTER));
            stmt.setString(5, commandeExterneArticle.getUpdatedAt() != null ?
                    commandeExterneArticle.getUpdatedAt().format(SQLITE_DATETIME_FORMATTER) :
                    LocalDateTime.now().format(SQLITE_DATETIME_FORMATTER));

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    commandeExterneArticle.setId(rs.getLong(1));
                }
            }
        }
    }

    @Override
    public CommandeExterneArticle getById(Long id) {
        if (id == null || id <= 0) {
            return null;
        }

        String sql = "SELECT cea.id, cea.commande_externe_id, cea.article_id, cea.quantite, cea.created_at, cea.updated_at, " +
                "a.nom AS article_nom " +
                "FROM commande_externe_article cea " +
                "JOIN article a ON cea.article_id = a.id " +
                "WHERE cea.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (Statement fkStmt = conn.createStatement()) {
                fkStmt.execute("PRAGMA foreign_keys = ON;");
            }
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractCommandeExterneArticleFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving CommandeExterneArticle by ID: " + id + ", Message: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<CommandeExterneArticle> getByCommandeExterneId(Long commandeExterneId) {
        if (commandeExterneId == null || commandeExterneId <= 0) {
            return new ArrayList<>();
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            return getByCommandeExterneId(commandeExterneId, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving CommandeExterneArticles for commande_externe_id: " + commandeExterneId + ", Message: " + e.getMessage(), e);
        }
    }

    @Override
    public List<CommandeExterneArticle> getByCommandeExterneId(Long commandeExterneId, Connection conn) throws SQLException {
        List<CommandeExterneArticle> articles = new ArrayList<>();
        String sql = "SELECT cea.id, cea.commande_externe_id, cea.article_id, cea.quantite, cea.created_at, cea.updated_at, " +
                "a.nom AS article_nom " +
                "FROM commande_externe_article cea " +
                "JOIN article a ON cea.article_id = a.id " +
                "WHERE cea.commande_externe_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (Statement fkStmt = conn.createStatement()) {
                fkStmt.execute("PRAGMA foreign_keys = ON;");
            }
            stmt.setLong(1, commandeExterneId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CommandeExterneArticle article = extractCommandeExterneArticleFromResultSet(rs);
                    articles.add(article);
                }
            }
        }
        return articles;
    }

    @Override
    public void update(CommandeExterneArticle commandeExterneArticle) {
        if (commandeExterneArticle == null || commandeExterneArticle.getId() == null || commandeExterneArticle.getCommandeExterne() == null || commandeExterneArticle.getArticle() == null) {
            throw new IllegalArgumentException("CommandeExterneArticle, its ID, CommandeExterne, and Article cannot be null");
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            update(commandeExterneArticle, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error updating CommandeExterneArticle ID: " + commandeExterneArticle.getId() + ", Message: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(CommandeExterneArticle commandeExterneArticle, Connection conn) throws SQLException {
        String sql = "UPDATE commande_externe_article SET commande_externe_id = ?, article_id = ?, quantite = ?, created_at = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, commandeExterneArticle.getCommandeExterne().getId());
            stmt.setLong(2, commandeExterneArticle.getArticle().getId());
            stmt.setInt(3, commandeExterneArticle.getQuantite());
            stmt.setString(4, commandeExterneArticle.getCreatedAt() != null ?
                    commandeExterneArticle.getCreatedAt().format(SQLITE_DATETIME_FORMATTER) :
                    LocalDateTime.now().format(SQLITE_DATETIME_FORMATTER));
            stmt.setString(5, LocalDateTime.now().format(SQLITE_DATETIME_FORMATTER));
            stmt.setLong(6, commandeExterneArticle.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid CommandeExterneArticle ID: " + id);
        }

        String sql = "DELETE FROM commande_externe_article WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (Statement fkStmt = conn.createStatement()) {
                fkStmt.execute("PRAGMA foreign_keys = ON;");
            }
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting CommandeExterneArticle ID: " + id + ", Message: " + e.getMessage(), e);
        }
    }

    private CommandeExterneArticle extractCommandeExterneArticleFromResultSet(ResultSet rs) throws SQLException {
        CommandeExterneArticle commandeExterneArticle = new CommandeExterneArticle();
        commandeExterneArticle.setId(rs.getLong("id"));

        CommandeExterne commandeExterne = new CommandeExterne();
        commandeExterne.setId(rs.getLong("commande_externe_id"));
        commandeExterneArticle.setCommandeExterne(commandeExterne);

        Article article = new Article();
        article.setId(rs.getLong("article_id"));
        article.setNom(rs.getString("article_nom"));
        commandeExterneArticle.setArticle(article);

        commandeExterneArticle.setQuantite(rs.getInt("quantite"));

        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null && !rs.wasNull()) {
            try {
                commandeExterneArticle.setCreatedAt(LocalDateTime.parse(createdAtStr, SQLITE_DATETIME_FORMATTER));
            } catch (Exception e) {
                throw new SQLException("Error parsing created_at: " + createdAtStr + ", Message: " + e.getMessage(), e);
            }
        }

        String updatedAtStr = rs.getString("updated_at");
        if (updatedAtStr != null && !rs.wasNull()) {
            try {
                commandeExterneArticle.setUpdatedAt(LocalDateTime.parse(updatedAtStr, SQLITE_DATETIME_FORMATTER));
            } catch (Exception e) {
                throw new SQLException("Error parsing updated_at: " + updatedAtStr + ", Message: " + e.getMessage(), e);
            }
        }

        return commandeExterneArticle;
    }
}