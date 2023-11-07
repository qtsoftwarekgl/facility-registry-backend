package com.frpr.repo;

import com.frpr.model.MetaData;
import com.frpr.model.MetaDataTypeEnum;
import com.frpr.model.StatusEnum;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MetaDataRepository extends MongoRepository<MetaData, String> {

    List<MetaData> findAllByTypeInAndStatus(List<MetaDataTypeEnum> typeEnums, StatusEnum status);

    List<MetaData> findAllByNameAndType(String name, MetaDataTypeEnum type);

    MetaData findOneByName(String name);
}