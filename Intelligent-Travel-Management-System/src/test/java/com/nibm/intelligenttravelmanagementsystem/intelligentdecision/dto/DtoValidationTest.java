package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.dto;

import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.AgeGroup;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.TravelStyle;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid RecommendationRequest should pass validation successfully")
    void testValidRecommendationRequest() {
        RecommendationRequest request = RecommendationRequest.builder()
                .budget(new BigDecimal("800.00"))
                .durationDays(5)
                .groupSize(2)
                .ageGroup(AgeGroup.YOUNG_ADULT)
                .travelStyle(TravelStyle.ADVENTURE)
                .beachPreference(3)
                .adventurePreference(9)
                .naturePreference(8)
                .culturePreference(6)
                .nightlifePreference(4)
                .relaxationPreference(5)
                .build();

        Set<ConstraintViolation<RecommendationRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid request should produce zero violations");
    }

    @Test
    @DisplayName("Negative budget should fail validation (budget >= 0 required)")
    void testNegativeBudgetValidation() {
        RecommendationRequest request = RecommendationRequest.builder()
                .budget(new BigDecimal("-50.00"))
                .durationDays(5)
                .groupSize(2)
                .beachPreference(3)
                .adventurePreference(5)
                .naturePreference(5)
                .culturePreference(3)
                .nightlifePreference(3)
                .relaxationPreference(3)
                .build();

        Set<ConstraintViolation<RecommendationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("budget")));
    }

    @Test
    @DisplayName("Zero or negative durationDays should fail validation (durationDays > 0 required)")
    void testZeroDurationDaysValidation() {
        RecommendationRequest request = RecommendationRequest.builder()
                .budget(new BigDecimal("500.00"))
                .durationDays(0) // Invalid
                .groupSize(2)
                .beachPreference(3)
                .adventurePreference(5)
                .naturePreference(5)
                .culturePreference(3)
                .nightlifePreference(3)
                .relaxationPreference(3)
                .build();

        Set<ConstraintViolation<RecommendationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("durationDays")));
    }

    @Test
    @DisplayName("Zero or negative groupSize should fail validation (groupSize > 0 required)")
    void testZeroGroupSizeValidation() {
        RecommendationRequest request = RecommendationRequest.builder()
                .budget(new BigDecimal("500.00"))
                .durationDays(3)
                .groupSize(0) // Invalid
                .beachPreference(3)
                .adventurePreference(5)
                .naturePreference(5)
                .culturePreference(3)
                .nightlifePreference(3)
                .relaxationPreference(3)
                .build();

        Set<ConstraintViolation<RecommendationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("groupSize")));
    }

    @Test
    @DisplayName("Preference values outside 1..10 range should fail validation")
    void testOutOfRangePreferenceValidation() {
        RecommendationRequest request = RecommendationRequest.builder()
                .budget(new BigDecimal("500.00"))
                .durationDays(3)
                .groupSize(2)
                .beachPreference(0) // Invalid: < 1
                .adventurePreference(15) // Invalid: > 10
                .naturePreference(5)
                .culturePreference(5)
                .nightlifePreference(5)
                .relaxationPreference(5)
                .build();

        Set<ConstraintViolation<RecommendationRequest>> violations = validator.validate(request);
        assertEquals(2, violations.size());
    }

    @Test
    @DisplayName("DestinationRecommendation DTO matches required destination, score, rank, reason contract")
    void testDestinationRecommendationContract() {
        DestinationRecommendation recommendation = DestinationRecommendation.builder()
                .destination("Ella")
                .score(0.86)
                .rank(1)
                .reason("Strong match for adventure and nature preferences, within budget and suitable for the requested duration.")
                .build();

        assertEquals("Ella", recommendation.getDestination());
        assertEquals(0.86, recommendation.getScore());
        assertEquals(1, recommendation.getRank());
        assertEquals("Strong match for adventure and nature preferences, within budget and suitable for the requested duration.", recommendation.getReason());
    }
}
