package com.nibm.intelligenttravelmanagementsystem.routeoptimization.dto;

import com.nibm.intelligenttravelmanagementsystem.routeoptimization.model.TransportMode;
import lombok.Data;

import java.util.List;

@Data
public class MultiStopRequest {
    private String startLocationId;  // NEW: Starting point
    private List<String> destinationIds;  // Renamed for clarity
    private TransportMode transportMode = TransportMode.NORMAL_VEHICLE;
    private boolean prioritizeTime = false;

    private boolean preferSafeRoute = false;
    private Integer maxRiskLevel;
}