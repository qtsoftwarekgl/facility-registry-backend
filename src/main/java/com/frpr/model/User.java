package com.frpr.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document("user")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    private String _id;
    private String status;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String email;
    private String name;
    private String role;
    private String ministry;
    private String value;
    //"2022-01-11T13:40:19.602Z
    //@JsonFormat(pattern="yyyy-MM-ddTHH:mm:ss.SSSz")
    private Date createdAt;
    public String documentType;
    public String documentNumber;
    public String surName;
    public String postNames;
    public String dateOfBirth;
    public String dateOfExpiry;
    public String maritalStatus;
    public String citizenStatus;
    public String sex;
    public String nationality;
    public String domicileCountry;
    public String domicileDistrict;
    public String domicileProvince;
    public String domicileSector;
    public String domicileCell;
    public String domicileVillage;
    public String photo;
    public String phoneNumber;
    public String accessType;
    public String residentialCountry;
    public String issueNumber;
    public String dateOfIssue;
    public String placeOfIssue;
    public String applicationNumber;
    public String nin;
    public String nid;
    public String fatherName;
    public String motherName;
    public String birthCountry;
    public String countryOfBirth;
    public String villageId;
    public String civilStatus;
    public Object spouse;
    public String applicantType;
    private int wrongPasswordCount;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String otp;

    private Date lastPasswordResetDate;
    private Boolean isRequiredToResetPassword;
    private String deactivateReason;
}