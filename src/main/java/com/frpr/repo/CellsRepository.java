package com.frpr.repo;

import com.frpr.model.Cell;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CellsRepository extends MongoRepository<Cell, String> {

    List<Cell> findAllBySectorIdAndStatus(String s, String s1);
    Cell findOneBySectorIdAndName(String sectorId, String name);
}