package model.dao;

import model.entities.Stock;
import model.entities.Article;
import model.entities.Local;
import utils.DatabaseConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class StockDAOImpl implements StockDAO {
    private static final Logger LOGGER = Logger.getLogger(StockDAOImpl.class.getName());
    private static final DateTimeFormatter SQLITE_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void insert(Stock stock) {
        String sql = "INSERT INTO stock (quantite, created_at, updated_at, article_id, local_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, stock.getQuantite());
            stmt.setString(2, stock.getCreatedAt() != null ? stock.getCreatedAt().format(SQLITE_DATETIME_FORMATTER) : LocalDateTime.now().format(SQLITE_DATETIME_FORMATTER));
            stmt.setString(3, stock.getUpdatedAt() != null ? stock.getUpdatedAt().format(SQLITE_DATETIME_FORMATTER) : LocalDateTime.now().format(SQLITE_DATETIME_FORMATTER));
            stmt.setLong(4, stock.getArticle().getId());
            stmt.setLong(5, stock.getLocal().getId());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    stock.setId(rs.getLong(1));
                }
            }
            LOGGER.info("Inserted stock with ID: " + stock.getId());
        } catch (SQLException e) {
            LOGGER.severe("Erreur lors de l'insertion du stock: " + e.getMessage());
            throw new RuntimeException("Erreur lors de l'insertion du stock", e);
        }
    }

    @Override
    public Stock getById(Long id) {
        String sql = "SELECT s.id, s.quantite, s.created_at, s.updated_at, s.article_id, s.local_id, a.nom as article_nom, l.nom as local_nom " +
                "FROM stock s " +
                "JOIN article a ON s.article_id = a.id " +
                "JOIN local l ON s.local_id = l.id " +
                "WHERE s.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractStockFromResultSet(rs);
                }
            }
            LOGGER.info("Retrieved stock with ID: " + id);
        } catch (SQLException e) {
            LOGGER.severe("Erreur lors de la récupération du stock par ID: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération du stock par ID", e);
        }
        return null;
    }

    @Override
    public List<Stock> getAll() {
        List<Stock> stocks = new ArrayList<>();
        String sql = "SELECT s.id, s.quantite, s.created_at, s.updated_at, s.article_id, s.local_id, a.nom as article_nom, l.nom as local_nom " +
                "FROM stock s " +
                "JOIN article a ON s.article_id = a.id " +
                "JOIN local l ON s.local_id = l.id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                stocks.add(extractStockFromResultSet(rs));
            }
            LOGGER.info("Retrieved " + stocks.size() + " stocks");
        } catch (SQLException e) {
            LOGGER.severe("Erreur lors de la récupération des stocks: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des stocks", e);
        }
        return stocks;
    }

    @Override
    public void update(Stock stock) {
        String sql = "UPDATE stock SET quantite = ?, created_at = ?, updated_at = ?, article_id = ?, local_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, stock.getQuantite());
            stmt.setString(2, stock.getCreatedAt() != null ? stock.getCreatedAt().format(SQLITE_DATETIME_FORMATTER) : LocalDateTime.now().format(SQLITE_DATETIME_FORMATTER));
            stmt.setString(3, LocalDateTime.now().format(SQLITE_DATETIME_FORMATTER));
            stmt.setLong(4, stock.getArticle().getId());
            stmt.setLong(5, stock.getLocal().getId());
            stmt.setLong(6, stock.getId());

            stmt.executeUpdate();
            LOGGER.info("Updated stock with ID: " + stock.getId());
        } catch (SQLException e) {
            LOGGER.severe("Erreur lors de la mise à jour du stock: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la mise à jour du stock", e);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM stock WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
            LOGGER.info("Deleted stock with ID: " + id);
        } catch (SQLException e) {
            LOGGER.severe("Erreur lors de la suppression du stock: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la suppression du stock", e);
        }
    }

    private Stock extractStockFromResultSet(ResultSet rs) throws SQLException {
        Stock stock = new Stock();
        stock.setId(rs.getLong("id"));
        stock.setQuantite(rs.getInt("quantite"));

        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null && !rs.wasNull()) {
            try {
                stock.setCreatedAt(LocalDateTime.parse(createdAtStr, SQLITE_DATETIME_FORMATTER));
            } catch (Exception e) {
                throw new SQLException("Erreur lors du parsing de created_at: " + createdAtStr, e);
            }
        }

        String updatedAtStr = rs.getString("updated_at");
        if (updatedAtStr != null && !rs.wasNull()) {
            try {
                stock.setUpdatedAt(LocalDateTime.parse(updatedAtStr, SQLITE_DATETIME_FORMATTER));
            } catch (Exception e) {
                throw new SQLException("Erreur lors du parsing de updated_at: " + updatedAtStr, e);
            }
        }

        Long articleId = rs.getLong("article_id");
        String articleNom = rs.getString("article_nom");
        if (!rs.wasNull() && articleNom != null) {
            Article article = new Article();
            article.setId(articleId);
            article.setNom(articleNom);
            stock.setArticle(article);
        }

        Long localId = rs.getLong("local_id");
        String localNom = rs.getString("local_nom");
        if (!rs.wasNull() && localNom != null) {
            Local local = new Local();
            local.setId(localId);
            local.setNom(localNom);
            stock.setLocal(local);
        }

        return stock;
    }
}