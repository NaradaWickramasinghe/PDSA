package com.nibm.intelligenttravelmanagementsystem.resourceallocation;

import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.ResourceAllocationRequest;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.ResourceAllocationResponse;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.SelectedResourceResponse;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.AllocationProblem;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.AllocationResult;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.ResourceCategory;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.ResourceOption;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PipelineResourceAllocationTest {

    private ResourceAllocationService service;
    private PipelineAllocationService pipelineService;
    private GreedyAllocationService greedyService;
    private DynamicProgrammingAllocationService dpService;
    private GeneticAllocationService geneticService;
    private ResourceDataProvider dataProvider;

    @BeforeEach
    void setUp() {
        greedyService = new GreedyAllocationService();
        dpService = new DynamicProgrammingAllocationService();
        geneticService = new GeneticAllocationService();
        pipelineService = new PipelineAllocationService(greedyService, dpService, geneticService);
        dataProvider = new InMemorySampleDataProvider();

        service = new ResourceAllocationService(
                List.of(pipelineService, greedyService, dpService, geneticService),
                dataProvider
        );
    }

    @Test
    @DisplayName("1. Pipeline Mode executes all three stages and combines Transportation, Accommodation, Activities, and Equipment")
    void testPipelineModeCombinesAllFourCategories() {
        ResourceAllocationRequest request = ResourceAllocationRequest.builder()
                .destination("Ella")
                .totalBudget(50000.0)
                .emergencyReserve(5000.0)
                .availableHours(18.0)
                .luggageCapacity(15.0)
                .travellerCount(2)
                .tripDurationDays(3)
                .selectedAlgorithm("PIPELINE")
                .build();

        ResourceAllocationResponse response = service.allocateResources(request);

        assertNotNull(response);
        assertTrue(response.isFeasible(), "Pipeline result must be feasible for standard constraints.");
        assertEquals("PIPELINE", response.getAlgorithmUsed());

        // Verify all 4 categories are present
        assertNotNull(response.getSelectedTransportation(), "Transportation list should not be null.");
        assertFalse(response.getSelectedTransportation().isEmpty(), "Transportation should be selected.");

        assertNotNull(response.getSelectedAccommodation(), "Accommodation list should not be null.");
        assertFalse(response.getSelectedAccommodation().isEmpty(), "Accommodation should be selected for 3-day trip.");

        assertNotNull(response.getSelectedActivities(), "Activities list should not be null.");
        assertFalse(response.getSelectedActivities().isEmpty(), "Activities should be selected when hours permit.");

        assertNotNull(response.getSelectedEquipment(), "Equipment list should not be null.");
        assertFalse(response.getSelectedEquipment().isEmpty(), "Equipment should be selected under luggage capacity.");

        // Verify hard constraint compliance
        double effectiveBudget = request.getTotalBudget() - request.getEmergencyReserve();
        assertTrue(response.getTotalCost() <= effectiveBudget + 1e-6,
                "Total cost (" + response.getTotalCost() + ") must not exceed spendable budget (" + effectiveBudget + ").");
        assertTrue(response.getTotalTimeUsed() <= request.getAvailableHours() + 1e-6,
                "Total time used must not exceed available hours.");
        assertTrue(response.getTotalWeight() <= request.getLuggageCapacity() + 1e-6,
                "Total weight must not exceed luggage capacity.");
    }

    @Test
    @DisplayName("2. Default algorithm mode is PIPELINE when selectedAlgorithm is null or empty")
    void testDefaultAlgorithmIsPipeline() {
        ResourceAllocationRequest request = ResourceAllocationRequest.builder()
                .destination("Kandy")
                .totalBudget(45000.0)
                .emergencyReserve(5000.0)
                .availableHours(16.0)
                .luggageCapacity(12.0)
                .travellerCount(2)
                .tripDurationDays(3)
                .selectedAlgorithm(null) // omitted -> should default to PIPELINE
                .build();

        ResourceAllocationResponse response = service.allocateResources(request);

        assertNotNull(response);
        assertTrue(response.isFeasible());
        assertEquals("PIPELINE", response.getAlgorithmUsed());
        assertFalse(response.getSelectedResources().isEmpty());
    }

    @Test
    @DisplayName("3. Stage 1 (Greedy): Transportation is selected based on capacity and cost efficiency")
    void testStage1GreedyTransportationSelection() {
        AllocationProblem problem = AllocationProblem.builder()
                .destination("Sigiriya")
                .travellerCount(4) // 4 travellers: 2-person options must be rejected
                .tripDurationDays(3)
                .totalBudget(60000.0)
                .emergencyReserve(5000.0)
                .maxAvailableHours(18.0)
                .maxCarryingCapacityKg(20.0)
                .candidateOptions(dataProvider.getCandidateOptions("Sigiriya"))
                .build();

        ResourceOption transport = greedyService.allocateTransportation(
                problem,
                55000.0,
                18.0,
                20.0,
                4
        );

        assertNotNull(transport, "Greedy transport should find a vehicle for 4 travellers.");
        assertEquals(ResourceCategory.TRANSPORTATION, transport.getCategory());
        assertTrue(transport.getCapacity() == null || transport.getCapacity() >= 4,
                "Selected transport must hold at least 4 travellers.");
    }

    @Test
    @DisplayName("4. Stage 2 (Dynamic Programming): 0/1 Knapsack selects optimal equipment under weight constraint")
    void testStage2DynamicProgrammingEquipmentSelection() {
        AllocationProblem problem = AllocationProblem.builder()
                .destination("ALL")
                .travellerCount(2)
                .tripDurationDays(3)
                .totalBudget(50000.0)
                .emergencyReserve(5000.0)
                .maxAvailableHours(18.0)
                .maxCarryingCapacityKg(5.0) // 5 kg limit
                .candidateOptions(dataProvider.getCandidateOptions("ALL"))
                .build();

        List<ResourceOption> equipment = dpService.allocateEquipment(problem, 10000.0, 5.0);

        assertNotNull(equipment);
        assertFalse(equipment.isEmpty(), "DP knapsack should select equipment within 5.0 kg.");

        double totalWeight = equipment.stream().mapToDouble(ResourceOption::getWeightKg).sum();
        assertTrue(totalWeight <= 5.0 + 1e-6, "Equipment weight (" + totalWeight + ") must not exceed 5.0 kg.");

        for (ResourceOption eq : equipment) {
            assertEquals(ResourceCategory.PHYSICAL_ITEM, eq.getCategory());
        }
    }

    @Test
    @DisplayName("5. Stage 3 (Genetic Algorithm): Accommodation and activities are optimized under remaining budget and time")
    void testStage3GeneticAlgorithmExperienceOptimization() {
        AllocationProblem problem = AllocationProblem.builder()
                .destination("Galle")
                .travellerCount(2)
                .tripDurationDays(3)
                .totalBudget(50000.0)
                .emergencyReserve(5000.0)
                .maxAvailableHours(18.0)
                .maxCarryingCapacityKg(15.0)
                .candidateOptions(dataProvider.getCandidateOptions("Galle"))
                .build();

        // Simulate remaining budget and time after transport and gear
        double remainingBudget = 30000.0;
        double remainingHours = 14.0;

        GeneticAllocationService.GeneticStageResult gaResult = geneticService.optimizeAccommodationAndActivities(
                problem,
                remainingBudget,
                remainingHours,
                2,
                3
        );

        assertNotNull(gaResult);
        assertNotNull(gaResult.getSelectedAccommodation(), "GA must select accommodation for a 3-day multi-day trip.");
        assertEquals(ResourceCategory.ACCOMMODATION, gaResult.getSelectedAccommodation().getCategory());

        assertFalse(gaResult.getSelectedActivities().isEmpty(), "GA must select feasible activities.");
        for (ResourceOption act : gaResult.getSelectedActivities()) {
            assertEquals(ResourceCategory.ACTIVITY, act.getCategory());
        }

        assertTrue(gaResult.getTotalCost() <= remainingBudget + 1e-6, "GA total cost must fit remaining budget.");
        assertTrue(gaResult.getTotalTime() <= remainingHours + 1e-6, "GA total time must fit remaining hours.");
    }

    @Test
    @DisplayName("6. Results from earlier pipeline stages materially deduct budget and time for downstream stages")
    void testSequentialCascadingDeductionAcrossStages() {
        ResourceAllocationRequest request = ResourceAllocationRequest.builder()
                .destination("Ella")
                .totalBudget(35000.0)
                .emergencyReserve(5000.0) // 30,000 spendable
                .availableHours(12.0)
                .luggageCapacity(10.0)
                .travellerCount(2)
                .tripDurationDays(2)
                .selectedAlgorithm("PIPELINE")
                .build();

        ResourceAllocationResponse response = service.allocateResources(request);

        assertTrue(response.isFeasible());

        // Verify transportation consumed part of the budget
        assertFalse(response.getSelectedTransportation().isEmpty());
        double transportCost = response.getSelectedTransportation().get(0).getCost();
        double transportTime = response.getSelectedTransportation().get(0).getDurationHours();

        // Verify downstream activities and equipment respected the diminished budget
        double totalCost = response.getTotalCost();
        assertTrue(totalCost >= transportCost, "Total cost must include transport cost.");
        assertTrue(response.getRemainingBudget() >= 0.0, "Remaining budget must not be negative.");
        assertTrue(response.getRemainingTime() >= 0.0, "Remaining time must not be negative.");
    }

    @Test
    @DisplayName("7. Different destinations provide distinct destination-specific travel plans via pipeline")
    void testDestinationAwarePipelinePlan() {
        ResourceAllocationRequest reqElla = ResourceAllocationRequest.builder()
                .destination("Ella")
                .totalBudget(50000.0)
                .emergencyReserve(5000.0)
                .availableHours(18.0)
                .luggageCapacity(15.0)
                .travellerCount(2)
                .tripDurationDays(3)
                .build();

        ResourceAllocationRequest reqGalle = ResourceAllocationRequest.builder()
                .destination("Galle")
                .totalBudget(50000.0)
                .emergencyReserve(5000.0)
                .availableHours(18.0)
                .luggageCapacity(15.0)
                .travellerCount(2)
                .tripDurationDays(3)
                .build();

        ResourceAllocationResponse respElla = service.allocateResources(reqElla);
        ResourceAllocationResponse respGalle = service.allocateResources(reqGalle);

        assertTrue(respElla.isFeasible());
        assertTrue(respGalle.isFeasible());

        // Check Ella plan
        for (SelectedResourceResponse item : respElla.getSelectedResources()) {
            boolean valid = item.getId().startsWith("ELLA") || item.getId().startsWith("EQ");
            assertTrue(valid, "Ella plan should only contain Ella items or General Gear, found: " + item.getId());
        }

        // Check Galle plan
        for (SelectedResourceResponse item : respGalle.getSelectedResources()) {
            boolean valid = item.getId().startsWith("GALLE") || item.getId().startsWith("EQ");
            assertTrue(valid, "Galle plan should only contain Galle items or General Gear, found: " + item.getId());
        }

        // Verify plans are materially different
        assertNotEquals(respElla.getSelectedTransportation().get(0).getId(),
                respGalle.getSelectedTransportation().get(0).getId());
    }
}
