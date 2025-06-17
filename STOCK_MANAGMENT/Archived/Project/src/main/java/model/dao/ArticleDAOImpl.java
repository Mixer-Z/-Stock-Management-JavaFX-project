package model.dao;

import model.entities.Article;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class ArticleDAOImpl implements ArticleDAO {

    private static final DateTimeFormatter SQLITE_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void insert(Article article) {
        String sql = "INSERT INTO article (reference, nom, categorie, stock_minimal, date_peremption, est_critique, est_consommable, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, article.getReference());
            stmt.setString(2, article.getNom());
            stmt.setString(3, article.getCategorie());
            stmt.setInt(4, article.getStockMinimal());
            if (article.getDatePeremption() != null) {
                stmt.setString(5, article.getDatePeremption().format(SQLITE_DATETIME_FORMATTER));
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }
            stmt.setInt(6, article.isEstCritique() ? 1 : 0);
            stmt.setInt(7, article.isEstConsommable() ? 1 : 0);
            stmt.setString(8, article.getCreatedAt().format(SQLITE_DATETIME_FORMATTER));
            stmt.setString(9, article.getUpdatedAt().format(SQLITE_DATETIME_FORMATTER));

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                article.setId(rs.getLong(1));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'insertion de l'article: " + e.getMessage(), e);
        }
    }

    @Override
    public Article getById(Long id) {
        String sql = "SELECT * FROM article WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractArticleFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de l'article par ID: " + e.getMessage(), e);
        }
        return null;
    }

    private Article extractArticleFromResultSet(ResultSet rs) throws SQLException {
        Article article = new Article();
        long articleId = rs.getLong("id");
        article.setId(articleId);
        article.setReference(rs.getString("reference"));
        article.setNom(rs.getString("nom"));
        article.setCategorie(rs.getString("categorie"));
        article.setStockMinimal(rs.getInt("stock_minimal"));

        String datePeremptionStr = rs.getString("date_peremption");
        if (datePeremptionStr != null && !rs.wasNull()) {
            try {
                article.setDatePeremption(LocalDateTime.parse(datePeremptionStr, SQLITE_DATETIME_FORMATTER));
            } catch (DateTimeParseException e) {
                System.err.println("Erreur lors du parsing de date_peremption: '" + datePeremptionStr + "' pour article ID: " + articleId);
                article.setDatePeremption(null);
            }
        }

        article.setEstCritique(rs.getInt("est_critique") == 1);
        article.setEstConsommable(rs.getInt("est_consommable") == 1);

        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null && !rs.wasNull()) {
            try {
                article.setCreatedAt(LocalDateTime.parse(createdAtStr, SQLITE_DATETIME_FORMATTER));
            } catch (DateTimeParseException e) {
                System.err.println("Erreur lors du parsing de created_at: '" + createdAtStr + "' pour article ID: " + articleId);
                article.setCreatedAt(LocalDateTime.now());
            }
        } else {
            System.err.println("created_at est NULL pour article ID: " + articleId);
            article.setCreatedAt(LocalDateTime.now());
        }

        String updatedAtStr = rs.getString("updated_at");
        if (updatedAtStr != null && !rs.wasNull()) {
            try {
                article.setUpdatedAt(LocalDateTime.parse(updatedAtStr, SQLITE_DATETIME_FORMATTER));
            } catch (DateTimeParseException e) {
                System.err.println("Erreur lors du parsing de updated_at: '" + updatedAtStr + "' pour article ID: " + articleId);
                article.setUpdatedAt(LocalDateTime.now());
            }
        } else {
            System.err.println("updated_at est NULL pour article ID: " + articleId);
            article.setUpdatedAt(LocalDateTime.now());
        }

        return article;
    }

    @Override
    public List<Article> getAll() {
        List<Article> articles = new ArrayList<>();
        String sql = "SELECT * FROM article";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                try {
                    articles.add(extractArticleFromResultSet(rs));
                } catch (SQLException e) {
                    long articleId = rs.getLong("id");
                    System.err.println("Erreur lors de l'extraction de l'article ID: " + articleId + " - " + e.getMessage());
                    e.printStackTrace();
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la récupération des articles: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Nombre d'articles récupérés: " + articles.size());
        return articles;
    }

    @Override
    public void update(Article article) {
        String sql = "UPDATE article SET reference = ?, nom = ?, categorie = ?, stock_minimal = ?, date_peremption = ?, " +
                "est_critique = ?, est_consommable = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, article.getReference());
            stmt.setString(2, article.getNom());
            stmt.setString(3, article.getCategorie());
            stmt.setInt(4, article.getStockMinimal());
            if (article.getDatePeremption() != null) {
                stmt.setString(5, article.getDatePeremption().format(SQLITE_DATETIME_FORMATTER));
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }
            stmt.setInt(6, article.isEstCritique() ? 1 : 0);
            stmt.setInt(7, article.isEstConsommable() ? 1 : 0);
            stmt.setString(8, LocalDateTime.now().format(SQLITE_DATETIME_FORMATTER));
            stmt.setLong(9, article.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de l'article: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM article WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de l'article: " + e.getMessage(), e);
        }
    }
}