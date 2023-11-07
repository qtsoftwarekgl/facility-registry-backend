package com.frpr.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document("provider_registry")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProviderRegistry {

    @Id
    private String _id;

    private String documentType;
    private String documentNumber;
    private String identifier;// unique field
    private String surName;
    private String postNames;
    private String dateOfBirth;
    private String maritalStatus;
    private String sex;
    private String nationality;
    private String domicileCountry;
    private String domicileDistrict;
    private String domicileProvince;
    private String domicileSector;
    private String domicileCell;
    private String domicileVillage;
    private String licenseNumber; // unique field
    private String qualification;
    private String email;
    private String phoneNumber;
    private String photo;
    private String status;
    private List<String> facilityId;
    private Iterable<FacilityRegistry> facilities;
    // private String issueNumber;
    private String dateOfIssue;
    private String dateOfExpiry;
    private String placeOfIssue;
    private String applicationNumber;
    private String nin;// unique field
    private String nid;// unique field
    private String fatherName;
    private String motherName;
    private String birthCountry;
    private String countryOfBirth;
    private String villageId;
    private String civilStatus;
    private Object spouse;
    private String applicantType;

}
