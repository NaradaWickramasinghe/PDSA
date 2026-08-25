package com.nibm.intelligenttravelmanagementsystem.routesequencing.service;

import com.nibm.intelligenttravelmanagementsystem.routesequencing.dto.ItineraryPlanRequest;
import com.nibm.intelligenttravelmanagementsystem.routesequencing.dto.ItineraryPlanResponse;

public interface RouteSequencingService {

    ItineraryPlanResponse createPersonalizedItinerary(ItineraryPlanRequest request);
}
