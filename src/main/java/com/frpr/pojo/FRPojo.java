package com.frpr.pojo;

import com.frpr.model.*;
import lombok.Data;

import java.util.List;

import java.util.Date;

@Data
public class FRPojo {
    private Provinces province;
    private Districts district;
    private Sectors sector;
    private Cell cell;
    private Villages village;
    private String _id;
    private String name;
    private String code;
    private String status;
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
    private List<ServicePojo> additionalServices;
    private List<PackagePojo> packages;
}
