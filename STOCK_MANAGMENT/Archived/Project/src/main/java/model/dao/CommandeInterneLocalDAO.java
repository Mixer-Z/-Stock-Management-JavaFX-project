package model.dao;

import model.entities.CommandeInterneLocal;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface CommandeInterneLocalDAO {
    void insert(CommandeInterneLocal local);
    void insert(CommandeInterneLocal local, Connection connection) throws SQLException;
    CommandeInterneLocal getById(Long id);
    List<CommandeInterneLocal> getByCommandeInterneId(Long commandeInterneId);
    List<CommandeInterneLocal> getByCommandeInterneId(Long commandeInterneId, Connection connection) throws SQLException;
    void update(CommandeInterneLocal local);
    void update(CommandeInterneLocal local, Connection connection) throws SQLException;
    void delete(Long id);
}