package com.nibm.intelligenttravelmanagementsystem.resourceallocation;

import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.*;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ResourceAllocationDtosTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("1. Should pass validation for valid ResourceAllocationRequest")
    void testValidRequestValidation() {
        ResourceAllocationRequest request = ResourceAllocationRequest.builder()
                .totalBudget(500.0)
                .emergencyReserve(100.0)
                .availableHours(12.0)
                .luggageCapacity(15.0)
                .travellerCount(2)
                .selectedAlgorithm("GREEDY")
                .build();

        Set<ConstraintViolation<ResourceAllocationRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
        assertTrue(request.isEmergencyReserveValid());
    }

    @Test
    @DisplayName("2. Should fail validation when constraints are negative or null")
    void testInvalidRequestValidation() {
        ResourceAllocationRequest request = ResourceAllocationRequest.builder()
                .totalBudget(-50.0) // Negative budget
                .emergencyReserve(100.0)
                .availableHours(-2.0) // Negative hours
                .luggageCapacity(-1.0) // Negative capacity
                .travellerCount(0) // Invalid count
                .build();

        Set<ConstraintViolation<ResourceAllocationRequest>> violations = validator.validate(request);
        assertEquals(4, violations.size());
    }

    @Test
    @DisplayName("3. Should detect emergency reserve exceeding total budget")
    void testEmergencyReserveExceedsBudget() {
        ResourceAllocationRequest request = ResourceAllocationRequest.builder()
                .totalBudget(100.0)
                .emergencyReserve(150.0) // Exceeds total budget
                .availableHours(10.0)
                .luggageCapacity(5.0)
                .build();

        assertFalse(request.isEmergencyReserveValid());
    }

    @Test
    @DisplayName("4. Should correctly map AllocationResult to ResourceAllocationResponse")
    void testMapperToResponse() {
        ResourceOption item = ResourceOption.builder()
                .id("RES-01")
                .name("Camera Kit")
                .category(ResourceCategory.PHYSICAL_ITEM)
                .cost(50.0)
                .durationHours(0.0)
                .weightKg(2.0)
                .usefulness(85.0)
                .build();

        AllocationResult result = AllocationResult.builder()
                .algorithmName("GREEDY")
                .feasible(true)
                .selectedResources(List.of(item))
                .totalCost(50.0)
                .remainingBudget(150.0)
                .totalTime(0.0)
                .remainingTime(10.0)
                .totalWeight(2.0)
                .remainingCapacity(8.0)
                .overallScore(85.0)
                .executionTimeMs(5)
                .statusMessage("Success")
                .build();

        ResourceAllocationResponse response = ResourceAllocationMapper.toResponse(result);

        assertNotNull(response);
        assertEquals("GREEDY", response.getAlgorithmUsed());
        assertTrue(response.isFeasible());
        assertEquals(1, response.getSelectedResources().size());

        SelectedResourceResponse itemDto = response.getSelectedResources().get(0);
        assertEquals("RES-01", itemDto.getId());
        assertEquals("PHYSICAL_ITEM", itemDto.getCategory());
        assertEquals(50.0, itemDto.getCost());
    }
}
