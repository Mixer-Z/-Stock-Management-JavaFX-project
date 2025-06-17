package model.dao;

import model.entities.CommandeExterneLocal;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface CommandeExterneLocalDAO {
    void insert(CommandeExterneLocal commandeExterneLocal);
    void insert(CommandeExterneLocal commandeExterneLocal, Connection conn) throws SQLException;
    CommandeExterneLocal getById(Long id);
    List<CommandeExterneLocal> getByCommandeExterneId(Long commandeExterneId);
    List<CommandeExterneLocal> getByCommandeExterneId(Long commandeExterneId, Connection conn) throws SQLException;
    void update(CommandeExterneLocal commandeExterneLocal);
    void update(CommandeExterneLocal commandeExterneLocal, Connection conn) throws SQLException;
    void delete(Long id);
}