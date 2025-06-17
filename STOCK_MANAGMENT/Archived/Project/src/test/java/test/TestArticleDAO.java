package test;

import model.dao.ArticleDAO;
import model.dao.ArticleDAOImpl;
import model.entities.Article;

public class TestArticleDAO {
    public static void main(String[] args) {
        ArticleDAO dao = new ArticleDAOImpl();

        Article article = new Article("R554dfEF", "Gants", "Équipement", 10, false, true);

        dao.insert(article);
        System.out.println("✅ Article inséré : " + article.getId());

        Article fetched = dao.getById(article.getId());
        System.out.println("📦 Récupéré : " + fetched);

        fetched.setNom("Gants Latex");
        dao.update(fetched);
        System.out.println("✏️ Article mis à jour.");

        dao.delete(fetched.getId());
        System.out.println("🗑️ Article supprimé.");
    }
}
