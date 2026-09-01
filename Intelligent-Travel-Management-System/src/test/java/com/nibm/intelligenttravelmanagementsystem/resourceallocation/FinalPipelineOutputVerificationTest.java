package com.nibm.intelligenttravelmanagementsystem.resourceallocation;

import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.ResourceAllocationRequest;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.ResourceAllocationResponse;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.SelectedResourceResponse;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FinalPipelineOutputVerificationTest {

    private ResourceAllocationService service;

    @BeforeEach
    void setUp() {
        GreedyAllocationService greedy = new GreedyAllocationService();
        DynamicProgrammingAllocationService dp = new DynamicProgrammingAllocationService();
        GeneticAllocationService genetic = new GeneticAllocationService();
        PipelineAllocationService pipeline = new PipelineAllocationService(greedy, dp, genetic);
        JsonResourceDataProvider dataProvider = new JsonResourceDataProvider();

        service = new ResourceAllocationService(
                List.of(pipeline, greedy, dp, genetic),
                dataProvider
        );
    }

    private void printScenarioReport(String scenarioName, ResourceAllocationResponse resp) {
        System.out.println("================================================================================");
        System.out.println("FINAL VERIFIED REPORT: " + scenarioName);
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("TRIP INFO: Destination=" + resp.getTripInformation().getDestination() +
                ", Duration=" + resp.getTripInformation().getTripDurationDays() + " days (" +
                resp.getTripInformation().getDurationHours() + "h), Travellers=" +
                resp.getTripInformation().getTravellerCount());

        System.out.println("FINANCIAL: Total Budget=" + resp.getFinancialSummary().getTotalBudget() + " LKR" +
                ", Reserve=" + resp.getFinancialSummary().getEmergencyReserve() + " LKR" +
                ", Available=" + resp.getFinancialSummary().getAvailableAllocationBudget() + " LKR" +
                ", Allocated=" + resp.getFinancialSummary().getTotalAllocatedCost() + " LKR" +
                ", Remaining=" + resp.getFinancialSummary().getRemainingBudget() + " LKR");

        System.out.println("TIME: Available=" + resp.getTimeSummary().getTotalAvailableTime() + "h" +
                ", Transport Time=" + resp.getTimeSummary().getTransportationTime() + "h" +
                ", Activity Time=" + resp.getTimeSummary().getActivityTime() + "h" +
                ", Total Time Used=" + resp.getTimeSummary().getTotalTimeUsed() + "h" +
                ", Remaining Time=" + resp.getTimeSummary().getRemainingTime() + "h");

        System.out.println("PHYSICAL RESOURCE: Capacity=" + resp.getPhysicalResourceSummary().getCarryingCapacity() + " kg" +
                ", Weight Used=" + resp.getPhysicalResourceSummary().getEquipmentWeightUsed() + " kg" +
                ", Remaining Capacity=" + resp.getPhysicalResourceSummary().getRemainingCapacity() + " kg");

        System.out.println("OVERALL USEFULNESS SCORE: " + resp.getOverallScore());
        System.out.println("ALGORITHM USED: " + resp.getAlgorithmUsed());
        System.out.println("STATUS MESSAGE: " + resp.getStatusMessage());

        System.out.println("\n1. SELECTED TRANSPORTATION (" + resp.getSelectedTransportation().size() + "):");
        for (SelectedResourceResponse tr : resp.getSelectedTransportation()) {
            System.out.println("   * [" + tr.getId() + "] " + tr.getName() + " | Cost: " + tr.getCost() + " LKR | Time: " + tr.getDurationHours() + "h | Cap: " + tr.getCapacity());
        }

        System.out.println("\n2. SELECTED ACCOMMODATION (" + resp.getSelectedAccommodation().size() + "):");
        for (SelectedResourceResponse ac : resp.getSelectedAccommodation()) {
            System.out.println("   * [" + ac.getId() + "] " + ac.getName() + " | Cost: " + ac.getCost() + " LKR | Cap: " + ac.getCapacity());
        }

        System.out.println("\n3. SELECTED ACTIVITIES (" + resp.getSelectedActivities().size() + "):");
        for (SelectedResourceResponse act : resp.getSelectedActivities()) {
            System.out.println("   * [" + act.getId() + "] " + act.getName() + " | Cost: " + act.getCost() + " LKR | Time: " + act.getDurationHours() + "h | Usefulness: " + act.getUsefulness());
        }

        System.out.println("\n4. SELECTED EQUIPMENT (" + resp.getSelectedEquipment().size() + "):");
        for (SelectedResourceResponse eq : resp.getSelectedEquipment()) {
            System.out.println("   * [" + eq.getId() + "] " + eq.getName() + " | Cost: " + eq.getCost() + " LKR | Weight: " + eq.getWeightKg() + "kg | Usefulness: " + eq.getUsefulness());
        }
        System.out.println("================================================================================\n");
    }

    @Test
    @DisplayName("SCENARIO 1: Ella, 2 Travellers, 3 Days, Budget 60,000 LKR, 24 Hours, 10 kg")
    void testScenario1EllaStandard() {
        ResourceAllocationRequest request = ResourceAllocationRequest.builder()
                .destination("Ella")
                .travellerCount(2)
                .tripDurationDays(3)
                .totalBudget(60000.0)
                .emergencyReserve(5000.0)
                .availableHours(24.0)
                .luggageCapacity(10.0)
                .selectedAlgorithm("PIPELINE")
                .build();

        ResourceAllocationResponse response = service.allocateResources(request);

        assertNotNull(response);
        assertTrue(response.isFeasible(), "Scenario 1 must be feasible.");
        printScenarioReport("SCENARIO 1 (Ella - 2 Pax, 3 Days)", response);

        // Verify all 4 categories appear
        assertFalse(response.getSelectedTransportation().isEmpty(), "Transportation must be selected.");
        assertFalse(response.getSelectedAccommodation().isEmpty(), "Accommodation must be selected.");
        assertFalse(response.getSelectedActivities().isEmpty(), "Activities must be selected.");
        assertFalse(response.getSelectedEquipment().isEmpty(), "Equipment must be selected.");

        // Verify summaries
        assertEquals("Ella", response.getTripInformation().getDestination());
        assertEquals(2, response.getTripInformation().getTravellerCount());
        assertEquals(3, response.getTripInformation().getTripDurationDays());
        assertTrue(response.getFinancialSummary().getTotalAllocatedCost() <= 55000.0 + 1e-6);
        assertTrue(response.getTimeSummary().getTotalTimeUsed() <= 24.0 + 1e-6);
        assertTrue(response.getPhysicalResourceSummary().getEquipmentWeightUsed() <= 10.0 + 1e-6);
    }

    @Test
    @DisplayName("SCENARIO 2: Galle, 2 Travellers, 3 Days, Budget 60,000 LKR, 24 Hours, 10 kg")
    void testScenario2GalleStandard() {
        ResourceAllocationRequest request = ResourceAllocationRequest.builder()
                .destination("Galle")
                .travellerCount(2)
                .tripDurationDays(3)
                .totalBudget(60000.0)
                .emergencyReserve(5000.0)
                .availableHours(24.0)
                .luggageCapacity(10.0)
                .selectedAlgorithm("PIPELINE")
                .build();

        ResourceAllocationResponse response = service.allocateResources(request);

        assertNotNull(response);
        assertTrue(response.isFeasible(), "Scenario 2 must be feasible.");
        printScenarioReport("SCENARIO 2 (Galle - 2 Pax, 3 Days)", response);

        // Verify all 4 categories appear
        assertFalse(response.getSelectedTransportation().isEmpty(), "Transportation must be selected.");
        assertFalse(response.getSelectedAccommodation().isEmpty(), "Accommodation must be selected.");
        assertFalse(response.getSelectedActivities().isEmpty(), "Activities must be selected.");
        assertFalse(response.getSelectedEquipment().isEmpty(), "Equipment must be selected.");

        // Verify Galle-specific IDs
        assertTrue(response.getSelectedTransportation().get(0).getId().startsWith("GALLE"));
        assertTrue(response.getSelectedAccommodation().get(0).getId().startsWith("GALLE"));
        assertTrue(response.getSelectedActivities().stream().allMatch(a -> a.getId().startsWith("GALLE")));

        // Verify constraints
        assertTrue(response.getTotalCost() <= 55000.0 + 1e-6);
        assertTrue(response.getTotalTimeUsed() <= 24.0 + 1e-6);
        assertTrue(response.getTotalWeight() <= 10.0 + 1e-6);
    }

    @Test
    @DisplayName("SCENARIO 3: Ella, 6 Travellers, 3 Days, Budget 80,000 LKR, 24 Hours, 10 kg")
    void testScenario3EllaGroup() {
        ResourceAllocationRequest request = ResourceAllocationRequest.builder()
                .destination("Ella")
                .travellerCount(6)
                .tripDurationDays(3)
                .totalBudget(80000.0)
                .emergencyReserve(5000.0)
                .availableHours(24.0)
                .luggageCapacity(10.0)
                .selectedAlgorithm("PIPELINE")
                .build();

        ResourceAllocationResponse response = service.allocateResources(request);

        assertNotNull(response);
        assertTrue(response.isFeasible(), "Scenario 3 must be feasible.");
        printScenarioReport("SCENARIO 3 (Ella - 6 Pax Group, 3 Days)", response);

        // Verify all 4 categories appear
        assertFalse(response.getSelectedTransportation().isEmpty(), "Group transportation must be selected.");
        assertFalse(response.getSelectedAccommodation().isEmpty(), "Group accommodation must be selected.");
        assertFalse(response.getSelectedActivities().isEmpty(), "Activities must be selected.");
        assertFalse(response.getSelectedEquipment().isEmpty(), "Equipment must be selected.");

        // Verify transport capacity is at least 6
        assertTrue(response.getSelectedTransportation().get(0).getCapacity() >= 6,
                "Transportation must hold at least 6 travellers.");

        // Verify constraints
        assertTrue(response.getTotalCost() <= 75000.0 + 1e-6);
        assertTrue(response.getTotalTimeUsed() <= 24.0 + 1e-6);
        assertTrue(response.getTotalWeight() <= 10.0 + 1e-6);
    }

    @Test
    @DisplayName("SCENARIO 4: Ella, 2 Travellers, 5 Days, Budget 100,000 LKR, 30 Hours, 15 kg")
    void testScenario4EllaExtendedTrip() {
        ResourceAllocationRequest request = ResourceAllocationRequest.builder()
                .destination("Ella")
                .travellerCount(2)
                .tripDurationDays(5)
                .totalBudget(100000.0)
                .emergencyReserve(5000.0)
                .availableHours(30.0)
                .luggageCapacity(15.0)
                .selectedAlgorithm("PIPELINE")
                .build();

        ResourceAllocationResponse response = service.allocateResources(request);

        assertNotNull(response);
        assertTrue(response.isFeasible(), "Scenario 4 must be feasible.");
        printScenarioReport("SCENARIO 4 (Ella - 2 Pax, 5 Days Extended)", response);

        // Verify all 4 categories appear
        assertFalse(response.getSelectedTransportation().isEmpty(), "Transportation must be selected.");
        assertFalse(response.getSelectedAccommodation().isEmpty(), "Accommodation must be selected.");
        assertFalse(response.getSelectedActivities().isEmpty(), "Activities must be selected.");
        assertFalse(response.getSelectedEquipment().isEmpty(), "Equipment must be selected.");

        // Verify 5-day duration in summary
        assertEquals(5, response.getTripInformation().getTripDurationDays());

        // Verify constraints
        assertTrue(response.getTotalCost() <= 95000.0 + 1e-6);
        assertTrue(response.getTotalTimeUsed() <= 30.0 + 1e-6);
        assertTrue(response.getTotalWeight() <= 15.0 + 1e-6);
    }
}
