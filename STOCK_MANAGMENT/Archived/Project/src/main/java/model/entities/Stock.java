package model.entities;

import java.time.LocalDateTime;
import java.util.Objects;

public class Stock {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int quantite;
    private Article article;
    private Local local;

    // Constructors
    public Stock() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Stock(int quantite, Article article, Local local) {
        this();
        this.quantite = quantite;
        this.article = article;
        this.local = local;
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

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
        this.updatedAt = LocalDateTime.now();
    }

    public Local getLocal() {
        return local;
    }

    public void setLocal(Local local) {
        this.local = local;
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

    // Business Methods

    /**
     * Ajoute une quantité à ce stock
     * @param quantite La quantité à ajouter
     * @return La nouvelle quantité totale
     */
    public int ajouter(int quantite) {
        if (quantite < 0) {
            throw new IllegalArgumentException("La quantité à ajouter ne peut pas être négative");
        }
        this.quantite += quantite;
        this.updatedAt = LocalDateTime.now();
        return this.quantite;
    }

    /**
     * Retire une quantité de ce stock
     * @param quantite La quantité à retirer
     * @return La nouvelle quantité totale
     * @throws IllegalArgumentException Si la quantité est insuffisante
     */
    public int retirer(int quantite) {
        if (quantite < 0) {
            throw new IllegalArgumentException("La quantité à retirer ne peut pas être négative");
        }
        if (this.quantite < quantite) {
            throw new IllegalArgumentException("Stock insuffisant: " + this.quantite + " < " + quantite);
        }
        this.quantite -= quantite;
        this.updatedAt = LocalDateTime.now();
        return this.quantite;
    }

    /**
     * Vérifie si le stock est épuisé
     * @return true si la quantité est égale à 0, false sinon
     */
    public boolean estEpuise() {
        return this.quantite == 0;
    }

    /**
     * Vérifie si le stock est inférieur au seuil minimal de l'article
     * @return true si la quantité est inférieure au seuil minimal, false sinon
     */
    public boolean estEnDessousDuSeuil() {
        return this.article != null && this.quantite < this.article.getStockMinimal();
    }

    /**
     * Vérifie si l'article de ce stock est en alerte de péremption
     * @param joursAvantAlerte Nombre de jours avant péremption pour déclencher l'alerte
     * @return true si l'article est en alerte de péremption, false sinon
     */
    public boolean estEnAlertePeremption(long joursAvantAlerte) {
        return this.article != null && this.article.estEnAlertePeremption(joursAvantAlerte);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stock stock = (Stock) o;
        return Objects.equals(id, stock.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Stock{" +
                "id=" + id +
                ", quantite=" + quantite +
                ", article=" + (article != null ? article.getNom() : "null") +
                ", local=" + (local != null ? local.getNom() : "null") +
                '}';
    }
}