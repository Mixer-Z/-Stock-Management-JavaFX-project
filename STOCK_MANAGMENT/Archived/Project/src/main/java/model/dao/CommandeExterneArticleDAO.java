package model.dao;

import model.entities.CommandeExterneArticle;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface CommandeExterneArticleDAO {
    void insert(CommandeExterneArticle commandeExterneArticle);
    void insert(CommandeExterneArticle commandeExterneArticle, Connection conn) throws SQLException;
    CommandeExterneArticle getById(Long id);
    List<CommandeExterneArticle> getByCommandeExterneId(Long commandeExterneId);
    List<CommandeExterneArticle> getByCommandeExterneId(Long commandeExterneId, Connection conn) throws SQLException;
    void update(CommandeExterneArticle commandeExterneArticle);
    void update(CommandeExterneArticle commandeExterneArticle, Connection conn) throws SQLException;
    void delete(Long id);
}