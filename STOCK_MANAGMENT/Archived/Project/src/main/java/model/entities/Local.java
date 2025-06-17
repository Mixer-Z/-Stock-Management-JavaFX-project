package model.entities;

import java.util.*;
import java.time.LocalDateTime;

public class Local {
    private Long id;
    private String nom;
    private String emplacement;
    private String type; // Type de local (bibliothèque, amphi, salle, bureau, magasin)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<CommandeInterneLocal> commandeInterneLocals = new ArrayList<>();
    private List<CommandeExterneLocal> commandeExterneLocals = new ArrayList<>();
    private List<Stock> stocks = new ArrayList<>();
    private Consommateur consommateur;

    // Constructors
    public Local() {}

    public Local(String nom, String emplacement) {
        this.nom = nom;
        this.emplacement = emplacement;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Local(String nom, String emplacement, String type) {
        this(nom, emplacement);
        this.type = type;
    }

    public Local(String nom, String emplacement, String type, Consommateur consommateur) {
        this(nom, emplacement, type);
        this.consommateur = consommateur;
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
    }

    public String getEmplacement() {
        return emplacement;
    }

    public void setEmplacement(String emplacement) {
        this.emplacement = emplacement;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Consommateur getConsommateur() {
        return consommateur;
    }

    public void setConsommateur(Consommateur consommateur) {
        this.consommateur = consommateur;
    }

    public List<Stock> getStocks() {
        return stocks;
    }

    public void setStocks(List<Stock> stocks) {
        this.stocks = stocks;
    }

    public List<CommandeInterneLocal> getCommandeInterneLocals() {
        return commandeInterneLocals;
    }

    public void setCommandeInterneLocals(List<CommandeInterneLocal> commandeInterneLocals) {
        this.commandeInterneLocals = commandeInterneLocals;
    }

    public List<CommandeExterneLocal> getCommandeExterneLocals() {
        return commandeExterneLocals;
    }

    public void setCommandeExterneLocals(List<CommandeExterneLocal> commandeExterneLocals) {
        this.commandeExterneLocals = commandeExterneLocals;
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
     * Vérifie si un article se trouve dans ce local
     * @param article L'article à vérifier
     * @return true si l'article est stocké dans ce local, false sinon
     */
    public boolean contientArticle(Article article) {
        for (Stock stock : stocks) {
            if (stock.getArticle().getId().equals(article.getId()) && stock.getQuantite() > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Obtient la quantité d'un article dans ce local
     * @param article L'article recherché
     * @return La quantité disponible, 0 si non trouvé
     */
    public int getQuantiteArticle(Article article) {
        for (Stock stock : stocks) {
            if (stock.getArticle().getId().equals(article.getId())) {
                return stock.getQuantite();
            }
        }
        return 0;
    }

    /**
     * Ajoute une quantité d'un article dans ce local
     * @param article L'article à ajouter
     * @param quantite La quantité à ajouter
     * @return Le stock mis à jour
     */
    public Stock ajouterArticle(Article article, int quantite) {
        // Chercher si l'article existe déjà dans ce local
        for (Stock stock : stocks) {
            if (stock.getArticle().getId().equals(article.getId())) {
                stock.setQuantite(stock.getQuantite() + quantite);
                stock.setUpdatedAt(LocalDateTime.now());
                return stock;
            }
        }

        // Sinon créer un nouveau stock
        Stock nouveauStock = new Stock(quantite, article, this);
        stocks.add(nouveauStock);
        return nouveauStock;
    }

    /**
     * Retire une quantité d'un article de ce local
     * @param article L'article à retirer
     * @param quantite La quantité à retirer
     * @return true si l'opération est réussie, false sinon
     */
    public boolean retirerArticle(Article article, int quantite) {
        for (Stock stock : stocks) {
            if (stock.getArticle().getId().equals(article.getId())) {
                if (stock.getQuantite() >= quantite) {
                    stock.setQuantite(stock.getQuantite() - quantite);
                    stock.setUpdatedAt(LocalDateTime.now());
                    return true;
                }
                return false; // Quantité insuffisante
            }
        }
        return false; // Article non trouvé dans ce local
    }

    /**
     * Obtient la liste des articles stockés dans ce local
     * @return Liste des articles stockés
     */
    public List<Article> getArticlesStockes() {
        List<Article> articles = new ArrayList<>();
        for (Stock stock : stocks) {
            if (stock.getQuantite() > 0) {
                articles.add(stock.getArticle());
            }
        }
        return articles;
    }

    @Override
    public String toString() {
        return "Local{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", emplacement='" + emplacement + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Local local = (Local) o;
        return Objects.equals(id, local.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}