package model.dao;



import model.entities.Article;

import java.util.List;

public interface ArticleDAO {
    void insert(Article article);
    Article getById(Long id);
    List<Article> getAll();
    void update(Article article);
    void delete(Long id);
}
