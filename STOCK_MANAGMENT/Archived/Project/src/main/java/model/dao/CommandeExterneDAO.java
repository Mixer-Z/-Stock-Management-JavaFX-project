package model.dao;

import model.entities.CommandeExterne;
import java.util.List;

public interface CommandeExterneDAO {
    void insert(CommandeExterne commandeExterne);
    CommandeExterne getById(Long id);
    List<CommandeExterne> getAll();
    void update(CommandeExterne commandeExterne);
    void delete(Long id);
}