package com.frpr.pojo;

import com.frpr.model.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PackagePojo {
    private String _id;
    private String name;
    private String categoryId;
    private String categoryName;
    private List<ServicePojo> services;
    private StatusEnum status;
}
