package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service;

import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.TravelerProfile;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.knn.DestinationRatingRecord;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.knn.HistoricalTravelerIndexItem;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.knn.KnnRecommendationResult;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.knn.NeighborMatch;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.preprocessing.DataPreprocessor;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.preprocessing.TravelerFeatureRecord;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnnServiceImpl implements KnnService {

    private final DataPreprocessor preprocessor;
    private final List<HistoricalTravelerIndexItem> indexedTravelers = new CopyOnWriteArrayList<>();

    private static final double EPSILON = 0.01;
    private static final double DEFAULT_NEUTRAL_SCORE = 0.50;

    @PostConstruct
    public void init() {
        log.info("Initializing KnnService: Generating and indexing baseline historical traveler dataset...");
        List<TravelerFeatureRecord> seedRecords = preprocessor.generateSyntheticDataset(42L);
        loadHistoricalData(seedRecords);
    }

    @Override
    public synchronized void loadHistoricalData(List<TravelerFeatureRecord> records) {
        indexedTravelers.clear();
        if (records == null || records.isEmpty()) {
            log.warn("KnnService initialized with empty historical data.");
            return;
        }

        for (TravelerFeatureRecord r : records) {
            double[] vec = preprocessor.extractKnnFeatures(r);
            UUID travelerId = r.getId() != null ? r.getId() : UUID.randomUUID();

            List<DestinationRatingRecord> ratings = new ArrayList<>();
            if (r.getTargetDestination() != null) {
                ratings.add(DestinationRatingRecord.builder()
                        .destinationName(r.getTargetDestination())
                        .rating(5) // Preferred destination ground-truth rating
                        .build());
            }

            indexedTravelers.add(HistoricalTravelerIndexItem.builder()
                    .travelerId(travelerId)
                    .featureVector(vec)
                    .visitedDestinations(ratings)
                    .build());
        }
        log.info("KnnService indexed {} historical traveler feature vectors.", indexedTravelers.size());
    }

    @Override
    public KnnRecommendationResult findRecommendations(TravelerFeatureRecord newTraveler, int k) {
        if (k <= 0) k = 5;

        // Cold-start fallback if index has fewer neighbors than k
        if (indexedTravelers.isEmpty()) {
            log.warn("KnnService encountered cold-start (0 indexed travelers). Returning neutral baseline.");
            return buildColdStartResult(k);
        }

        double[] targetVector = preprocessor.extractKnnFeatures(newTraveler);

        // 1. Calculate Euclidean distances to all indexed historical travelers
        List<NeighborMatch> neighborMatches = new ArrayList<>(indexedTravelers.size());
        for (HistoricalTravelerIndexItem item : indexedTravelers) {
            double dist = calculateEuclideanDistance(targetVector, item.getFeatureVector());
            double weight = 1.0 / (dist + EPSILON);
            neighborMatches.add(NeighborMatch.builder()
                    .travelerId(item.getTravelerId())
                    .distance(dist)
                    .similarityWeight(weight)
                    .travelerItem(item)
                    .build());
        }

        // 2. Sort neighbors from closest to farthest distance and pick the top K
        Collections.sort(neighborMatches, Comparator.comparingDouble(NeighborMatch::getDistance));
        int effectiveK = Math.min(k, neighborMatches.size());
        List<NeighborMatch> kNearest = neighborMatches.subList(0, effectiveK);

        // 3. Aggregate destination preferences from K neighbors weighted by inverse distance
        Map<String, Double> weightedDestinationScores = new HashMap<>();
        Map<String, Integer> destinationVoteCounts = new HashMap<>();
        Map<String, Double> destinationTotalRatings = new HashMap<>();
        double totalNeighborWeight = 0.0;

        for (NeighborMatch neighbor : kNearest) {
            double w = neighbor.getSimilarityWeight();
            totalNeighborWeight += w;

            for (DestinationRatingRecord ratingRec : neighbor.getTravelerItem().getVisitedDestinations()) {
                String destName = ratingRec.getDestinationName();
                // Convert rating (1-5) into a 0.2–1.0 range
                double normalizedRating = (double) ratingRec.getRating() / 5.0;

                // Add this neighbor's weighted rating to the running total for this destination
                if (weightedDestinationScores.containsKey(destName)) {
                    weightedDestinationScores.put(destName, weightedDestinationScores.get(destName) + (w * normalizedRating));
                } else {
                    weightedDestinationScores.put(destName, w * normalizedRating);
                }

                // Count how many neighbors voted for this destination
                if (destinationVoteCounts.containsKey(destName)) {
                    destinationVoteCounts.put(destName, destinationVoteCounts.get(destName) + 1);
                } else {
                    destinationVoteCounts.put(destName, 1);
                }

                // Accumulate total raw ratings (used to compute average later)
                if (destinationTotalRatings.containsKey(destName)) {
                    destinationTotalRatings.put(destName, destinationTotalRatings.get(destName) + ratingRec.getRating());
                } else {
                    destinationTotalRatings.put(destName, (double) ratingRec.getRating());
                }
            }
        }

        // 4. Normalize final destination evidence scores into [0.0, 1.0]
        Map<String, Double> finalScores = new HashMap<>();
        List<KnnRecommendationResult.KnnDestinationEvidence> evidences = new ArrayList<>();

        for (Map.Entry<String, Double> entry : weightedDestinationScores.entrySet()) {
            String dest = entry.getKey();
            double rawWeighted = entry.getValue();
            double normalizedScore = totalNeighborWeight > 0 ? (rawWeighted / totalNeighborWeight) : DEFAULT_NEUTRAL_SCORE;
            // Bounded in [0.0, 1.0]
            normalizedScore = Math.min(1.0, Math.max(0.0, normalizedScore));

            int votes = destinationVoteCounts.getOrDefault(dest, 1);
            double avgRating = destinationTotalRatings.getOrDefault(dest, 5.0) / votes;

            finalScores.put(dest, normalizedScore);
            evidences.add(KnnRecommendationResult.KnnDestinationEvidence.builder()
                    .destinationName(dest)
                    .evidenceScore(normalizedScore)
                    .voteCount(votes)
                    .averageRating(avgRating)
                    .totalWeightedScore(rawWeighted)
                    .explanation(String.format("Recommended by %d similar traveler(s) (k=%d) with an average rating of %.1f/5",
                            votes, effectiveK, avgRating))
                    .build());
        }

        // Sort the evidences from highest to lowest score so the best destinations appear first
        Collections.sort(evidences, Comparator.comparingDouble(KnnRecommendationResult.KnnDestinationEvidence::getEvidenceScore).reversed());

        return KnnRecommendationResult.builder()
                .kUsed(effectiveK)
                .totalNeighborsSearched(indexedTravelers.size())
                .destinationScores(finalScores)
                .topEvidences(evidences)
                .nearestNeighbors(kNearest)
                .build();
    }

    @Override
    public KnnRecommendationResult findRecommendations(TravelerProfile newTraveler, int k) {
        if (newTraveler == null) {
            return findRecommendations(TravelerFeatureRecord.builder().build(), k);
        }
        TravelerFeatureRecord record = TravelerFeatureRecord.fromEntity(newTraveler, null);
        return findRecommendations(record, k);
    }

    @Override
    public Map<String, Double> scoreDestinations(TravelerFeatureRecord newTraveler, int k) {
        return findRecommendations(newTraveler, k).getDestinationScores();
    }

    private double calculateEuclideanDistance(double[] v1, double[] v2) {
        double sum = 0.0;
        for (int i = 0; i < v1.length; i++) {
            double diff = v1[i] - v2[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    private KnnRecommendationResult buildColdStartResult(int k) {
        return KnnRecommendationResult.builder()
                .kUsed(0)
                .totalNeighborsSearched(0)
                .destinationScores(Collections.emptyMap())
                .topEvidences(Collections.emptyList())
                .nearestNeighbors(Collections.emptyList())
                .build();
    }

    @Override
    public int getIndexedTravelerCount() {
        return indexedTravelers.size();
    }
}
