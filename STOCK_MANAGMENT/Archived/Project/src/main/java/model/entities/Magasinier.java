package model.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Magasinier {
    private Long id;
    private String nom;
    private String nomUtilisateur; // Added username field
    private String hashedPassword;
    private boolean actif; // Account status (active/inactive)
    private LocalDateTime dernierConnexion; // Last login timestamp
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<CommandeInterne> commandesInternes = new ArrayList<>();
    private List<CommandeExterne> commandesExternes = new ArrayList<>();

    // Constructors
    public Magasinier() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.actif = true;
    }

    public Magasinier(String nom, String nomUtilisateur, String hashedPassword) {
        this();
        this.nom = nom;
        this.nomUtilisateur = nomUtilisateur;
        this.hashedPassword = hashedPassword;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.actif = true;
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

    public String getNomUtilisateur() {
        return nomUtilisateur;
    }

    public void setNomUtilisateur(String nomUtilisateur) {
        this.nomUtilisateur = nomUtilisateur;
        this.updatedAt = LocalDateTime.now();
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getDernierConnexion() {
        return dernierConnexion;
    }

    public void setDernierConnexion(LocalDateTime dernierConnexion) {
        this.dernierConnexion = dernierConnexion;
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
        this.updatedAt = LocalDateTime.now();
    }

    public List<CommandeExterne> getCommandesExternes() {
        return commandesExternes;
    }

    public void setCommandesExternes(List<CommandeExterne> commandesExternes) {
        this.commandesExternes = commandesExternes;
        this.updatedAt = LocalDateTime.now();
    }

    // Business Methods

    /**
     * Enregistre une connexion de l'utilisateur
     */
    public void enregistrerConnexion() {
        this.dernierConnexion = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Crée une nouvelle commande interne
     * @param consommateur Le consommateur qui fait la demande
     * @param local Le local d'où proviennent les articles
     * @return La nouvelle commande interne
     */
    public CommandeInterne creerCommandeInterne(Consommateur consommateur, Local local) {
        CommandeInterne commande = new CommandeInterne(null, "En attente", this, consommateur, local);
        commandesInternes.add(commande);
        return commande;
    }

    /**
     * Crée une nouvelle commande externe
     * @param fournisseur Le fournisseur auprès duquel commander
     * @param local Le local dans lequel stocker les articles
     * @return La nouvelle commande externe
     */
    public CommandeExterne creerCommandeExterne(Fournisseur fournisseur, Local local) {
        CommandeExterne commande = new CommandeExterne(null, "En attente", this, fournisseur, local);
        commandesExternes.add(commande);
        return commande;
    }

    /**
     * Recherche les commandes internes par statut
     * @param statut Le statut à rechercher
     * @return Liste des commandes internes avec ce statut
     */
    public List<CommandeInterne> getCommandesInternesParStatut(String statut) {
        List<CommandeInterne> result = new ArrayList<>();
        for (CommandeInterne commande : commandesInternes) {
            if (commande.getStatut().equals(statut)) {
                result.add(commande);
            }
        }
        return result;
    }

    /**
     * Recherche les commandes externes par statut
     * @param statut Le statut à rechercher
     * @return Liste des commandes externes avec ce statut
     */
    public List<CommandeExterne> getCommandesExternesParStatut(String statut) {
        List<CommandeExterne> result = new ArrayList<>();
        for (CommandeExterne commande : commandesExternes) {
            if (commande.getStatut().equals(statut)) {
                result.add(commande);
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Magasinier magasinier = (Magasinier) o;
        return Objects.equals(id, magasinier.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Magasinier{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", nomUtilisateur='" + nomUtilisateur + '\'' +
                ", actif=" + actif +
                ", dernierConnexion=" + dernierConnexion +
                '}';
    }
}