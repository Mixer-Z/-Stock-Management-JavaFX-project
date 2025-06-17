package model.dao;

import model.entities.CommandeInterne;
import java.util.List;

public interface CommandeInterneDAO {
    void insert(CommandeInterne commandeInterne);
    CommandeInterne getById(Long id);
    List<CommandeInterne> getAll();
    void update(CommandeInterne commandeInterne);
    void delete(Long id);
}