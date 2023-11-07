package com.frpr.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document("package")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Package {
    @Id
    private String _id;
    private String name;
    private String categoryId;
    @DBRef
    private List<Service> services;
    private String createdBy;
    private Date dateCreated;
    private Date dateChanged;
    private String changedBy;
    private StatusEnum status;
}
