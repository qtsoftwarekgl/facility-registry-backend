package com.frpr.pojo;

import com.frpr.model.StatusEnum;
import lombok.Data;

import java.util.Date;

@Data
public class ServicePojo {
    private String _id;
    private String name;
    private StatusEnum status;
}
