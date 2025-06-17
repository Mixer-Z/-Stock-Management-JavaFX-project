package model.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Consommateur {
    private Long id;
    private String nom;
    private String email; // Added email for contact
    private String telephone; // Added phone number for contact
    private String type; // Type of consumer (department, service, etc.)
    private String description; // Brief description of the consumer
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<CommandeInterne> commandesInternes = new ArrayList<>();
    private List<Local> locaux = new ArrayList<>();

    // Constructors
    public Consommateur() {}

    public Consommateur(String nom) {
        this.nom = nom;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Consommateur(String nom, String email, String telephone, String type, String description) {
        this.nom = nom;
        this.email = email;
        this.telephone = telephone;
        this.type = type;
        this.description = description;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
        this.updatedAt = LocalDateTime.now();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public List<CommandeInterne> getCommandesInternes() {
        return commandesInternes;
    }

    public void setCommandesInternes(List<CommandeInterne> commandesInternes) {
        this.commandesInternes = commandesInternes;
    }

    public List<Local> getLocaux() {
        return locaux;
    }

    public void setLocaux(List<Local> locaux) {
        this.locaux = locaux;
    }

    // Methods for managing associated locations
    public void addLocal(Local local) {
        if (!locaux.contains(local)) {
            locaux.add(local);
            // Bidirectional relationship management
            if (local.getConsommateur() != this) {
                local.setConsommateur(this);
            }
        }
    }

    public void removeLocal(Local local) {
        if (locaux.contains(local)) {
            locaux.remove(local);
            // Bidirectional relationship management
            if (local.getConsommateur() == this) {
                local.setConsommateur(null);
            }
        }
    }

    // Methods for analyzing consumption
    public List<Article> getMostConsumedArticles(int limit) {
        // Logic to analyze and return most consumed articles
        // This would typically query the database or analyze the commandesInternes
        return new ArrayList<>(); // Placeholder
    }

    public int getTotalCommandeCount() {
        return commandesInternes.size();
    }

    public List<CommandeInterne> getCommandesByDateRange(LocalDateTime start, LocalDateTime end) {
        List<CommandeInterne> result = new ArrayList<>();
        for (CommandeInterne commande : commandesInternes) {
            LocalDateTime date = commande.getConfirmerA();
            if (date != null && !date.isBefore(start) && !date.isAfter(end)) {
                result.add(commande);
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Consommateur that = (Consommateur) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Consommateur{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", email='" + email + '\'' +
                ", telephone='" + telephone + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
