package com.frpr.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("cells")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cell {
    @Id
    private String _id;

    public String name;
    public String code;
    public String sectorId;
    public String status;
}