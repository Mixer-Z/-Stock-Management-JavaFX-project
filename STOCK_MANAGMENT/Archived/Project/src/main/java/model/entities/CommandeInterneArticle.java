package model.entities;

import java.time.LocalDateTime;
import java.util.Objects;

public class CommandeInterneArticle {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private int quantite;
    private String etat; // État de l'article à la livraison (bon état, endommagé, etc.)
    private String notes; // Commentaires supplémentaires

    private CommandeInterne commandeInterne;
    private Article article;

    // Constructors
    public CommandeInterneArticle() {}

    public CommandeInterneArticle(CommandeInterne commandeInterne, Article article, int quantite) {
        this.commandeInterne = commandeInterne;
        this.article = article;
        this.quantite = quantite;
        this.etat = "Bon état";
        this.notes = "";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public CommandeInterneArticle(CommandeInterne commandeInterne, Article article, int quantite, String etat, String notes) {
        this.commandeInterne = commandeInterne;
        this.article = article;
        this.quantite = quantite;
        this.etat = etat;
        this.notes = notes;
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

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
        this.updatedAt = LocalDateTime.now();
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
        this.updatedAt = LocalDateTime.now();
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
        this.updatedAt = LocalDateTime.now();
    }

    public CommandeInterne getCommandeInterne() {
        return commandeInterne;
    }

    public void setCommandeInterne(CommandeInterne commandeInterne) {
        this.commandeInterne = commandeInterne;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
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

    // Calculate the total price if the article has a price attribute
    public double getTotal() {
        // This would require article to have a price attribute
        // return this.quantite * this.article.getPrix();
        return 0.0; // Placeholder for now
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandeInterneArticle that = (CommandeInterneArticle) o;
        return Objects.equals(commandeInterne.getId(), that.commandeInterne.getId()) &&
                Objects.equals(article.getId(), that.article.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandeInterne.getId(), article.getId());
    }

    @Override
    public String toString() {
        return "CommandeInterneArticle{" +
                "commandeInterneId=" + (commandeInterne != null ? commandeInterne.getId() : "null") +
                ", articleId=" + (article != null ? article.getId() : "null") +
                ", articleNom=" + (article != null ? article.getNom() : "null") +
                ", quantite=" + quantite +
                ", etat='" + etat + '\'' +
                '}';
    }
}