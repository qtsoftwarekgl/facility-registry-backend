package com.frpr.pojo;

import lombok.Data;

@Data
public class ChangePasswordReq {

    private String currentPassword;
    private String email;
    private String newPassword;
    private String confirmPassword;
}
