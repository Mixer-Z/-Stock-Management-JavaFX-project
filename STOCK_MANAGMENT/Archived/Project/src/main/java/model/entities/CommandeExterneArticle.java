package model.entities;

import java.time.LocalDateTime;
import java.util.Objects;

public class CommandeExterneArticle {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int quantite;
    private CommandeExterne commandeExterne;
    private Article article;

    // Constructors
    public CommandeExterneArticle() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public CommandeExterneArticle(CommandeExterne commandeExterne, Article article, int quantite) {
        this();
        this.commandeExterne = commandeExterne;
        this.article = article;
        this.quantite = quantite;
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
    }

    public CommandeExterne getCommandeExterne() {
        return commandeExterne;
    }

    public void setCommandeExterne(CommandeExterne commandeExterne) {
        this.commandeExterne = commandeExterne;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandeExterneArticle that = (CommandeExterneArticle) o;
        return Objects.equals(commandeExterne.getId(), that.commandeExterne.getId()) &&
                Objects.equals(article.getId(), that.article.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandeExterne.getId(), article.getId());
    }

    @Override
    public String toString() {
        return "CommandeExterneArticle{" +
                "commandeExterneId=" + (commandeExterne != null ? commandeExterne.getId() : "null") +
                ", articleId=" + (article != null ? article.getId() : "null") +
                ", quantite=" + quantite +
                '}';
    }
}
