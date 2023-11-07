package com.frpr.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

import java.util.Date;

@Document("facility_registry")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FacilityRegistry {

    @Id
    private String _id;
    private String name;

    private String code;
    private String status;

    private String province;
    private String district;
    private String sector;
    private String cell;
    private String village;
    private String locationCode;
    private String category;
    private String type;
    private String longitude;
    private String latitude;
    private Date facilityOpeningDate;
    private Date facilityClosingDate;
    private String pobox;
    private String phonenumber;
    private String email;
    private String streetAddress;
    private String deactivateReason;

    @DBRef
    private List<Service> additionalServices;

    @DBRef
    private List<Package> packages;
}
