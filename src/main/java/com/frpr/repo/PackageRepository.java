package com.frpr.repo;

import com.frpr.model.Package;
import com.frpr.model.Service;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PackageRepository extends MongoRepository<Package, String> {

    List<Package> findAllByName(String packageName);

    Package findAllByNameAndCategoryId(String cat, String name);

    List<Package> findAllByServicesIn(List<Service> services);

    List<Package> findAllByCategoryId(String categoryId);

}