package com.frpr.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("meta_data")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetaData extends AuditModel {

    @Id
    private String _id;
    private String name;
    private String description;
    private StatusEnum status;
    private MetaDataTypeEnum type;
}
