package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service;

import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.Destination;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.knn.KnnRecommendationResult;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.tree.DecisionTreePrediction;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.preprocessing.TravelerFeatureRecord;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ranking.RankedDestinationCandidate;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ranking.RankingWeights;

import java.util.List;

public interface RankingService {

    List<RankedDestinationCandidate> rankDestinations(
            TravelerFeatureRecord traveler,
            List<Destination> candidateDestinations,
            DecisionTreePrediction treePrediction,
            KnnRecommendationResult knnResult,
            int topN,
            RankingWeights customWeights
    );

    List<RankedDestinationCandidate> rankDestinations(
            TravelerFeatureRecord traveler,
            List<Destination> candidateDestinations,
            DecisionTreePrediction treePrediction,
            KnnRecommendationResult knnResult,
            int topN
    );
}
