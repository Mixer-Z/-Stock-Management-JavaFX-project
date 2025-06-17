package model.dao;

import model.entities.Magasinier;
import java.util.List;

public interface MagasinierDAO {
    void insert(Magasinier magasinier);
    Magasinier getById(Long id);
    Magasinier getByNomUtilisateur(String nomUtilisateur);
    List<Magasinier> getAll();
    void update(Magasinier magasinier);
    void delete(Long id);
}