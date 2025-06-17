package test;

import model.dao.CommandeExterneDAO;
import model.dao.CommandeExterneDAOImpl;
import model.entities.CommandeExterne;
import model.entities.Magasinier;
import model.entities.Fournisseur;
import model.entities.Local;
import model.entities.CommandeExterneArticle;
import model.entities.CommandeExterneLocal;
import model.entities.Article;
import utils.DatabaseConnection;
import java.sql.*;
import java.time.LocalDateTime;

public class TestCommandeExterneDAO {
    public static void main(String[] args) {
        // Initialize test data
        initializeTestData();

        CommandeExterneDAO dao = new CommandeExterneDAOImpl();

        // Debug: Print database URL
        try {
            System.out.println("Database URL: " + DatabaseConnection.getConnection().getMetaData().getURL());
        } catch (Exception e) {
            System.out.println("Erreur lors de la récupération de l'URL de la base de données: " + e.getMessage());
        }

        // Create test entities
        Magasinier magasinier = new Magasinier();
        magasinier.setId(1L);
        Fournisseur fournisseur = new Fournisseur();
        fournisseur.setId(1L);
        Local local = new Local();
        local.setId(1L);
        Article article1 = new Article();
        article1.setId(1L);
        Article article2 = new Article();
        article2.setId(2L);

        // Create CommandeExterne with related entities
        CommandeExterne commande = new CommandeExterne(null, "En attente", magasinier, fournisseur, local);
        CommandeExterneArticle article = new CommandeExterneArticle(commande, article1, 10);
        commande.getCommandeExterneArticles().add(article);
        CommandeExterneLocal local1 = new CommandeExterneLocal(commande, local);
        commande.getCommandeExterneLocals().add(local1);

        // Test insert
        dao.insert(commande);
        System.out.println("✅ Commande externe insérée : " + commande.getId());
        System.out.println("CreerA: " + commande.getCreerA());
        System.out.println("Articles: " + commande.getCommandeExterneArticles().size());
        System.out.println("Locals: " + commande.getCommandeExterneLocals().size());

        // Test getById
        CommandeExterne fetched = dao.getById(commande.getId());
        System.out.println("📦 Récupérée : " + fetched);
        System.out.println("Articles récupérés: " + fetched.getCommandeExterneArticles().size());
        System.out.println("Locals récupérés: " + fetched.getCommandeExterneLocals().size());

        // Test update
        fetched.setStatut("Validée");
        fetched.setConfirmerA(LocalDateTime.now());
        fetched.getCommandeExterneArticles().get(0).setQuantite(20);
        CommandeExterneArticle newArticle = new CommandeExterneArticle(fetched, article2, 5);
        fetched.getCommandeExterneArticles().add(newArticle);
        dao.update(fetched);
        System.out.println("✏️ Commande externe mise à jour.");
        System.out.println("MiseAjourA: " + fetched.getMiseAjourA());

        // Verify update
        fetched = dao.getById(fetched.getId());
        System.out.println("📦 Après mise à jour : " + fetched);
        System.out.println("Articles après mise à jour: " + fetched.getCommandeExterneArticles().size());
        System.out.println("Locals après mise à jour: " + fetched.getCommandeExterneLocals().size());

        // Test delete
        Long commandeId = fetched.getId();
        dao.delete(commandeId);
        System.out.println("🗑️ Commande externe supprimée.");

        // Verify deletion
        verifyDeletion(commandeId);
    }

    private static void initializeTestData() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute("INSERT OR IGNORE INTO magasinier (id, nom) VALUES (1, 'Test Magasinier')");
            stmt.execute("INSERT OR IGNORE INTO fournisseur (id, nom) VALUES (1, 'Test Fournisseur')");
            stmt.execute("INSERT OR IGNORE INTO local (id, nom, emplacement) VALUES (1, 'Test Local', 'Bâtiment A')");
            stmt.execute("INSERT OR IGNORE INTO article (id, nom) VALUES (1, 'Test Article 1')");
            stmt.execute("INSERT OR IGNORE INTO article (id, nom) VALUES (2, 'Test Article 2')");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'initialisation des données de test", e);
        }
    }

    private static void verifyDeletion(Long commandeId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check commande_externe
            String sql = "SELECT COUNT(*) FROM commande_externe WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, commandeId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        System.out.println("✅ Commande externe supprimée de la table commande_externe.");
                    } else {
                        System.out.println("❌ Commande externe toujours présente dans commande_externe.");
                    }
                }
            }

            // Check commande_externe_article
            sql = "SELECT COUNT(*) FROM commande_externe_article WHERE commande_externe_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, commandeId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        System.out.println("✅ Articles supprimés de la table commande_externe_article.");
                    } else {
                        System.out.println("❌ Articles toujours présents dans commande_externe_article.");
                    }
                }
            }

            // Check commande_externe_local
            sql = "SELECT COUNT(*) FROM commande_externe_local WHERE commande_externe_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, commandeId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        System.out.println("✅ Locaux supprimés de la table commande_externe_local.");
                    } else {
                        System.out.println("❌ Locaux toujours présents dans commande_externe_local.");
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la vérification de la suppression", e);
        }
    }
}