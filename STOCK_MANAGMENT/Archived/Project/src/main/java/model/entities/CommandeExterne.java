package model.entities;

import java.time.LocalDateTime;
import java.util.*;

public class CommandeExterne {
    private Long id;
    private LocalDateTime creerA;
    private LocalDateTime confirmerA;
    private LocalDateTime miseAjourA;
    private String statut; // "En attente", "Validée", "Annulée", "Livrée"
    private Magasinier magasinier;
    private Fournisseur fournisseur;
    private Local local;
    private List<CommandeExterneArticle> commandeExterneArticles = new ArrayList<>();
    private List<CommandeExterneLocal> commandeExterneLocals = new ArrayList<>();

    // Constructors
    public CommandeExterne() {
        this.creerA = LocalDateTime.now();
        this.miseAjourA = LocalDateTime.now();
        this.statut = "En attente";
    }

    public CommandeExterne(LocalDateTime confirmerA, String statut, Magasinier magasinier,
                           Fournisseur fournisseur, Local local) {
        this();
        this.confirmerA = confirmerA;
        this.statut = statut;
        this.magasinier = magasinier;
        this.fournisseur = fournisseur;
        this.local = local;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getConfirmerA() {
        return confirmerA;
    }

    public void setConfirmerA(LocalDateTime confirmerA) {
        this.confirmerA = confirmerA;
    }

    public LocalDateTime getCreerA() {
        return creerA;
    }

    public void setCreerA(LocalDateTime creerA) {
        this.creerA = creerA;
    }

    public LocalDateTime getMiseAjourA() {
        return miseAjourA;
    }

    public void setMiseAjourA(LocalDateTime miseAjourA) {
        this.miseAjourA = miseAjourA;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
        this.miseAjourA = LocalDateTime.now();
    }

    public Magasinier getMagasinier() {
        return magasinier;
    }

    public void setMagasinier(Magasinier magasinier) {
        this.magasinier = magasinier;
    }

    public Fournisseur getFournisseur() {
        return fournisseur;
    }

    public void setFournisseur(Fournisseur fournisseur) {
        this.fournisseur = fournisseur;
    }

    public Local getLocal() {
        return local;
    }

    public void setLocal(Local local) {
        this.local = local;
    }

    public List<CommandeExterneArticle> getCommandeExterneArticles() {
        return commandeExterneArticles;
    }

    public void setCommandeExterneArticles(List<CommandeExterneArticle> commandeExterneArticles) {
        this.commandeExterneArticles = commandeExterneArticles;
    }

    public List<CommandeExterneLocal> getCommandeExterneLocals() {
        return commandeExterneLocals;
    }

    public void setCommandeExterneLocals(List<CommandeExterneLocal> commandeExterneLocals) {
        this.commandeExterneLocals = commandeExterneLocals;
    }

    // Business Methods

    /**
     * Ajoute un article à la commande
     * @param article L'article à ajouter
     * @param quantite La quantité à commander
     */
    public void addArticle(Article article, int quantite) {
        // Vérifier si l'article existe déjà dans la commande
        for (CommandeExterneArticle cea : commandeExterneArticles) {
            if (cea.getArticle().getId().equals(article.getId())) {
                cea.setQuantite(cea.getQuantite() + quantite);
                cea.setUpdatedAt(LocalDateTime.now());
                return;
            }
        }

        // Sinon, ajouter un nouvel article
        CommandeExterneArticle item = new CommandeExterneArticle(this, article, quantite);
        commandeExterneArticles.add(item);
    }

    /**
     * Supprime un article de la commande
     * @param article L'article à supprimer
     * @return true si supprimé, false si non trouvé
     */
    public boolean removeArticle(Article article) {
        return commandeExterneArticles.removeIf(cea -> cea.getArticle().getId().equals(article.getId()));
    }

    /**
     * Modifie la quantité d'un article dans la commande
     * @param article L'article à modifier
     * @param nouvelleQuantite La nouvelle quantité
     * @return true si modifié, false si non trouvé
     */
    public boolean modifierQuantiteArticle(Article article, int nouvelleQuantite) {
        for (CommandeExterneArticle cea : commandeExterneArticles) {
            if (cea.getArticle().getId().equals(article.getId())) {
                cea.setQuantite(nouvelleQuantite);
                cea.setUpdatedAt(LocalDateTime.now());
                return true;
            }
        }
        return false;
    }

    /**
     * Valide la commande externe et met à jour les stocks
     * @return true si la validation a réussi, false sinon
     */
    public boolean validerCommande() {
        if (!"En attente".equals(statut)) {
            return false;
        }

        // Mettre à jour le statut
        this.statut = "Validée";
        this.confirmerA = LocalDateTime.now();
        this.miseAjourA = LocalDateTime.now();

        // Ajouter les articles au stock du local spécifié
        for (CommandeExterneArticle cea : commandeExterneArticles) {
            local.ajouterArticle(cea.getArticle(), cea.getQuantite());
        }

        return true;
    }

    /**
     * Annule la commande externe
     * @return true si l'annulation a réussi, false sinon
     */
    public boolean annulerCommande() {
        if (!"En attente".equals(statut)) {
            return false;
        }

        this.statut = "Annulée";
        this.miseAjourA = LocalDateTime.now();
        return true;
    }

    /**
     * Calcule le montant total de la commande
     * @return Le montant total
     */
    public double calculerMontantTotal() {
        // Cette méthode nécessiterait d'ajouter un attribut prix aux articles
        // Pour l'instant, retourne juste la somme des quantités
        return commandeExterneArticles.stream()
                .mapToDouble(CommandeExterneArticle::getQuantite)
                .sum();
    }

    @Override
    public String toString() {
        return "CommandeExterne{" +
                "id=" + id +
                ", dateCreation=" + (creerA != null ? creerA : "null") +
                ", dateConfirmation=" + (confirmerA != null ? confirmerA : "null") +
                ", statut=" + (statut != null ? statut : "null") +
                ", fournisseur=" + (fournisseur != null ? (fournisseur.getNom() != null ? fournisseur.getNom() : "nom non défini") : "null") +
                ", local=" + (local != null ? (local.getNom() != null ? local.getNom() : "nom non défini") : "null") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandeExterne that = (CommandeExterne) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}