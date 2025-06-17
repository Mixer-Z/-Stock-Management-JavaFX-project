package test;

import model.dao.MagasinierDAO;
import model.dao.MagasinierDAOImpl;
import model.entities.Magasinier;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestMagasinierDAO {
    public static void main(String[] args) {
        MagasinierDAO dao = new MagasinierDAOImpl();

        // Generate a unique nom_utilisateur
        String uniqueUsername = "pmartin_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        // Create and insert magasinier
        Magasinier magasinier = new Magasinier("Paul Martin", uniqueUsername, "hashed_password_123");
        magasinier.setActif(true);
        try {
            System.out.println("Attempting to insert magasinier with nom_utilisateur: " + uniqueUsername);
            dao.insert(magasinier);
            System.out.println("✅ Magasinier inséré : ID = " + magasinier.getId());
        } catch (RuntimeException e) {
            System.err.println("❌ Erreur lors de l'insertion : " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Retrieve by ID
        try {
            Magasinier fetched = dao.getById(magasinier.getId());
            if (fetched != null) {
                System.out.println("📦 Récupéré par ID : " + fetched);
                System.out.println("   Nom: " + fetched.getNom());
                System.out.println("   Nom Utilisateur: " + fetched.getNomUtilisateur());
                System.out.println("   Actif: " + fetched.isActif());
                System.out.println("   Created At: " + fetched.getCreatedAt());
            } else {
                System.out.println("❌ Magasinier non trouvé pour ID = " + magasinier.getId());
            }
        } catch (RuntimeException e) {
            System.err.println("❌ Erreur lors de la récupération par ID : " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Retrieve by username
        try {
            Magasinier fetchedByUsername = dao.getByNomUtilisateur(uniqueUsername);
            if (fetchedByUsername != null) {
                System.out.println("📦 Récupéré par nom utilisateur : " + fetchedByUsername);
            } else {
                System.out.println("❌ Magasinier non trouvé pour nom utilisateur = " + uniqueUsername);
            }
        } catch (RuntimeException e) {
            System.err.println("❌ Erreur lors de la récupération par nom utilisateur : " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Update magasinier
        try {
            magasinier.setNom("Paul Martin Jr.");
            magasinier.setActif(false);
            magasinier.enregistrerConnexion(); // Simulate login
            dao.update(magasinier);
            System.out.println("✏️ Magasinier mis à jour.");
            Magasinier updated = dao.getById(magasinier.getId());
            System.out.println("📦 Après mise à jour : " + updated);
            System.out.println("   Dernier Connexion: " + updated.getDernierConnexion());
        } catch (RuntimeException e) {
            System.err.println("❌ Erreur lors de la mise à jour : " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Delete magasinier
        try {
            System.out.println(magasinier.getId());
            dao.delete(magasinier.getId());
            System.out.println("🗑️ Magasinier supprimé.");
            Magasinier deleted = dao.getById(magasinier.getId());
            System.out.println("📦 Après suppression : " + (deleted == null ? "Non trouvé (supprimé)" : deleted));
        } catch (RuntimeException e) {
            System.err.println("❌ Erreur lors de la suppression : " + e.getMessage());
            e.printStackTrace();
        }
    }
}