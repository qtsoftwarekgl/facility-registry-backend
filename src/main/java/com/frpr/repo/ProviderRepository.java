package com.frpr.repo;

import com.frpr.model.ProviderRegistry;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProviderRepository extends MongoRepository<ProviderRegistry, String> {

}