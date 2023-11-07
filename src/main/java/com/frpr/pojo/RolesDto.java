package com.frpr.pojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RolesDto {
    private String ministry;
    private String name;
    private String role;
    private String status;
    private String value;
}
