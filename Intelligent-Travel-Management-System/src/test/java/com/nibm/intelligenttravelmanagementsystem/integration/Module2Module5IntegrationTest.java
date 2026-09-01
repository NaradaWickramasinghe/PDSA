package com.nibm.intelligenttravelmanagementsystem.integration;

import com.nibm.intelligenttravelmanagementsystem.overalloptimization.dto.OverallTravelPlanRequest;
import com.nibm.intelligenttravelmanagementsystem.overalloptimization.dto.OverallTravelPlanResponse;
import com.nibm.intelligenttravelmanagementsystem.overalloptimization.service.MasterTravelOptimizerService;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.ResourceAllocationRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class Module2Module5IntegrationTest {

    @Autowired
    private MasterTravelOptimizerService masterTravelOptimizerService;

    @Test
    @DisplayName("Module 5 -> Module 2 Integration Test - Overall Optimization delegates to Resource Allocation Service without circular dependency")
    void testMasterTravelOptimizerIntegrationWithModule2() {
        ResourceAllocationRequest resourceAllocationRequest = ResourceAllocationRequest.builder()
                .destination("Ella")
                .totalBudget(50000.0)
                .emergencyReserve(5000.0)
                .availableHours(16.0)
                .luggageCapacity(15.0)
                .travellerCount(2)
                .selectedAlgorithm("DYNAMIC_PROGRAMMING")
                .build();

        OverallTravelPlanRequest overallRequest = OverallTravelPlanRequest.builder()
                .planName("Sri Lanka Ella Explorer Plan")
                .resourceAllocationRequest(resourceAllocationRequest)
                .build();

        OverallTravelPlanResponse response = masterTravelOptimizerService.optimizeTravelPlan(overallRequest);

        assertNotNull(response);
        assertNotNull(response.getAllocatedResources());
        assertTrue(response.getAllocatedResources().isFeasible());
    }
}
