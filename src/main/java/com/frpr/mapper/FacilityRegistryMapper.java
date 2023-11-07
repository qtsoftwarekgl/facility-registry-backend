package com.frpr.mapper;

import com.frpr.model.FacilityRegistry;
import com.frpr.model.Package;
import com.frpr.model.Service;
import com.frpr.pojo.FRPojo;
import com.frpr.pojo.PackagePojo;
import com.frpr.pojo.ServicePojo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FacilityRegistryMapper {

    @Mapping(target = "province", ignore = true)
    @Mapping(target = "district", ignore = true)
    @Mapping(target = "sector", ignore = true)
    @Mapping(target = "cell", ignore = true)
    @Mapping(target = "village", ignore = true)
    FRPojo toFRPojo(FacilityRegistry facilityRegistry);

    PackagePojo toPackagePojo(Package aPackage);

    List<PackagePojo> toPackagePojoList(List<Package> aPackages);

    ServicePojo toServicePojo(Service aPackage);

    List<ServicePojo> toServicePojoList(List<Service> aPackage);
}
