package model.dao;

import model.entities.Local;
import java.util.List;

public interface LocalDAO {
    void insert(Local local);
    Local getById(Long id);
    List<Local> getAll();
    void update(Local local);
    void delete(Long id);
}