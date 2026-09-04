package com.nibm.intelligenttravelmanagementsystem.resourceallocation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceOption {
    private String id;
    private String destination;
    private String name;
    private String description;
    private ResourceCategory category;
    private double cost;
    private double durationHours;
    private double weightKg;
    private double usefulness;
    @Builder.Default
    private boolean available = true;
    private String transportType;
    private Integer capacity;
}
