package com.nibm.intelligenttravelmanagementsystem.resourceallocation;

import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.*;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.service.AllocationAlgorithm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResourceAllocationModelsTest {

    @Test
    @DisplayName("Should correctly calculate effective budget in AllocationProblem")
    void testEffectiveBudget() {
        AllocationProblem problem = AllocationProblem.builder()
                .totalBudget(500.0)
                .emergencyReserve(100.0)
                .maxAvailableHours(12.0)
                .maxCarryingCapacityKg(15.0)
                .build();

        assertEquals(400.0, problem.getEffectiveBudget());
    }

    @Test
    @DisplayName("Should create infeasible AllocationResult with correct defaults")
    void testInfeasibleResult() {
        AllocationResult result = AllocationResult.infeasible("GREEDY", "Constraints violated");

        assertFalse(result.isFeasible());
        assertEquals("GREEDY", result.getAlgorithmName());
        assertEquals("Constraints violated", result.getStatusMessage());
        assertTrue(result.getSelectedResources().isEmpty());
        assertEquals(0.0, result.getOverallScore());
    }

    @Test
    @DisplayName("Should build ResourceOption with Builder pattern")
    void testResourceOptionBuilder() {
        ResourceOption option = ResourceOption.builder()
                .id("RES-01")
                .name("First Aid Kit")
                .category(ResourceCategory.PHYSICAL_ITEM)
                .cost(25.0)
                .durationHours(0.0)
                .weightKg(0.8)
                .usefulness(95.0)
                .available(true)
                .build();

        assertEquals("RES-01", option.getId());
        assertEquals(ResourceCategory.PHYSICAL_ITEM, option.getCategory());
        assertTrue(option.isAvailable());
    }

    @Test
    @DisplayName("Should verify AllocationAlgorithm interface contract implementation")
    void testAlgorithmContract() {
        AllocationAlgorithm mockAlgorithm = new AllocationAlgorithm() {
            @Override
            public AllocationResult allocate(AllocationProblem problem) {
                return AllocationResult.builder()
                        .algorithmName(getAlgorithmName())
                        .feasible(true)
                        .selectedResources(problem.getCandidateOptions())
                        .build();
            }

            @Override
            public String getAlgorithmName() {
                return "MOCK_ALGORITHM";
            }
        };

        AllocationProblem problem = AllocationProblem.builder()
                .candidateOptions(List.of(ResourceOption.builder().id("1").name("Test Item").build()))
                .build();

        AllocationResult result = mockAlgorithm.allocate(problem);

        assertEquals("MOCK_ALGORITHM", result.getAlgorithmName());
        assertTrue(result.isFeasible());
        assertEquals(1, result.getSelectedResources().size());
    }
}
