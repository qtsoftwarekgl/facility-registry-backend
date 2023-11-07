package com.frpr.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("villages")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Villages {
    @Id
    private String _id;
    public String code;
    public String name;
    public String cellId;
    public String status;
}
