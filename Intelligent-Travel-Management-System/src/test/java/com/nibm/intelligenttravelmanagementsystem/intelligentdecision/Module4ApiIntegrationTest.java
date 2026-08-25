package com.nibm.intelligenttravelmanagementsystem.intelligentdecision;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nibm.intelligenttravelmanagementsystem.common.exception.GlobalExceptionHandler;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.controller.DecisionController;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.dto.DestinationRecommendation;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.dto.RecommendationRequest;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.dto.RecommendationResponse;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.SuitabilityLabel;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.RecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DecisionController.class)
@Import(GlobalExceptionHandler.class)
class Module4ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        DestinationRecommendation ella = DestinationRecommendation.builder()
                .destination("Ella")
                .province("Uva")
                .rank(1)
                .score(0.92)
                .matchPercentage(92.0)
                .treeScore(0.90)
                .knnEvidenceScore(0.88)
                .preferenceScore(0.95)
                .budgetScore(0.90)
                .durationScore(1.0)
                .averageDailyCost(new BigDecimal("65.00"))
                .minimumDays(2)
                .maximumDays(5)
                .difficultyLevel(3)
                .suitabilityLabel(SuitabilityLabel.EXCELLENT_FIT)
                .reason("Strong match for adventure and nature preferences, within budget and suitable for the requested duration.")
                .destinationId(UUID.fromString("11111111-1111-1111-1111-111111110001"))
                .build();

        DestinationRecommendation mirissa = DestinationRecommendation.builder()
                .destination("Mirissa")
                .province("Southern")
                .rank(2)
                .score(0.81)
                .matchPercentage(81.0)
                .treeScore(0.60)
                .knnEvidenceScore(0.75)
                .preferenceScore(0.80)
                .budgetScore(0.85)
                .durationScore(1.0)
                .averageDailyCost(new BigDecimal("75.00"))
                .minimumDays(2)
                .maximumDays(6)
                .difficultyLevel(1)
                .suitabilityLabel(SuitabilityLabel.EXCELLENT_FIT)
                .reason("Strong match for beach preferences, within budget and suitable for the requested duration.")
                .destinationId(UUID.fromString("11111111-1111-1111-1111-111111110002"))
                .build();

        RecommendationResponse mockResponse = RecommendationResponse.builder()
                .travelerId(UUID.randomUUID())
                .totalCandidatesEvaluated(12)
                .recommendations(List.of(ella, mirissa))
                .decisionTreePrimaryPrediction("Ella")
                .summaryRationale("Top destination is Ella (Score: 92%) matching decision tree classification 'Ella'. Strong match for adventure and nature preferences, within budget and suitable for the requested duration.")
                .generatedAt(OffsetDateTime.now())
                .build();

        when(recommendationService.getRecommendations(any(RecommendationRequest.class)))
                .thenReturn(mockResponse);
    }

    @Test
    @DisplayName("API Test: Standard course recommendation payload produces valid HTTP 200, ranked structure, and factual reason")
    void testStandardRecommendationPayload() throws Exception {
        String jsonPayload = """
            {
              "budget": 800,
              "durationDays": 5,
              "groupSize": 2,
              "beachPreference": 3,
              "adventurePreference": 9,
              "naturePreference": 8,
              "culturePreference": 6,
              "nightlifePreference": 4,
              "relaxationPreference": 5
            }
            """;

        mockMvc.perform(post("/api/decisions/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Recommendations generated successfully"))
                .andExpect(jsonPath("$.data.totalCandidatesEvaluated").value(12))
                .andExpect(jsonPath("$.data.decisionTreePrimaryPrediction").value("Ella"))
                .andExpect(jsonPath("$.data.recommendations", hasSize(2)))
                // Verify Destination 1: Ella
                .andExpect(jsonPath("$.data.recommendations[0].rank").value(1))
                .andExpect(jsonPath("$.data.recommendations[0].destination").value("Ella"))
                .andExpect(jsonPath("$.data.recommendations[0].score").value(0.92))
                .andExpect(jsonPath("$.data.recommendations[0].matchPercentage").value(92.0))
                .andExpect(jsonPath("$.data.recommendations[0].reason").value(containsString("adventure and nature preferences")))
                .andExpect(jsonPath("$.data.recommendations[0].reason").value(containsString("within budget")))
                .andExpect(jsonPath("$.data.recommendations[0].suitabilityLabel").value("EXCELLENT_FIT"))
                // Verify Destination 2: Mirissa
                .andExpect(jsonPath("$.data.recommendations[1].rank").value(2))
                .andExpect(jsonPath("$.data.recommendations[1].destination").value("Mirissa"))
                .andExpect(jsonPath("$.data.recommendations[1].score").value(0.81));
    }

    @Test
    @DisplayName("API Test: Negative budget = -100 returns HTTP 400 Bad Request")
    void testInvalidNegativeBudget() throws Exception {
        String jsonPayload = """
            {
              "budget": -100,
              "durationDays": 5,
              "groupSize": 2,
              "beachPreference": 3,
              "adventurePreference": 9,
              "naturePreference": 8,
              "culturePreference": 6,
              "nightlifePreference": 4,
              "relaxationPreference": 5
            }
            """;

        mockMvc.perform(post("/api/decisions/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Budget must be greater than or equal to zero")));
    }

    @Test
    @DisplayName("API Test: Zero durationDays = 0 returns HTTP 400 Bad Request")
    void testInvalidZeroDuration() throws Exception {
        String jsonPayload = """
            {
              "budget": 800,
              "durationDays": 0,
              "groupSize": 2,
              "beachPreference": 3,
              "adventurePreference": 9,
              "naturePreference": 8,
              "culturePreference": 6,
              "nightlifePreference": 4,
              "relaxationPreference": 5
            }
            """;

        mockMvc.perform(post("/api/decisions/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Duration must be at least 1 day")));
    }

    @Test
    @DisplayName("API Test: Zero groupSize = 0 returns HTTP 400 Bad Request")
    void testInvalidZeroGroupSize() throws Exception {
        String jsonPayload = """
            {
              "budget": 800,
              "durationDays": 5,
              "groupSize": 0,
              "beachPreference": 3,
              "adventurePreference": 9,
              "naturePreference": 8,
              "culturePreference": 6,
              "nightlifePreference": 4,
              "relaxationPreference": 5
            }
            """;

        mockMvc.perform(post("/api/decisions/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Group size must be at least 1")));
    }

    @Test
    @DisplayName("API Test: Preference rating outside range (15) returns HTTP 400 Bad Request")
    void testInvalidPreferenceOutOfRange() throws Exception {
        String jsonPayload = """
            {
              "budget": 800,
              "durationDays": 5,
              "groupSize": 2,
              "beachPreference": 3,
              "adventurePreference": 15,
              "naturePreference": 8,
              "culturePreference": 6,
              "nightlifePreference": 4,
              "relaxationPreference": 5
            }
            """;

        mockMvc.perform(post("/api/decisions/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Adventure preference must be between 1 and 10")));
    }

    @Test
    @DisplayName("API Test: Missing required budget field returns HTTP 400 Bad Request")
    void testMissingRequiredBudgetField() throws Exception {
        String jsonPayload = """
            {
              "durationDays": 5,
              "groupSize": 2,
              "beachPreference": 3,
              "adventurePreference": 9,
              "naturePreference": 8,
              "culturePreference": 6,
              "nightlifePreference": 4,
              "relaxationPreference": 5
            }
            """;

        mockMvc.perform(post("/api/decisions/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Budget is required")));
    }
}
