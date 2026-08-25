package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model;

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

class DomainEntityTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid TravelerProfile should pass validation")
    void testValidTravelerProfile() {
        TravelerProfile profile = TravelerProfile.builder()
                .ageGroup(AgeGroup.YOUNG_ADULT)
                .budget(new BigDecimal("800.00"))
                .durationDays(5)
                .groupSize(2)
                .travelStyle(TravelStyle.ADVENTURE)
                .beachPreference(3)
                .adventurePreference(5)
                .naturePreference(4)
                .culturePreference(3)
                .nightlifePreference(4)
                .relaxationPreference(3)
                .build();

        Set<ConstraintViolation<TravelerProfile>> violations = validator.validate(profile);
        assertTrue(violations.isEmpty(), "Should have no validation violations");
    }

    @Test
    @DisplayName("TravelerProfile with invalid preferences should fail validation")
    void testInvalidTravelerProfilePreferences() {
        TravelerProfile profile = TravelerProfile.builder()
                .ageGroup(AgeGroup.ADULT)
                .budget(new BigDecimal("500.00"))
                .durationDays(3)
                .groupSize(1)
                .beachPreference(0) // Invalid: < 1
                .adventurePreference(6) // Invalid: > 5
                .naturePreference(3)
                .culturePreference(3)
                .nightlifePreference(3)
                .relaxationPreference(3)
                .build();

        Set<ConstraintViolation<TravelerProfile>> violations = validator.validate(profile);
        assertEquals(2, violations.size(), "Should have 2 violations for out-of-bound preferences");
    }

    @Test
    @DisplayName("Valid Destination should pass validation")
    void testValidDestination() {
        Destination destination = Destination.builder()
                .name("Ella")
                .province("Uva")
                .averageDailyCost(new BigDecimal("65.00"))
                .minimumDays(2)
                .maximumDays(5)
                .beachScore(1)
                .adventureScore(9)
                .natureScore(10)
                .cultureScore(4)
                .nightlifeScore(6)
                .relaxationScore(7)
                .difficultyLevel(3)
                .familyFriendly(true)
                .coupleFriendly(true)
                .groupFriendly(true)
                .build();

        Set<ConstraintViolation<Destination>> violations = validator.validate(destination);
        assertTrue(violations.isEmpty(), "Valid destination should have no violations");
    }

    @Test
    @DisplayName("Destination with negative cost or invalid scores should fail validation")
    void testInvalidDestination() {
        Destination destination = Destination.builder()
                .name("") // Blank name
                .province("Uva")
                .averageDailyCost(new BigDecimal("-10.00")) // Negative cost
                .minimumDays(0) // Minimum days < 1
                .maximumDays(5)
                .beachScore(15) // > 10
                .adventureScore(9)
                .natureScore(10)
                .cultureScore(4)
                .nightlifeScore(6)
                .relaxationScore(7)
                .difficultyLevel(6) // > 5
                .build();

        Set<ConstraintViolation<Destination>> violations = validator.validate(destination);
        assertFalse(violations.isEmpty(), "Invalid destination should produce violations");
        assertTrue(violations.size() >= 4, "Should catch multiple constraints");
    }

    @Test
    @DisplayName("SuitabilityLabel base scores match specification")
    void testSuitabilityLabels() {
        assertEquals(1.0, SuitabilityLabel.EXCELLENT_FIT.getBaseScore());
        assertEquals(0.7, SuitabilityLabel.MODERATE_FIT.getBaseScore());
        assertEquals(0.4, SuitabilityLabel.CHALLENGING_FIT.getBaseScore());
        assertEquals(0.0, SuitabilityLabel.NOT_SUITABLE.getBaseScore());
    }
}
