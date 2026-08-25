package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service;

import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.TravelerProfile;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.knn.KnnRecommendationResult;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.preprocessing.TravelerFeatureRecord;

import java.util.List;
import java.util.Map;

public interface KnnService {

    KnnRecommendationResult findRecommendations(TravelerFeatureRecord newTraveler, int k);

    KnnRecommendationResult findRecommendations(TravelerProfile newTraveler, int k);

    Map<String, Double> scoreDestinations(TravelerFeatureRecord newTraveler, int k);

    void loadHistoricalData(List<TravelerFeatureRecord> records);

    int getIndexedTravelerCount();
}
