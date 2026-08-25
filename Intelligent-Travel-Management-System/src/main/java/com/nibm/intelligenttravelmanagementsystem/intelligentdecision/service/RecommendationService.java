package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service;

import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.dto.RecommendationRequest;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.dto.RecommendationResponse;

public interface RecommendationService {

    RecommendationResponse getRecommendations(RecommendationRequest request);
}
