package model.entities;

import java.util.*;
import java.time.LocalDateTime;

public class Article {
    private Long id;
    private String reference;
    private String nom;
    private String categorie;
    private int stockMinimal;
    private LocalDateTime datePeremption;
    private boolean estCritique;
    private boolean estConsommable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<CommandeInterneArticle> commandeInterneArticles = new ArrayList<>();
    private List<CommandeExterneArticle> commandeExterneArticles = new ArrayList<>();
    private List<Stock> stocks = new ArrayList<>();

    // Constructors
    public Article() {}

    public Article(String reference, String nom, String categorie, int stockMinimal,
                   boolean estCritique, boolean estConsommable) {
        this.reference = reference;
        this.nom = nom;
        this.categorie = categorie;
        this.stockMinimal = stockMinimal;
        this.estCritique = estCritique;
        this.estConsommable = estConsommable;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Article(String reference, String nom, String categorie, int stockMinimal,
                   LocalDateTime datePeremption, boolean estCritique, boolean estConsommable) {
        this(reference, nom, categorie, stockMinimal, estCritique, estConsommable);
        this.datePeremption = datePeremption;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public int getStockMinimal() {
        return stockMinimal;
    }

    public void setStockMinimal(int stockMinimal) {
        this.stockMinimal = stockMinimal;
    }

    public LocalDateTime getDatePeremption() {
        return datePeremption;
    }

    public void setDatePeremption(LocalDateTime datePeremption) {
        this.datePeremption = datePeremption;
    }

    public boolean isEstCritique() {
        return estCritique;
    }

    public void setEstCritique(boolean estCritique) {
        this.estCritique = estCritique;
    }

    public boolean isEstConsommable() {
        return estConsommable;
    }

    public void setEstConsommable(boolean estConsommable) {
        this.estConsommable = estConsommable;
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

    public List<CommandeInterneArticle> getCommandeInterneArticles() {
        return commandeInterneArticles;
    }

    public void setCommandeInterneArticles(List<CommandeInterneArticle> commandeInterneArticles) {
        this.commandeInterneArticles = commandeInterneArticles;
    }

    public List<CommandeExterneArticle> getCommandeExterneArticles() {
        return commandeExterneArticles;
    }

    public void setCommandeExterneArticles(List<CommandeExterneArticle> commandeExterneArticles) {
        this.commandeExterneArticles = commandeExterneArticles;
    }

    public List<Stock> getStocks() {
        return stocks;
    }

    public void setStocks(List<Stock> stocks) {
        this.stocks = stocks;
    }

    // Business Methods

    /**
     * Récupère la quantité totale en stock pour cet article
     * @return La quantité totale
     */
    public int getQuantiteTotale() {
        int total = 0;
        for (Stock stock : stocks) {
            total += stock.getQuantite();
        }
        return total;
    }

    /**
     * Vérifie si l'article est en alerte de stock (stock inférieur au minimum)
     * @return true si en alerte, false sinon
     */
    public boolean estEnAlerteStock() {
        return getQuantiteTotale() < stockMinimal;
    }

    /**
     * Vérifie si l'article est en alerte de péremption
     * @param joursAvantAlerte Nombre de jours avant péremption pour déclencher l'alerte
     * @return true si en alerte de péremption, false sinon
     */
    public boolean estEnAlertePeremption(long joursAvantAlerte) {
        if (datePeremption == null) {
            return false;
        }

        LocalDateTime dateAlerte = LocalDateTime.now().plusDays(joursAvantAlerte);
        return datePeremption.isBefore(dateAlerte);
    }

    /**
     * Ajouter un stock à l'article dans un local spécifique
     * @param local Le local de stockage
     * @param quantite La quantité à ajouter
     * @return Le stock créé ou mis à jour
     */
    public Stock ajouterStock(Local local, int quantite) {
        // Chercher si un stock existe déjà pour ce local
        for (Stock stock : stocks) {
            if (stock.getLocal().getId().equals(local.getId())) {
                stock.setQuantite(stock.getQuantite() + quantite);
                stock.setUpdatedAt(LocalDateTime.now());
                return stock;
            }
        }

        // Sinon créer un nouveau stock
        Stock nouveauStock = new Stock(quantite, this, local);
        stocks.add(nouveauStock);
        return nouveauStock;
    }

    /**
     * Retirer une quantité du stock dans un local spécifique
     * @param local Le local de stockage
     * @param quantite La quantité à retirer
     * @return true si l'opération est réussie, false sinon
     */
    public boolean retirerStock(Local local, int quantite) {
        for (Stock stock : stocks) {
            if (stock.getLocal().getId().equals(local.getId())) {
                if (stock.getQuantite() >= quantite) {
                    stock.setQuantite(stock.getQuantite() - quantite);
                    stock.setUpdatedAt(LocalDateTime.now());
                    return true;
                }
                return false; // Quantité insuffisante
            }
        }
        return false; // Stock non trouvé pour ce local
    }

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", reference='" + reference + '\'' +
                ", nom='" + nom + '\'' +
                ", categorie='" + categorie + '\'' +
                ", stockMinimal=" + stockMinimal +
                ", estCritique=" + estCritique +
                ", estConsommable=" + estConsommable +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Article article = (Article) o;
        return Objects.equals(id, article.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}