package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service;

import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.dto.DestinationRecommendation;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.dto.RecommendationRequest;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.dto.RecommendationResponse;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.DecisionLog;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.Destination;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.TravelerProfile;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.repository.DecisionLogRepository;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.repository.DestinationRepository;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.repository.TravelerProfileRepository;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.knn.KnnRecommendationResult;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.tree.DecisionTreePrediction;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.preprocessing.DataPreprocessor;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.preprocessing.TravelerFeatureRecord;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ranking.RankedDestinationCandidate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final DestinationRepository destinationRepository;
    private final TravelerProfileRepository travelerProfileRepository;
    private final DecisionLogRepository decisionLogRepository;

    private final DataPreprocessor preprocessor;
    private final DecisionTreeService decisionTreeService;
    private final KnnService knnService;
    private final RankingService rankingService;

    @Override
    @Transactional(readOnly = false)
    public RecommendationResponse getRecommendations(RecommendationRequest request) {
        log.info("Processing personalized recommendation request for travelerId: {}", request.getTravelerId());

        // 1. Data Preprocessing & Sanitization
        TravelerFeatureRecord featureRecord = convertRequestToRecord(request);
        TravelerFeatureRecord cleanedRecord = preprocessor.cleanAndImpute(featureRecord);

        // 2. Fetch Candidate Destinations (from request IDs or all available in DB)
        List<Destination> candidateDestinations;
        if (request.getCandidateDestinationIds() != null && !request.getCandidateDestinationIds().isEmpty()) {
            candidateDestinations = destinationRepository.findAllById(request.getCandidateDestinationIds());
        } else {
            candidateDestinations = destinationRepository.findAll();
        }

        if (candidateDestinations.isEmpty()) {
            log.warn("No candidate destinations found in repository. Returning empty response.");
            return buildEmptyResponse(request);
        }

        // 3. Step 1: Decision Tree Suitability Classification
        DecisionTreePrediction treePrediction = decisionTreeService.predict(cleanedRecord);

        // 4. Step 2: k-NN Historical Peer Collaborative Scoring (k=5)
        KnnRecommendationResult knnResult = knnService.findRecommendations(cleanedRecord, 5);

        // 5. Step 3: Multi-Criteria Weighted Ranking & Explainability Generation
        int topN = (request.getTopN() != null && request.getTopN() > 0) ? request.getTopN() : 5;
        List<RankedDestinationCandidate> rankedCandidates = rankingService.rankDestinations(
                cleanedRecord,
                candidateDestinations,
                treePrediction,
                knnResult,
                topN
        );

        // 6. Map to DestinationRecommendation DTOs
        List<DestinationRecommendation> recommendationDTOs = rankedCandidates.stream()
                .map(this::mapToDestinationRecommendation)
                .collect(Collectors.toList());

        // 7. Persist Audit Decision Logs
        persistDecisionLogs(request, rankedCandidates);

        // 8. Construct Final Response
        String summaryRationale = buildSummaryRationale(treePrediction, recommendationDTOs);

        return RecommendationResponse.builder()
                .travelerId(request.getTravelerId())
                .totalCandidatesEvaluated(candidateDestinations.size())
                .recommendations(recommendationDTOs)
                .decisionTreePrimaryPrediction(treePrediction.getPredictedClass())
                .summaryRationale(summaryRationale)
                .generatedAt(OffsetDateTime.now())
                .build();
    }

    private TravelerFeatureRecord convertRequestToRecord(RecommendationRequest req) {
        return TravelerFeatureRecord.builder()
                .id(req.getTravelerId())
                .budget(req.getBudget() != null ? req.getBudget().doubleValue() : 0.0)
                .durationDays(req.getDurationDays() != null ? req.getDurationDays() : 1)
                .groupSize(req.getGroupSize() != null ? req.getGroupSize() : 1)
                .ageGroup(req.getAgeGroup())
                .travelStyle(req.getTravelStyle())
                .beachPreference(req.getBeachPreference() != null ? req.getBeachPreference() : 3)
                .adventurePreference(req.getAdventurePreference() != null ? req.getAdventurePreference() : 3)
                .naturePreference(req.getNaturePreference() != null ? req.getNaturePreference() : 3)
                .culturePreference(req.getCulturePreference() != null ? req.getCulturePreference() : 3)
                .nightlifePreference(req.getNightlifePreference() != null ? req.getNightlifePreference() : 3)
                .relaxationPreference(req.getRelaxationPreference() != null ? req.getRelaxationPreference() : 3)
                .build();
    }

    private DestinationRecommendation mapToDestinationRecommendation(RankedDestinationCandidate c) {
        Destination d = c.getDestination();
        return DestinationRecommendation.builder()
                .destination(c.getDestinationName())
                .score(c.getFinalScore())
                .rank(c.getRank())
                .reason(c.getExplanation())
                .destinationId(c.getDestinationId())
                .province(d != null ? d.getProvince() : "Sri Lanka")
                .matchPercentage(Math.round(c.getFinalScore() * 1000.0) / 10.0)
                .treeScore(c.getTreeScore())
                .knnEvidenceScore(c.getKnnScore())
                .preferenceScore(c.getPreferenceScore())
                .budgetScore(c.getBudgetScore())
                .durationScore(c.getDurationScore())
                .averageDailyCost(d != null ? d.getAverageDailyCost() : null)
                .minimumDays(d != null ? d.getMinimumDays() : null)
                .maximumDays(d != null ? d.getMaximumDays() : null)
                .difficultyLevel(d != null ? d.getDifficultyLevel() : null)
                .suitabilityLabel(c.getSuitabilityLabel())
                .build();
    }

    private void persistDecisionLogs(RecommendationRequest req, List<RankedDestinationCandidate> ranked) {
        try {
            TravelerProfile travelerProfile = null;
            if (req.getTravelerId() != null) {
                travelerProfile = travelerProfileRepository.findById(req.getTravelerId()).orElse(null);
            }

            List<DecisionLog> logs = new ArrayList<>();
            for (RankedDestinationCandidate c : ranked) {
                if (c.getDestination() != null) {
                    logs.add(DecisionLog.builder()
                            .traveler(travelerProfile)
                            .destination(c.getDestination())
                            .treeScore((float) c.getTreeScore())
                            .knnScore((float) c.getKnnScore())
                            .finalScore((float) c.getFinalScore())
                            .rankPosition(c.getRank())
                            .explanation(c.getExplanation())
                            .build());
                }
            }
            if (!logs.isEmpty()) {
                decisionLogRepository.saveAll(logs);
            }
        } catch (Exception e) {
            log.warn("Could not persist decision audit log: {}", e.getMessage());
        }
    }

    private String buildSummaryRationale(DecisionTreePrediction treePrediction, List<DestinationRecommendation> recommendations) {
        if (recommendations.isEmpty()) {
            return "No matching destinations found for current constraints.";
        }
        DestinationRecommendation top = recommendations.get(0);
        return String.format("Top destination is %s (Score: %.0f%%) matching decision tree classification '%s'. %s",
                top.getDestination(),
                top.getMatchPercentage(),
                treePrediction.getPredictedClass(),
                top.getReason());
    }

    private RecommendationResponse buildEmptyResponse(RecommendationRequest request) {
        return RecommendationResponse.builder()
                .travelerId(request.getTravelerId())
                .totalCandidatesEvaluated(0)
                .recommendations(Collections.emptyList())
                .decisionTreePrimaryPrediction("NONE")
                .summaryRationale("No available candidate destinations to evaluate.")
                .generatedAt(OffsetDateTime.now())
                .build();
    }
}
