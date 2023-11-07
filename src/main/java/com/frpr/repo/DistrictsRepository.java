package com.frpr.repo;

import com.frpr.model.Cell;
import com.frpr.model.Districts;
import com.frpr.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DistrictsRepository extends MongoRepository<Districts, String> {

    List<Districts> findAllByProvinceIdAndStatus(String s, String s1);
    Districts findOneByProvinceIdAndName(String province, String name);
}