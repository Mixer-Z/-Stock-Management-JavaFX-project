package test;

import model.dao.LocalDAO;
import model.dao.LocalDAOImpl;
import model.entities.Local;
import model.entities.Consommateur;

public class TestLocalDAO {
    public static void main(String[] args) {
        LocalDAO dao = new LocalDAOImpl();

        // Debug: Print database URL
        try {
            System.out.println("Database URL: " + utils.DatabaseConnection.getConnection().getMetaData().getURL());
        } catch (Exception e) {
            System.out.println("Erreur lors de la récupération de l'URL de la base de données: " + e.getMessage());
        }

        // Create test entities
        Consommateur consommateur = new Consommateur();
        consommateur.setId(1L); // Assumes consommateur with ID 1 exists

        // Test insert
        Local local = new Local("Salle 101", "Bâtiment A", "salle", consommateur);
        dao.insert(local);
        System.out.println("✅ Local inséré : " + local.getId());
        System.out.println("CreatedAt: " + local.getCreatedAt());

        // Test getById
        Local fetched = dao.getById(local.getId());
        System.out.println("📦 Récupéré : " + fetched);

        // Test update
        fetched.setNom("Salle 102");
        fetched.setType("amphi");
        dao.update(fetched);
        System.out.println("✏️ Local mis à jour.");
        System.out.println("UpdatedAt: " + fetched.getUpdatedAt());

        // Test delete
        dao.delete(fetched.getId());
        System.out.println("🗑️ Local supprimé.");
    }
}