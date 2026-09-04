package com.nibm.intelligenttravelmanagementsystem.resourceallocation;

import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.ResourceAllocationRequest;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.ResourceAllocationResponse;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.service.ResourceAllocationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StructuredResponseTest {

    @Autowired
    private ResourceAllocationService service;

    @Test
    @DisplayName("Verify that ResourceAllocationResponse clearly populates all 9 Travel Resource Plan sections")
    void testStructuredTravelResourcePlanSections() {
        ResourceAllocationRequest request = ResourceAllocationRequest.builder()
                .destination("Ella")
                .totalBudget(50000.0)
                .emergencyReserve(5000.0)
                .availableHours(16.0)
                .luggageCapacity(15.0)
                .travellerCount(2)
                .selectedAlgorithm("DYNAMIC_PROGRAMMING")
                .build();

        ResourceAllocationResponse response = service.allocateResources(request);

        assertNotNull(response);
        assertTrue(response.isFeasible());

        // 1. Trip Information
        assertNotNull(response.getTripInformation());
        assertEquals("Ella", response.getTripInformation().getDestination());
        assertEquals(16.0, response.getTripInformation().getDurationHours());
        assertEquals(2, response.getTripInformation().getTravellerCount());

        // 2. Financial Summary
        assertNotNull(response.getFinancialSummary());
        assertEquals(50000.0, response.getFinancialSummary().getTotalBudget());
        assertEquals(5000.0, response.getFinancialSummary().getEmergencyReserve());
        assertEquals(45000.0, response.getFinancialSummary().getAvailableAllocationBudget());
        assertEquals(response.getTotalCost(), response.getFinancialSummary().getTotalAllocatedCost());
        assertEquals(response.getRemainingBudget(), response.getFinancialSummary().getRemainingBudget());

        // 3. Time Summary
        assertNotNull(response.getTimeSummary());
        assertEquals(16.0, response.getTimeSummary().getTotalAvailableTime());
        assertEquals(response.getTotalTimeUsed(), response.getTimeSummary().getTotalTimeUsed());
        assertEquals(response.getRemainingTime(), response.getTimeSummary().getRemainingTime());
        assertTrue(response.getTimeSummary().getTransportationTime() >= 0.0);

        // 4. Physical Resource Summary
        assertNotNull(response.getPhysicalResourceSummary());
        assertEquals(15.0, response.getPhysicalResourceSummary().getCarryingCapacity());
        assertEquals(response.getTotalWeight(), response.getPhysicalResourceSummary().getEquipmentWeightUsed());
        assertEquals(response.getRemainingCapacity(), response.getPhysicalResourceSummary().getRemainingCapacity());

        // 5. Selected Transportation
        assertNotNull(response.getSelectedTransportation());
        assertFalse(response.getSelectedTransportation().isEmpty(), "Travel plan should contain selected transportation.");

        // 6. Selected Accommodation
        assertNotNull(response.getSelectedAccommodation());
        assertFalse(response.getSelectedAccommodation().isEmpty(), "Travel plan should contain selected accommodation.");

        // 7. Selected Activities
        assertNotNull(response.getSelectedActivities());

        // 8. Selected Equipment
        assertNotNull(response.getSelectedEquipment());

        // 9. Algorithm Information
        assertEquals("DYNAMIC_PROGRAMMING", response.getAlgorithmUsed());
        assertTrue(response.getExecutionTimeMs() >= 0);
        assertTrue(response.getOverallScore() > 0.0);
    }
}
