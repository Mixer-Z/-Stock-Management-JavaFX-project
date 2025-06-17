package model.dao;

import model.entities.Consommateur;
import java.util.List;

public interface ConsommateurDAO {
    void insert(Consommateur consommateur);
    Consommateur getById(Long id);
    List<Consommateur> getAll();
    void update(Consommateur consommateur);
    void delete(Long id);
}