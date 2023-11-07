package com.frpr.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document("audit_log")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditLog {

    @Id
    private String _id;
    private String userName;
    private String entity;
    private String path;
    private Action action;
    private String httpMethod;
    private String payload;
    private String oldData;
    private String newData;
    private String facilityCode;
    private Date createdAt;
    private String ip;

    public AuditLog(String userName, String entity, Action action, String payload, String oldData, String ip, String httpMethod, String path, String facilityCode, String newData) {
        this.userName = userName;
        this.action = action;
        this.entity = entity;
        this.payload = payload;
        this.oldData = oldData;
        this.createdAt = new Date();
        this.ip = ip;
        this.httpMethod = httpMethod;
        this.path = path;
        this.facilityCode = facilityCode;
        this.newData = newData;
    }

    public AuditLog(String userName, String entity, Action action, String payload, String oldData, String ip, String httpMethod, String path, String newData) {
        this.userName = userName;
        this.action = action;
        this.entity = entity;
        this.payload = payload;
        this.oldData = oldData;
        this.createdAt = new Date();
        this.ip = ip;
        this.httpMethod = httpMethod;
        this.path = path;
        this.newData = newData;
    }

}