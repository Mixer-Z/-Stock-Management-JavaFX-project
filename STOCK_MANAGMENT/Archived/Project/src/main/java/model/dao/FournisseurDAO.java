package model.dao;

import model.entities.Fournisseur;
import java.util.List;

public interface FournisseurDAO {
    void create(Fournisseur fournisseur) throws Exception;
    Fournisseur findById(Long id) throws Exception;
    List<Fournisseur> findAll() throws Exception;
    void update(Fournisseur fournisseur) throws Exception;
    void delete(Long id) throws Exception;
}