package com.nibm.intelligenttravelmanagementsystem.routeoptimization.dto;

import com.nibm.intelligenttravelmanagementsystem.routeoptimization.model.TransportMode;
import lombok.Data;

import java.util.List;

@Data
public class RouteRequest {
    private Long startLocationId;
    private Long endLocationId;
    private List<Long> multipleLocations; // For multi-stop
    private TransportMode transportMode = TransportMode.NORMAL_VEHICLE;
    private boolean prioritizeTime = false;
}