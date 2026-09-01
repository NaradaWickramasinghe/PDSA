package com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectedResourceResponse {
    private String id;
    private String destination;
    private String name;
    private String description;
    private String category;
    private double cost;
    private double durationHours;
    private double weightKg;
    private double usefulness;
    private String transportType;
    private Integer capacity;
}
