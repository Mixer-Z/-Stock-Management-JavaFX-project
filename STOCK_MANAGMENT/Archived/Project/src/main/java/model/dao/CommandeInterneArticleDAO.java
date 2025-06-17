package model.dao;

import model.entities.CommandeInterneArticle;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface CommandeInterneArticleDAO {
    void insert(CommandeInterneArticle commandeInterneArticle);
    void insert(CommandeInterneArticle commandeInterneArticle, Connection connection) throws SQLException;
    CommandeInterneArticle getById(Long id);
    List<CommandeInterneArticle> getByCommandeInterneId(Long commandeInterneId);
    List<CommandeInterneArticle> getByCommandeInterneId(Long commandeInterneId, Connection connection) throws SQLException;
    void update(CommandeInterneArticle commandeInterneArticle);
    void update(CommandeInterneArticle commandeInterneArticle, Connection connection) throws SQLException;
    void delete(Long id);
}