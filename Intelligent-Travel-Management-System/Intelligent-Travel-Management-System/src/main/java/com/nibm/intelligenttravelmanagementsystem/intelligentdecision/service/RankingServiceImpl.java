package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service;

import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.Destination;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.SuitabilityLabel;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.knn.KnnRecommendationResult;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.tree.DecisionTreePrediction;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.preprocessing.DataPreprocessor;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.preprocessing.TravelerFeatureRecord;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ranking.RankedDestinationCandidate;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ranking.RankingWeights;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final RankingWeights defaultWeights;
    private final DataPreprocessor preprocessor;

    @Override
    public List<RankedDestinationCandidate> rankDestinations(
            TravelerFeatureRecord traveler,
            List<Destination> candidateDestinations,
            DecisionTreePrediction treePrediction,
            KnnRecommendationResult knnResult,
            int topN
    ) {
        return rankDestinations(traveler, candidateDestinations, treePrediction, knnResult, topN, defaultWeights);
    }

    @Override
    public List<RankedDestinationCandidate> rankDestinations(
            TravelerFeatureRecord rawTraveler,
            List<Destination> candidateDestinations,
            DecisionTreePrediction treePrediction,
            KnnRecommendationResult knnResult,
            int topN,
            RankingWeights weights
    ) {
        if (candidateDestinations == null || candidateDestinations.isEmpty()) {
            return Collections.emptyList();
        }

        TravelerFeatureRecord traveler = preprocessor.cleanAndImpute(rawTraveler);
        RankingWeights normalizedWeights = (weights != null ? weights : defaultWeights).getNormalized();

        Map<String, Double> knnScores = (knnResult != null && knnResult.getDestinationScores() != null)
                ? knnResult.getDestinationScores()
                : Collections.emptyMap();

        Map<String, Double> treeProbabilities = (treePrediction != null && treePrediction.getClassProbabilities() != null)
                ? treePrediction.getClassProbabilities()
                : Collections.emptyMap();

        String treePredictedClass = treePrediction != null ? treePrediction.getPredictedClass() : "";
        double treeBaseScore = (treePrediction != null && treePrediction.getSuitabilityLabel() != null)
                ? treePrediction.getSuitabilityLabel().getBaseScore()
                : 0.70;

        List<RankedDestinationCandidate> candidates = new ArrayList<>();

        for (Destination dest : candidateDestinations) {
            String destName = dest.getName();

            // 1. Decision Tree Contribution [0.0, 1.0]
            double treeContrib = 0.0;
            if (destName.equalsIgnoreCase(treePredictedClass)) {
                treeContrib = Math.max(0.85, treeBaseScore);
            } else if (treeProbabilities.containsKey(destName)) {
                treeContrib = treeProbabilities.get(destName);
            } else {
                treeContrib = 0.20; // Baseline for non-selected branches
            }

            // 2. KNN Contribution [0.0, 1.0]
            double knnContrib = knnScores.getOrDefault(destName, 0.40);

            // 3. Preference Compatibility [0.0, 1.0]
            double prefContrib = calculatePreferenceCompatibility(traveler, dest);

            // 4. Budget Compatibility [0.0, 1.0]
            double budgetContrib = calculateBudgetCompatibility(
                    traveler.getBudget(),
                    traveler.getDurationDays(),
                    dest.getAverageDailyCost().doubleValue()
            );

            // 5. Duration Compatibility [0.0, 1.0]
            double durationContrib = calculateDurationCompatibility(
                    traveler.getDurationDays(),
                    dest.getMinimumDays(),
                    dest.getMaximumDays()
            );

            // 6. Normalized Weighted Combination
            double rawFinalScore = (normalizedWeights.getTree() * treeContrib)
                    + (normalizedWeights.getKnn() * knnContrib)
                    + (normalizedWeights.getPreference() * prefContrib)
                    + (normalizedWeights.getBudget() * budgetContrib)
                    + (normalizedWeights.getDuration() * durationContrib);

            double normalizedFinalScore = Math.min(1.0, Math.max(0.0, rawFinalScore));

            // Suitability Classification
            SuitabilityLabel suitabilityLabel = determineSuitability(normalizedFinalScore, budgetContrib, durationContrib);

            // Factual Explainability Reasoning
            String explanation = buildFactualReason(
                    dest, traveler, treeContrib, knnContrib, prefContrib, budgetContrib, durationContrib
            );

            candidates.add(RankedDestinationCandidate.builder()
                    .destinationId(dest.getId())
                    .destinationName(destName)
                    .finalScore(round3(normalizedFinalScore))
                    .treeScore(round3(treeContrib))
                    .knnScore(round3(knnContrib))
                    .preferenceScore(round3(prefContrib))
                    .budgetScore(round3(budgetContrib))
                    .durationScore(round3(durationContrib))
                    .suitabilityLabel(suitabilityLabel)
                    .explanation(explanation)
                    .destination(dest)
                    .build());
        }

        // Deterministic Ranking with Tie-Breaking:
        // 1. Primary: finalScore descending
        // 2. Secondary: preferenceScore descending
        // 3. Tertiary: destinationName alphabetical ascending
        candidates.sort(Comparator
                .comparingDouble(RankedDestinationCandidate::getFinalScore).reversed()
                .thenComparing(Comparator.comparingDouble(RankedDestinationCandidate::getPreferenceScore).reversed())
                .thenComparing(RankedDestinationCandidate::getDestinationName));

        // Assign sequential 1-based ranks
        for (int i = 0; i < candidates.size(); i++) {
            candidates.get(i).setRank(i + 1);
        }

        int limit = topN > 0 ? Math.min(topN, candidates.size()) : candidates.size();
        return candidates.stream().limit(limit).collect(Collectors.toList());
    }

    private double calculatePreferenceCompatibility(TravelerFeatureRecord traveler, Destination dest) {
        double[] tVec = {
                traveler.getBeachPreference() / 5.0,
                traveler.getAdventurePreference() / 5.0,
                traveler.getNaturePreference() / 5.0,
                traveler.getCulturePreference() / 5.0,
                traveler.getNightlifePreference() / 5.0,
                traveler.getRelaxationPreference() / 5.0
        };

        double[] dVec = {
                dest.getBeachScore() / 10.0,
                dest.getAdventureScore() / 10.0,
                dest.getNatureScore() / 10.0,
                dest.getCultureScore() / 10.0,
                dest.getNightlifeScore() / 10.0,
                dest.getRelaxationScore() / 10.0
        };

        double dotProduct = 0.0;
        double normT = 0.0;
        double normD = 0.0;

        for (int i = 0; i < 6; i++) {
            dotProduct += tVec[i] * dVec[i];
            normT += tVec[i] * tVec[i];
            normD += dVec[i] * dVec[i];
        }

        if (normT <= 0.0 || normD <= 0.0) return 0.50;
        double cosSim = dotProduct / (Math.sqrt(normT) * Math.sqrt(normD));
        return Math.min(1.0, Math.max(0.0, cosSim));
    }

    private double calculateBudgetCompatibility(double travelerBudget, int durationDays, double averageDailyCost) {
        double estimatedTotalCost = averageDailyCost * durationDays;
        if (travelerBudget <= 0.0) return 0.50;

        if (estimatedTotalCost <= travelerBudget) {
            double margin = (travelerBudget - estimatedTotalCost) / travelerBudget;
            return 0.75 + (0.25 * Math.min(1.0, margin));
        } else {
            double deficit = (estimatedTotalCost - travelerBudget) / travelerBudget;
            return Math.max(0.0, 0.60 - (0.60 * deficit));
        }
    }

    private double calculateDurationCompatibility(int durationDays, int minDays, int maxDays) {
        if (durationDays >= minDays && durationDays <= maxDays) {
            return 1.00;
        }
        if (durationDays < minDays) {
            int gap = minDays - durationDays;
            return Math.max(0.20, 1.00 - (gap * 0.25));
        } else {
            int gap = durationDays - maxDays;
            return Math.max(0.30, 1.00 - (gap * 0.15));
        }
    }

    private SuitabilityLabel determineSuitability(double finalScore, double budgetScore, double durationScore) {
        if (budgetScore < 0.25 || durationScore < 0.25 || finalScore < 0.35) {
            return SuitabilityLabel.NOT_SUITABLE;
        }
        if (finalScore >= 0.80) {
            return SuitabilityLabel.EXCELLENT_FIT;
        }
        if (finalScore >= 0.60) {
            return SuitabilityLabel.MODERATE_FIT;
        }
        return SuitabilityLabel.CHALLENGING_FIT;
    }

    /**
     * Constructs factual, evidence-backed natural language explanations based strictly on calculated scoring factors.
     */
    private String buildFactualReason(
            Destination dest,
            TravelerFeatureRecord traveler,
            double treeScore,
            double knnScore,
            double prefScore,
            double budgetScore,
            double durationScore
    ) {
        List<String> clauses = new ArrayList<>();

        // 1. Preference alignment clause
        List<String> strongPrefs = new ArrayList<>();
        if (traveler.getAdventurePreference() >= 4 && dest.getAdventureScore() >= 7) strongPrefs.add("adventure");
        if (traveler.getNaturePreference() >= 4 && dest.getNatureScore() >= 7) strongPrefs.add("nature");
        if (traveler.getBeachPreference() >= 4 && dest.getBeachScore() >= 7) strongPrefs.add("beach");
        if (traveler.getCulturePreference() >= 4 && dest.getCultureScore() >= 7) strongPrefs.add("culture");
        if (traveler.getRelaxationPreference() >= 4 && dest.getRelaxationScore() >= 7) strongPrefs.add("relaxation");
        if (traveler.getNightlifePreference() >= 4 && dest.getNightlifeScore() >= 7) strongPrefs.add("nightlife");

        if (!strongPrefs.isEmpty()) {
            clauses.add("Strong match for " + String.join(" and ", strongPrefs) + " preferences");
        } else if (prefScore >= 0.70) {
            clauses.add("Good overall preference alignment");
        }

        // 2. Budget clause
        if (budgetScore >= 0.75) {
            clauses.add("within budget");
        } else if (budgetScore >= 0.40) {
            clauses.add("close to budget limit");
        } else {
            clauses.add("exceeds budget");
        }

        // 3. Duration clause
        if (durationScore >= 1.0) {
            clauses.add("suitable for the requested duration");
        } else if (durationScore >= 0.70) {
            clauses.add("moderately fits duration");
        } else {
            clauses.add("outside optimal duration window");
        }

        // 4. Collaborative/Tree signal clause (if prominent)
        if (knnScore >= 0.75) {
            clauses.add("similar travelers rated it highly");
        }

        if (clauses.isEmpty()) {
            return "Potential destination match based on overall profile.";
        }

        // Join clauses into a clean sentence
        String explanation = String.join(", ", clauses);
        explanation = Character.toUpperCase(explanation.charAt(0)) + explanation.substring(1) + ".";
        return explanation;
    }

    private double round3(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
