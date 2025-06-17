package test;

import model.dao.ConsommateurDAO;
import model.dao.ConsommateurDAOImpl;
import model.entities.Consommateur;

public class TestConsommateurDAO {
    public static void main(String[] args) {
        ConsommateurDAO dao = new ConsommateurDAOImpl();

        Consommateur consommateur = new Consommateur("Test Consumer", "test@consumer.com", "987-654-3210", "Department", "Test department description");

        dao.insert(consommateur);
        System.out.println("✅ Consommateur inséré : " + consommateur.getId());

        Consommateur fetched = dao.getById(consommateur.getId());
        System.out.println("📦 Récupéré : " + fetched);

        fetched.setNom("Updated Consumer");
        fetched.setEmail("updated@consumer.com");
        dao.update(fetched);
        System.out.println("✏️ Consommateur mis à jour.");

        dao.delete(fetched.getId());
        System.out.println("🗑️ Consommateur supprimé.");
    }
}