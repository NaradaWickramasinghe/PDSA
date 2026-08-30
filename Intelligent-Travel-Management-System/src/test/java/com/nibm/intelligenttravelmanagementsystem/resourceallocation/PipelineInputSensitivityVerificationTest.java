package com.nibm.intelligenttravelmanagementsystem.resourceallocation;

import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.ResourceAllocationRequest;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.ResourceAllocationResponse;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.SelectedResourceResponse;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class PipelineInputSensitivityVerificationTest {

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

    private void printScenario(String title, ResourceAllocationRequest req, ResourceAllocationResponse resp) {
        System.out.println("================================================================================");
        System.out.println("SCENARIO: " + title);
        System.out.println("INPUT: Dest=" + req.getDestination() + ", Budget=" + req.getTotalBudget() +
                ", Reserve=" + req.getEmergencyReserve() + ", Travellers=" + req.getTravellerCount() +
                ", Days=" + req.getTripDurationDays() + ", Hours=" + req.getAvailableHours() +
                ", Luggage=" + req.getLuggageCapacity() + " kg");
        System.out.println("FEASIBLE: " + resp.isFeasible() + ", Algorithm=" + resp.getAlgorithmUsed());
        System.out.println("COST: " + resp.getTotalCost() + " LKR (Rem: " + resp.getRemainingBudget() + " LKR)");
        System.out.println("TIME: " + resp.getTotalTimeUsed() + " h (Rem: " + resp.getRemainingTime() + " h)");
        System.out.println("WEIGHT: " + resp.getTotalWeight() + " kg (Rem: " + resp.getRemainingCapacity() + " kg)");

        System.out.println("TRANSPORTATION (" + resp.getSelectedTransportation().size() + "):");
        for (SelectedResourceResponse r : resp.getSelectedTransportation()) {
            System.out.println("  - [" + r.getId() + "] " + r.getName() + " (Cost=" + r.getCost() + ", Time=" + r.getDurationHours() + "h, Cap=" + r.getCapacity() + ")");
        }

        System.out.println("EQUIPMENT (" + resp.getSelectedEquipment().size() + "):");
        for (SelectedResourceResponse r : resp.getSelectedEquipment()) {
            System.out.println("  - [" + r.getId() + "] " + r.getName() + " (Cost=" + r.getCost() + ", Weight=" + r.getWeightKg() + "kg, Usefulness=" + r.getUsefulness() + ")");
        }

        System.out.println("ACCOMMODATION (" + resp.getSelectedAccommodation().size() + "):");
        for (SelectedResourceResponse r : resp.getSelectedAccommodation()) {
            System.out.println("  - [" + r.getId() + "] " + r.getName() + " (Cost=" + r.getCost() + ", Cap=" + r.getCapacity() + ")");
        }

        System.out.println("ACTIVITIES (" + resp.getSelectedActivities().size() + "):");
        for (SelectedResourceResponse r : resp.getSelectedActivities()) {
            System.out.println("  - [" + r.getId() + "] " + r.getName() + " (Cost=" + r.getCost() + ", Time=" + r.getDurationHours() + "h, Usefulness=" + r.getUsefulness() + ")");
        }
        System.out.println("================================================================================\n");
    }

    @Test
    void runDiagnosticScenarios() {
        // 1. Destination: Ella vs Galle
        ResourceAllocationRequest reqElla = ResourceAllocationRequest.builder()
                .destination("Ella").totalBudget(50000.0).emergencyReserve(5000.0)
                .travellerCount(2).tripDurationDays(3).availableHours(18.0).luggageCapacity(15.0)
                .selectedAlgorithm("PIPELINE").build();
        printScenario("1A: Destination = Ella", reqElla, service.allocateResources(reqElla));

        ResourceAllocationRequest reqGalle = ResourceAllocationRequest.builder()
                .destination("Galle").totalBudget(50000.0).emergencyReserve(5000.0)
                .travellerCount(2).tripDurationDays(3).availableHours(18.0).luggageCapacity(15.0)
                .selectedAlgorithm("PIPELINE").build();
        printScenario("1B: Destination = Galle", reqGalle, service.allocateResources(reqGalle));

        // 2. Budget: LKR 30,000 vs LKR 80,000
        ResourceAllocationRequest reqLowBudget = ResourceAllocationRequest.builder()
                .destination("Ella").totalBudget(30000.0).emergencyReserve(5000.0)
                .travellerCount(2).tripDurationDays(3).availableHours(18.0).luggageCapacity(15.0)
                .selectedAlgorithm("PIPELINE").build();
        printScenario("2A: Budget = LKR 30,000 (Spendable: 25,000)", reqLowBudget, service.allocateResources(reqLowBudget));

        ResourceAllocationRequest reqHighBudget = ResourceAllocationRequest.builder()
                .destination("Ella").totalBudget(80000.0).emergencyReserve(5000.0)
                .travellerCount(2).tripDurationDays(3).availableHours(18.0).luggageCapacity(15.0)
                .selectedAlgorithm("PIPELINE").build();
        printScenario("2B: Budget = LKR 80,000 (Spendable: 75,000)", reqHighBudget, service.allocateResources(reqHighBudget));

        // 3. Traveller count: 2 vs 6 travellers
        ResourceAllocationRequest req2Trav = ResourceAllocationRequest.builder()
                .destination("Ella").totalBudget(75000.0).emergencyReserve(5000.0)
                .travellerCount(2).tripDurationDays(3).availableHours(18.0).luggageCapacity(15.0)
                .selectedAlgorithm("PIPELINE").build();
        printScenario("3A: Travellers = 2", req2Trav, service.allocateResources(req2Trav));

        ResourceAllocationRequest req6Trav = ResourceAllocationRequest.builder()
                .destination("Ella").totalBudget(75000.0).emergencyReserve(5000.0)
                .travellerCount(6).tripDurationDays(3).availableHours(18.0).luggageCapacity(15.0)
                .selectedAlgorithm("PIPELINE").build();
        printScenario("3B: Travellers = 6", req6Trav, service.allocateResources(req6Trav));

        // 4. Trip duration: 2 days vs 5 days
        ResourceAllocationRequest req2Days = ResourceAllocationRequest.builder()
                .destination("Ella").totalBudget(65000.0).emergencyReserve(5000.0)
                .travellerCount(2).tripDurationDays(2).availableHours(24.0).luggageCapacity(15.0)
                .selectedAlgorithm("PIPELINE").build();
        printScenario("4A: Trip Duration = 2 Days (1 Night)", req2Days, service.allocateResources(req2Days));

        ResourceAllocationRequest req5Days = ResourceAllocationRequest.builder()
                .destination("Ella").totalBudget(65000.0).emergencyReserve(5000.0)
                .travellerCount(2).tripDurationDays(5).availableHours(24.0).luggageCapacity(15.0)
                .selectedAlgorithm("PIPELINE").build();
        printScenario("4B: Trip Duration = 5 Days (4 Nights)", req5Days, service.allocateResources(req5Days));

        // 5. Available time: 10 hours vs 30 hours
        ResourceAllocationRequest req10Hours = ResourceAllocationRequest.builder()
                .destination("Ella").totalBudget(60000.0).emergencyReserve(5000.0)
                .travellerCount(2).tripDurationDays(3).availableHours(10.0).luggageCapacity(15.0)
                .selectedAlgorithm("PIPELINE").build();
        printScenario("5A: Available Time = 10.0 Hours", req10Hours, service.allocateResources(req10Hours));

        ResourceAllocationRequest req30Hours = ResourceAllocationRequest.builder()
                .destination("Ella").totalBudget(60000.0).emergencyReserve(5000.0)
                .travellerCount(2).tripDurationDays(3).availableHours(30.0).luggageCapacity(15.0)
                .selectedAlgorithm("PIPELINE").build();
        printScenario("5B: Available Time = 30.0 Hours", req30Hours, service.allocateResources(req30Hours));

        // 6. Luggage capacity: 2 kg vs 10 kg
        ResourceAllocationRequest req2Kg = ResourceAllocationRequest.builder()
                .destination("Ella").totalBudget(50000.0).emergencyReserve(5000.0)
                .travellerCount(2).tripDurationDays(3).availableHours(18.0).luggageCapacity(2.0)
                .selectedAlgorithm("PIPELINE").build();
        printScenario("6A: Luggage Capacity = 2.0 kg", req2Kg, service.allocateResources(req2Kg));

        ResourceAllocationRequest req10Kg = ResourceAllocationRequest.builder()
                .destination("Ella").totalBudget(50000.0).emergencyReserve(5000.0)
                .travellerCount(2).tripDurationDays(3).availableHours(18.0).luggageCapacity(10.0)
                .selectedAlgorithm("PIPELINE").build();
        printScenario("6B: Luggage Capacity = 10.0 kg", req10Kg, service.allocateResources(req10Kg));
    }
}
