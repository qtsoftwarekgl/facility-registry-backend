package com.frpr.repo;

import com.frpr.model.Provinces;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProvincesRepository extends MongoRepository<Provinces, String> {

    List<Provinces> findAllByStatus(String s);
    Provinces findOneByName(String s);
}