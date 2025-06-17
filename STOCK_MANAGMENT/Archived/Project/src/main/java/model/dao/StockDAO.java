package model.dao;

import model.entities.Stock;
import java.util.List;

public interface StockDAO {
    void insert(Stock stock);
    Stock getById(Long id);
    List<Stock> getAll();
    void update(Stock stock);
    void delete(Long id);
}