package test;

import model.dao.StockDAO;
import model.dao.StockDAOImpl;
import model.entities.Stock;
import model.entities.Article;
import model.entities.Local;

public class TestStockDAO {
    public static void main(String[] args) {
        StockDAO dao = new StockDAOImpl();

        // Debug: Print database URL
        try {
            System.out.println("Database URL: " + utils.DatabaseConnection.getConnection().getMetaData().getURL());
        } catch (Exception e) {
            System.out.println("Erreur lors de la récupération de l'URL de la base de données: " + e.getMessage());
        }

        // Create test entities
        Article article = new Article();
        article.setId(1L); // Assumes article with ID 1 exists
        Local local = new Local();
        local.setId(1L); // Assumes local with ID 1 exists

        // Test insert
        Stock stock = new Stock(100, article, local);
        dao.insert(stock);
        System.out.println("✅ Stock inséré : " + stock.getId());
        System.out.println("CreatedAt: " + stock.getCreatedAt());

        // Test getById
        Stock fetched = dao.getById(stock.getId());
        System.out.println("📦 Récupéré : " + fetched);

        // Test update
        fetched.setQuantite(150);
        dao.update(fetched);
        System.out.println("✏️ Stock mis à jour.");
        System.out.println("UpdatedAt: " + fetched.getUpdatedAt());

        // Test delete
        dao.delete(fetched.getId());
        System.out.println("🗑️ Stock supprimé.");
    }
}