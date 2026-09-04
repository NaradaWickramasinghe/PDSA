package com.nibm.intelligenttravelmanagementsystem.resourceallocation;

import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.ResourceAllocationRequest;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.ResourceAllocationResponse;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.service.*;
import com.nibm.intelligenttravelmanagementsystem.common.exception.GlobalExceptionHandler;
import com.nibm.intelligenttravelmanagementsystem.shared.exception.InvalidAllocationRequestException;
import com.nibm.intelligenttravelmanagementsystem.shared.exception.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResourceAllocationValidationTest {

    private ResourceAllocationService service;
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        GreedyAllocationService greedy = new GreedyAllocationService();
        DynamicProgrammingAllocationService dp = new DynamicProgrammingAllocationService();
        GeneticAllocationService genetic = new GeneticAllocationService();
        ResourceDataProvider dataProvider = new InMemorySampleDataProvider();

        service = new ResourceAllocationService(List.of(greedy, dp, genetic), dataProvider);
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("1. Negative Budget - Should throw InvalidAllocationRequestException")
    void testNegativeBudget() {
        ResourceAllocationRequest request = ResourceAllocationRequest.builder()
                .totalBudget(-50.0)
                .emergencyReserve(10.0)
                .availableHours(5.0)
                .luggageCapacity(5.0)
                .build();

        InvalidAllocationRequestException ex = assertThrows(
                InvalidAllocationRequestException.class,
                () -> service.allocateResources(request)
        );
        assertTrue(ex.getMessage().contains("Total budget must be greater than or equal to 0"));
    }

    @Test
    @DisplayName("2. Negative Emergency Reserve - Should throw InvalidAllocationRequestException")
    void testNegativeReserve() {
        ResourceAllocationRequest request = ResourceAllocationRequest.builder()
                .totalBudget(100.0)
                .emergencyReserve(-20.0)
                .availableHours(5.0)
                .luggageCapacity(5.0)
                .build();

        InvalidAllocationRequestException ex = assertThrows(
                InvalidAllocationRequestException.class,
                () -> service.allocateResources(request)
        );
        assertTrue(ex.getMessage().contains("Emergency reserve must be greater than or equal to 0"));
    }

    @Test
    @DisplayName("3. Emergency Reserve Exceeds Budget - Should throw InvalidAllocationRequestException")
    void testReserveExceedsBudget() {
        ResourceAllocationRequest request = ResourceAllocationRequest.builder()
                .totalBudget(100.0)
                .emergencyReserve(150.0)
                .availableHours(5.0)
                .luggageCapacity(5.0)
                .build();

        InvalidAllocationRequestException ex = assertThrows(
                InvalidAllocationRequestException.class,
                () -> service.allocateResources(request)
        );
        assertTrue(ex.getMessage().contains("Emergency reserve cannot exceed total budget"));
    }

    @Test
    @DisplayName("4. Zero/Negative Available Hours - Should throw InvalidAllocationRequestException")
    void testZeroAvailableHours() {
        ResourceAllocationRequest request = ResourceAllocationRequest.builder()
                .totalBudget(100.0)
                .emergencyReserve(20.0)
                .availableHours(0.0)
                .luggageCapacity(5.0)
                .build();

        InvalidAllocationRequestException ex = assertThrows(
                InvalidAllocationRequestException.class,
                () -> service.allocateResources(request)
        );
        assertTrue(ex.getMessage().contains("Available travel hours must be greater than 0"));
    }

    @Test
    @DisplayName("5. Negative Luggage Capacity - Should throw InvalidAllocationRequestException")
    void testNegativeCapacity() {
        ResourceAllocationRequest request = ResourceAllocationRequest.builder()
                .totalBudget(100.0)
                .emergencyReserve(20.0)
                .availableHours(5.0)
                .luggageCapacity(-3.0)
                .build();

        InvalidAllocationRequestException ex = assertThrows(
                InvalidAllocationRequestException.class,
                () -> service.allocateResources(request)
        );
        assertTrue(ex.getMessage().contains("Luggage capacity must be greater than or equal to 0"));
    }

    @Test
    @DisplayName("6. Invalid Algorithm Name - Should throw InvalidAllocationRequestException")
    void testInvalidAlgorithmName() {
        ResourceAllocationRequest request = ResourceAllocationRequest.builder()
                .totalBudget(100.0)
                .emergencyReserve(20.0)
                .availableHours(5.0)
                .luggageCapacity(5.0)
                .selectedAlgorithm("RANDOM_GUESS")
                .build();

        InvalidAllocationRequestException ex = assertThrows(
                InvalidAllocationRequestException.class,
                () -> service.allocateResources(request)
        );
        assertTrue(ex.getMessage().contains("Invalid algorithm name"));
    }

    @Test
    @DisplayName("7. Empty Resource Dataset - Should return infeasible response cleanly")
    void testEmptyDataset() {
        ResourceAllocationService emptyService = new ResourceAllocationService(
                List.of(new GreedyAllocationService()),
                () -> List.of() // Empty data provider
        );

        ResourceAllocationRequest request = ResourceAllocationRequest.builder()
                .totalBudget(100.0)
                .emergencyReserve(20.0)
                .availableHours(5.0)
                .luggageCapacity(5.0)
                .selectedAlgorithm("GREEDY")
                .build();

        ResourceAllocationResponse response = emptyService.allocateResources(request);
        assertNotNull(response);
        assertFalse(response.isFeasible());
        assertTrue(response.getStatusMessage().contains("empty"));
    }

    @Test
    @DisplayName("8. GlobalExceptionHandler - Should sanitize generic internal server exceptions without revealing stack trace")
    void testGlobalExceptionHandlerSanitization() {
        var response = exceptionHandler.handleGenericException(new RuntimeException("/secret/db/password"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("An unexpected error occurred while processing your request.", response.getBody().getMessage());
        assertFalse(response.getBody().getMessage().contains("/secret/db/password"));
    }
}
