package com.frpr.repo;

import com.frpr.model.Service;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ServiceRepository extends MongoRepository<Service, String> {

    Service findAllByName(String name);

}