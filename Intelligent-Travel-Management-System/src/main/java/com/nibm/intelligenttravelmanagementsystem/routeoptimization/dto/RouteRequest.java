package com.nibm.intelligenttravelmanagementsystem.routeoptimization.dto;

import com.nibm.intelligenttravelmanagementsystem.routeoptimization.model.TransportMode;
import lombok.Data;

import java.util.List;

@Data
public class RouteRequest {
    private String startLocationId;
    private String endLocationId;
    private List<String> multipleLocations; // For multi-stop
    private TransportMode transportMode = TransportMode.NORMAL_VEHICLE;
    private boolean prioritizeTime = false;

    // NEW: Safe route toggle
    private boolean preferSafeRoute = false;  // If true, avoids high-risk roads
    private Integer maxRiskLevel;
}