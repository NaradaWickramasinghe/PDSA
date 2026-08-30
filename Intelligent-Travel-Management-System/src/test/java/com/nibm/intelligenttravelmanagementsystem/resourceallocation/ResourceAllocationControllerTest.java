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
class ResourceAllocationControllerTest {

    @Autowired
    private ResourceAllocationService service;

    @Test
    @DisplayName("1. Frontend Pipeline Test: Ella + 2 travellers + 3 days")
    void testFrontendPayloadElla() {
        // Request payload exactly matching the updated frontend submitForm
        ResourceAllocationRequest request = ResourceAllocationRequest.builder()
                .destination("Ella")
                .tripDurationDays(3)
                .travellerCount(2)
                .totalBudget(60000.0)
                .emergencyReserve(5000.0)
                .availableHours(18.0)
                .luggageCapacity(15.0)
                .selectedAlgorithm("PIPELINE")
                .build();

        ResourceAllocationResponse response = service.allocateResources(request);

        assertNotNull(response);
        assertTrue(response.isFeasible(), "Response must be feasible for standard Ella trip.");
        assertEquals("PIPELINE", response.getAlgorithmUsed());

        // 1. Trip Information
        assertNotNull(response.getTripInformation());
        assertEquals("Ella", response.getTripInformation().getDestination());
        assertEquals(3, response.getTripInformation().getTripDurationDays());
        assertEquals(2, response.getTripInformation().getTravellerCount());

        // 2. Financial Summary
        assertNotNull(response.getFinancialSummary());
        assertEquals(60000.0, response.getFinancialSummary().getTotalBudget());
        assertEquals(5000.0, response.getFinancialSummary().getEmergencyReserve());
        assertEquals(55000.0, response.getFinancialSummary().getAvailableAllocationBudget());
        assertTrue(response.getFinancialSummary().getTotalAllocatedCost() > 0);
        assertTrue(response.getFinancialSummary().getRemainingBudget() >= 0);

        // 3. Transportation
        assertNotNull(response.getSelectedTransportation());
        assertFalse(response.getSelectedTransportation().isEmpty(), "Must select transportation");
        assertTrue(response.getSelectedTransportation().get(0).getId().startsWith("ELLA"));

        // 4. Accommodation
        assertNotNull(response.getSelectedAccommodation());
        assertFalse(response.getSelectedAccommodation().isEmpty(), "Must select accommodation");
        assertTrue(response.getSelectedAccommodation().get(0).getId().startsWith("ELLA"));

        // 5. Activities
        assertNotNull(response.getSelectedActivities());
        assertFalse(response.getSelectedActivities().isEmpty(), "Must select activities");
        assertTrue(response.getSelectedActivities().stream().allMatch(a -> a.getId().startsWith("ELLA")));

        // 6. Equipment
        assertNotNull(response.getSelectedEquipment());
        assertFalse(response.getSelectedEquipment().isEmpty(), "Must select equipment");

        // 7. Time Summary
        assertNotNull(response.getTimeSummary());
        assertEquals(18.0, response.getTimeSummary().getTotalAvailableTime());
        assertTrue(response.getTimeSummary().getTransportationTime() > 0);
        assertTrue(response.getTimeSummary().getActivityTime() > 0);
        assertTrue(response.getTimeSummary().getTotalTimeUsed() <= 18.0);

        // 8. Physical Resource Summary
        assertNotNull(response.getPhysicalResourceSummary());
        assertEquals(15.0, response.getPhysicalResourceSummary().getCarryingCapacity());
        assertTrue(response.getPhysicalResourceSummary().getEquipmentWeightUsed() <= 15.0);

        // 9. Algorithm Information
        assertTrue(response.getOverallScore() > 0);
        assertNotNull(response.getStatusMessage());
    }

    @Test
    @DisplayName("2. Frontend Pipeline Test: Galle + 2 travellers + 3 days")
    void testFrontendPayloadGalle() {
        // Request payload exactly matching the updated frontend submitForm
        ResourceAllocationRequest request = ResourceAllocationRequest.builder()
                .destination("Galle")
                .tripDurationDays(3)
                .travellerCount(2)
                .totalBudget(60000.0)
                .emergencyReserve(5000.0)
                .availableHours(18.0)
                .luggageCapacity(15.0)
                .selectedAlgorithm("PIPELINE")
                .build();

        ResourceAllocationResponse response = service.allocateResources(request);

        assertNotNull(response);
        assertTrue(response.isFeasible(), "Response must be feasible for standard Galle trip.");
        assertEquals("PIPELINE", response.getAlgorithmUsed());

        // 1. Trip Information
        assertNotNull(response.getTripInformation());
        assertEquals("Galle", response.getTripInformation().getDestination());
        assertEquals(3, response.getTripInformation().getTripDurationDays());
        assertEquals(2, response.getTripInformation().getTravellerCount());

        // 2. Financial Summary
        assertNotNull(response.getFinancialSummary());
        assertEquals(60000.0, response.getFinancialSummary().getTotalBudget());
        assertTrue(response.getFinancialSummary().getTotalAllocatedCost() > 0);

        // 3. Transportation (Galle-specific)
        assertNotNull(response.getSelectedTransportation());
        assertFalse(response.getSelectedTransportation().isEmpty());
        assertTrue(response.getSelectedTransportation().get(0).getId().startsWith("GALLE"));

        // 4. Accommodation (Galle-specific)
        assertNotNull(response.getSelectedAccommodation());
        assertFalse(response.getSelectedAccommodation().isEmpty());
        assertTrue(response.getSelectedAccommodation().get(0).getId().startsWith("GALLE"));

        // 5. Activities (Galle-specific)
        assertNotNull(response.getSelectedActivities());
        assertFalse(response.getSelectedActivities().isEmpty());
        assertTrue(response.getSelectedActivities().stream().allMatch(a -> a.getId().startsWith("GALLE")));

        // 6. Equipment
        assertNotNull(response.getSelectedEquipment());
        assertFalse(response.getSelectedEquipment().isEmpty());

        // 7. Time Summary
        assertNotNull(response.getTimeSummary());
        assertTrue(response.getTimeSummary().getTotalTimeUsed() <= 18.0);

        // 8. Physical Summary
        assertNotNull(response.getPhysicalResourceSummary());
        assertTrue(response.getPhysicalResourceSummary().getEquipmentWeightUsed() <= 15.0);

        // 9. Algorithm Info
        assertTrue(response.getOverallScore() > 0);
        assertNotNull(response.getStatusMessage());
    }
}
