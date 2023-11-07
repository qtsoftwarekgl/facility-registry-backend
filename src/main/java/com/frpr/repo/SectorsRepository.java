package com.frpr.repo;

import com.frpr.model.Sectors;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SectorsRepository extends MongoRepository<Sectors, String> {

    List<Sectors> findAllByDistrictIdAndStatus(String s, String s1);
    Sectors findOneByDistrictIdAndName(String districtId, String name);
}