package com.frpr.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("sectors")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Sectors {
    @Id
    private String _id;
    public String code;
    public String name;
    public String districtId;
    public String status;
}


