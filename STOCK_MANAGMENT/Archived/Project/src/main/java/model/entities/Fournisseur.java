package model.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Fournisseur {
    private Long id;
    private String nom;
    private String adresse;
    private String telephone;
    private String email; // Added email for contact
    private String siteWeb; // Added website
    private String personneContact; // Contact person
    private String notes; // General notes about the supplier
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<CommandeExterne> commandesExternes = new ArrayList<>();

    // Constructors
    public Fournisseur() {}

    public Fournisseur(String nom, String adresse, String telephone) {
        this.nom = nom;
        this.adresse = adresse;
        this.telephone = telephone;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Fournisseur(String nom, String adresse, String telephone, String email,
                       String siteWeb, String personneContact) {
        this.nom = nom;
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
        this.siteWeb = siteWeb;
        this.personneContact = personneContact;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
        this.updatedAt = LocalDateTime.now();
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
        this.updatedAt = LocalDateTime.now();
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
        this.updatedAt = LocalDateTime.now();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }

    public String getSiteWeb() {
        return siteWeb;
    }

    public void setSiteWeb(String siteWeb) {
        this.siteWeb = siteWeb;
        this.updatedAt = LocalDateTime.now();
    }

    public String getPersonneContact() {
        return personneContact;
    }

    public void setPersonneContact(String personneContact) {
        this.personneContact = personneContact;
        this.updatedAt = LocalDateTime.now();
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<CommandeExterne> getCommandesExternes() {
        return commandesExternes;
    }

    public void setCommandesExternes(List<CommandeExterne> commandesExternes) {
        this.commandesExternes = commandesExternes;
    }


    // Methods for order history analysis
    public int getTotalCommandeCount() {
        return commandesExternes.size();
    }

    public List<CommandeExterne> getCommandesByDateRange(LocalDateTime start, LocalDateTime end) {
        List<CommandeExterne> result = new ArrayList<>();
        for (CommandeExterne commande : commandesExternes) {
            LocalDateTime date = commande.getCreerA();
            if (date != null && !date.isBefore(start) && !date.isAfter(end)) {
                result.add(commande);
            }
        }
        return result;
    }

    public List<Article> getMostOrderedArticles(int limit) {
        // Logic to analyze and return most ordered articles from this supplier
        // This would typically query the database or analyze the commandesExternes
        return new ArrayList<>(); // Placeholder
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fournisseur that = (Fournisseur) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Fournisseur{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", adresse='" + adresse + '\'' +
                ", telephone='" + telephone + '\'' +
                ", email='" + email + '\'' +
                ", personneContact='" + personneContact + '\'' +
                '}';
    }
}

