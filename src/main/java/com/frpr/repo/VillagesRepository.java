package com.frpr.repo;

import com.frpr.model.Villages;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface VillagesRepository extends MongoRepository<Villages, String> {

    List<Villages> findAllByCellIdAndStatus(String s, String s1);
    Villages findOneByCellIdAndName(String cellId, String name);
}