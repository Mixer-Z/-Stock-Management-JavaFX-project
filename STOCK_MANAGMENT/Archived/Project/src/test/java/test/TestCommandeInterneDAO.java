package test;

import model.dao.CommandeInterneDAO;
import model.dao.CommandeInterneDAOImpl;
import model.entities.CommandeInterne;
import model.entities.Magasinier;
import model.entities.Consommateur;
import model.entities.Local;
import model.entities.CommandeInterneArticle;
import model.entities.CommandeInterneLocal;
import model.entities.Article;
import utils.DatabaseConnection;
import java.sql.*;
import java.time.LocalDateTime;

public class TestCommandeInterneDAO {
    public static void main(String[] args) {
        // Initialize test data
        initializeTestData();

        CommandeInterneDAO dao = new CommandeInterneDAOImpl();

        // Debug: Print database URL
        try {
            System.out.println("Database URL: " + DatabaseConnection.getConnection().getMetaData().getURL());
        } catch (Exception e) {
            System.out.println("Erreur lors de la récupération de l'URL de la base de données: " + e.getMessage());
        }

        // Create test entities
        Magasinier magasinier = new Magasinier();
        magasinier.setId(1L);
        Consommateur consommateur = new Consommateur();
        consommateur.setId(1L);
        Local local = new Local();
        local.setId(1L);
        Article article1 = new Article();
        article1.setId(1L);
        Article article2 = new Article();
        article2.setId(2L);

        // Create CommandeInterne with related entities
        CommandeInterne commande = new CommandeInterne(null, "En attente", magasinier, consommateur, local);
        CommandeInterneArticle article = new CommandeInterneArticle(commande, article1, 10, "Bon état", "Article note");
        commande.getCommandeInterneArticles().add(article);
        CommandeInterneLocal local1 = new CommandeInterneLocal(commande, local, "Local note");
        commande.getCommandeInterneLocals().add(local1);

        // Test insert
        dao.insert(commande);
        System.out.println("✅ Commande interne insérée : " + commande.getId());
        System.out.println("CreerA: " + commande.getCreerA());
        System.out.println("Articles: " + commande.getCommandeInterneArticles().size());
        System.out.println("Locals: " + commande.getCommandeInterneLocals().size());

        // Test getById
        CommandeInterne fetched = dao.getById(commande.getId());
        System.out.println("📦 Récupérée : " + fetched);
        System.out.println("Articles récupérés: " + fetched.getCommandeInterneArticles().size());
        System.out.println("Locals récupérés: " + fetched.getCommandeInterneLocals().size());

        // Test update
        fetched.setStatut("Validée");
        fetched.setConfirmerA(LocalDateTime.now());
        fetched.getCommandeInterneArticles().get(0).setQuantite(20);
        fetched.getCommandeInterneArticles().get(0).setEtat("Endommagé");
        CommandeInterneArticle newArticle = new CommandeInterneArticle(fetched, article2, 5, "Bon état", "New article");
        fetched.getCommandeInterneArticles().add(newArticle);
        fetched.getCommandeInterneLocals().get(0).setNotes("Updated note");
        dao.update(fetched);
        System.out.println("✏️ Commande interne mise à jour.");
        System.out.println("MiseAjourA: " + fetched.getMiseAjourA());

        // Verify update
        fetched = dao.getById(fetched.getId());
        System.out.println("📦 Après mise à jour : " + fetched);
        System.out.println("Articles après mise à jour: " + fetched.getCommandeInterneArticles().size());
        System.out.println("Locals après mise à jour: " + fetched.getCommandeInterneLocals().size());

        // Test delete
        Long commandeId = fetched.getId();
        dao.delete(commandeId);
        System.out.println("🗑️ Commande interne supprimée.");

        // Verify deletion
        verifyDeletion(commandeId);
    }

    private static void initializeTestData() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute("INSERT OR IGNORE INTO magasinier (id, nom) VALUES (1, 'Test Magasinier')");
            stmt.execute("INSERT OR IGNORE INTO consommateur (id, nom) VALUES (1, 'Test Consommateur')");
            stmt.execute("INSERT OR IGNORE INTO local (id, nom, emplacement) VALUES (1, 'Test Local', 'Bâtiment A')");
            stmt.execute("INSERT OR IGNORE INTO article (id, nom) VALUES (1, 'Test Article 1')");
            stmt.execute("INSERT OR IGNORE INTO article (id, nom) VALUES (2, 'Test Article 2')");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'initialisation des données de test", e);
        }
    }

    private static void verifyDeletion(Long commandeId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check commande_interne
            String sql = "SELECT COUNT(*) FROM commande_interne WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, commandeId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        System.out.println("✅ Commande interne supprimée de la table commande_interne.");
                    } else {
                        System.out.println("❌ Commande interne toujours présente dans commande_interne.");
                    }
                }
            }

            // Check commande_interne_article
            sql = "SELECT COUNT(*) FROM commande_interne_article WHERE commande_interne_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, commandeId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        System.out.println("✅ Articles supprimés de la table commande_interne_article.");
                    } else {
                        System.out.println("❌ Articles toujours présents dans commande_interne_article.");
                    }
                }
            }

            // Check commande_interne_local
            sql = "SELECT COUNT(*) FROM commande_interne_local WHERE commande_interne_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, commandeId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        System.out.println("✅ Locaux supprimés de la table commande_interne_local.");
                    } else {
                        System.out.println("❌ Locaux toujours présents dans commande_interne_local.");
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la vérification de la suppression", e);
        }
    }
}