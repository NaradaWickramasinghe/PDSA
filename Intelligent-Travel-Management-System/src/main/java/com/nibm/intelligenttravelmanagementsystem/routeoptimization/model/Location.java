package com.nibm.intelligenttravelmanagementsystem.routeoptimization.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private String type;

    // Additional fields from shared Node (for display purposes)
    private String province;
    private String district;
    private String description;

    // Constructor for basic location
    public Location(String id, String name, double latitude, double longitude, String type) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
    }
}