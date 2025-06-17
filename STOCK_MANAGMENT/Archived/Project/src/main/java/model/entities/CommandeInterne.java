package model.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommandeInterne {
    private Long id;

    private LocalDateTime confirmerA;
    private LocalDateTime creerA;
    private LocalDateTime miseAjourA;

    private String statut; // En attente, Confirmée, Annulée, etc.
    private Magasinier magasinier;
    private Consommateur consommateur;
    private Local local; // Adding local relationship as each command needs to specify where items come from

    private List<CommandeInterneArticle> commandeInterneArticles = new ArrayList<>();
    private List<CommandeInterneLocal> commandeInterneLocals = new ArrayList<>();

    // Constructors
    public CommandeInterne() {}

    public CommandeInterne(LocalDateTime date, String statut, Magasinier magasinier, Consommateur consommateur, Local local) {
        this.confirmerA = date;
        this.statut = statut;
        this.magasinier = magasinier;
        this.consommateur = consommateur;
        this.local = local;
        this.creerA = LocalDateTime.now();
        this.miseAjourA = LocalDateTime.now();
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

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Magasinier getMagasinier() {
        return magasinier;
    }

    public void setMagasinier(Magasinier magasinier) {
        this.magasinier = magasinier;
    }

    public Consommateur getConsommateur() {
        return consommateur;
    }

    public void setConsommateur(Consommateur consommateur) {
        this.consommateur = consommateur;
    }

    public Local getLocal() {
        return local;
    }

    public void setLocal(Local local) {
        this.local = local;
    }

    public List<CommandeInterneArticle> getCommandeInterneArticles() {
        return commandeInterneArticles;
    }

    public void setCommandeInterneArticles(List<CommandeInterneArticle> commandeInterneArticles) {
        this.commandeInterneArticles = commandeInterneArticles;
    }

    public List<CommandeInterneLocal> getCommandeInterneLocals() {
        return commandeInterneLocals;
    }

    public void setCommandeInterneLocals(List<CommandeInterneLocal> commandeInterneLocals) {
        this.commandeInterneLocals = commandeInterneLocals;
    }

    public void addArticle(Article article, int quantite) {
        // Check for existing article in the command
        for (CommandeInterneArticle item : commandeInterneArticles) {
            if (item.getArticle().getId().equals(article.getId())) {
                // Update quantity if article already exists
                item.setQuantite(item.getQuantite() + quantite);
                return;
            }
        }

        // Add new article if not found
        CommandeInterneArticle item = new CommandeInterneArticle(this, article, quantite);
        commandeInterneArticles.add(item);
    }

    public void removeArticle(Article article) {
        commandeInterneArticles.removeIf(item -> item.getArticle().getId().equals(article.getId()));
    }

    public void updateArticleQuantity(Article article, int newQuantite) {
        for (CommandeInterneArticle item : commandeInterneArticles) {
            if (item.getArticle().getId().equals(article.getId())) {
                item.setQuantite(newQuantite);
                return;
            }
        }
    }

    public int getArticleQuantity(Article article) {
        for (CommandeInterneArticle item : commandeInterneArticles) {
            if (item.getArticle().getId().equals(article.getId())) {
                return item.getQuantite();
            }
        }
        return 0;
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

    // Method to confirm the command and destock accordingly
    public void confirmerCommande() {
        if (!this.statut.equals("En attente")) {
            throw new IllegalStateException("La commande doit être en attente pour être confirmée");
        }

        // Update statut
        this.statut = "Confirmée";
        this.confirmerA = LocalDateTime.now();
        this.miseAjourA = LocalDateTime.now();

        // Destock each article from specified local
        for (CommandeInterneArticle item : commandeInterneArticles) {
            // Logic to destock would call a service/repository method
            // stockService.destock(item.getArticle(), local, item.getQuantite());
        }
    }

    // Method to cancel the command
    public void annulerCommande() {
        if (this.statut.equals("Confirmée")) {
            throw new IllegalStateException("La commande confirmée ne peut pas être annulée");
        }

        this.statut = "Annulée";
        this.miseAjourA = LocalDateTime.now();
    }

    // Calculate total number of items in the command
    public int getTotalItems() {
        int total = 0;
        for (CommandeInterneArticle item : commandeInterneArticles) {
            total += item.getQuantite();
        }
        return total;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandeInterne that = (CommandeInterne) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CommandeInterne{" +
                "id=" + id +
                ", confirmerA=" + confirmerA +
                ", statut=" + statut +
                ", magasinier=" + (magasinier != null ? magasinier.getNom() : "null") +
                ", consommateur=" + (consommateur != null ? consommateur.getNom() : "null") +
                ", local=" + (local != null ? local.getNom() : "null") +
                ", articles=" + commandeInterneArticles.size() +
                '}';
    }
}
