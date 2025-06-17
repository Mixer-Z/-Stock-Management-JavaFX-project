package test;

import model.dao.FournisseurDAO;
import model.dao.FournisseurDAOImpl;
import model.entities.Fournisseur;
import java.time.LocalDateTime;
import java.util.List;

public class TestFournisseurDAO {
    public static void main(String[] args) {
        FournisseurDAO dao = new FournisseurDAOImpl();

        try {
            // Test Create
            Fournisseur fournisseur = new Fournisseur(
                    "Test Supplier",
                    "123 Test Street",
                    "123-456-7890",
                    "test@supplier.com",
                    "http://testsupplier.com",
                    "John Doe"
            );
            dao.create(fournisseur);
            System.out.println("Created fournisseur: " + fournisseur);

            // Test FindById
            Fournisseur found = dao.findById(fournisseur.getId());
            System.out.println("Found fournisseur: " + found);

            // Test Update
            fournisseur.setNom("Updated Supplier");
            fournisseur.setAdresse("456 Updated Street");
            dao.update(fournisseur);
            found = dao.findById(fournisseur.getId());
            System.out.println("Updated fournisseur: " + found);

            // Test FindAll
            List<Fournisseur> allFournisseurs = dao.findAll();
            System.out.println("All fournisseurs: " + allFournisseurs);

            // Test Delete
            dao.delete(fournisseur.getId());
            found = dao.findById(fournisseur.getId());
            System.out.println("After delete, fournisseur exists: " + (found != null));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}