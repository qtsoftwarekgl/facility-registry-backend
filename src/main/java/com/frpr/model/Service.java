package com.frpr.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document("service")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Service {

    @Id
    private String _id;
    private String name;
    private String createdBy;
    private Date dateCreated;
    private Date dateChanged;
    private String changedBy;
    private StatusEnum status;
}
