package com.frpr.repo;

import com.frpr.model.FacilityRegistry;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FacilityRepository extends MongoRepository<FacilityRegistry, String> {

    FacilityRegistry findBy_id(String fc);

    List<FacilityRegistry> findAllByNameLike(String a);

    FacilityRegistry findAllByCode(String code);

}