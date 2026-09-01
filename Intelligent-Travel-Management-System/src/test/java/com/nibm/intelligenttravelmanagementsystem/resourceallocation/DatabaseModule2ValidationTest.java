package com.nibm.intelligenttravelmanagementsystem.resourceallocation;

import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.ResourceAllocationRequest;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.ResourceAllocationResponse;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.ResourceCategory;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.ResourceOption;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.repository.ResourceOptionEntity;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.repository.ResourceOptionRepository;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.service.DatabaseResourceDataProvider;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.service.ResourceAllocationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DatabaseModule2ValidationTest {

    @Autowired
    private ResourceOptionRepository repository;

    @Autowired
    private DatabaseResourceDataProvider dataProvider;

    @Autowired
    private ResourceAllocationService allocationService;

    @Test
    @DisplayName("1. Verify exactly 136 records exist in database with unique IDs")
    void testExactRecordCountAndUniqueIds() {
        List<ResourceOptionEntity> all = repository.findAll();
        assertEquals(136, all.size(), "Database must contain exactly 136 resource records.");

        long uniqueIdCount = all.stream().map(ResourceOptionEntity::getId).distinct().count();
        assertEquals(136, uniqueIdCount, "All 136 IDs must be unique.");
    }

    @Test
    @DisplayName("2. Verify four categories match expected counts (48, 40, 34, 14)")
    void testCategoryCounts() {
        List<ResourceOptionEntity> all = repository.findAll();
        Map<ResourceCategory, Long> categoryCounts = all.stream()
                .collect(Collectors.groupingBy(ResourceOptionEntity::getCategory, Collectors.counting()));

        assertEquals(48L, categoryCounts.get(ResourceCategory.ACTIVITY), "ACTIVITY count must be 48");
        assertEquals(40L, categoryCounts.get(ResourceCategory.ACCOMMODATION), "ACCOMMODATION count must be 40");
        assertEquals(34L, categoryCounts.get(ResourceCategory.TRANSPORTATION), "TRANSPORTATION count must be 34");
        assertEquals(14L, categoryCounts.get(ResourceCategory.PHYSICAL_ITEM), "PHYSICAL_ITEM count must be 14");
    }

    @Test
    @DisplayName("3. Verify all 7 destinations and ALL match expected counts")
    void testDestinationCounts() {
        List<ResourceOptionEntity> all = repository.findAll();
        Map<String, Long> destCounts = all.stream()
                .collect(Collectors.groupingBy(ResourceOptionEntity::getDestination, Collectors.counting()));

        assertEquals(24L, destCounts.get("Ella"), "Ella count must be 24");
        assertEquals(18L, destCounts.get("Galle"), "Galle count must be 18");
        assertEquals(18L, destCounts.get("Kandy"), "Kandy count must be 18");
        assertEquals(18L, destCounts.get("Nuwara Eliya"), "Nuwara Eliya count must be 18");
        assertEquals(18L, destCounts.get("Sigiriya"), "Sigiriya count must be 18");
        assertEquals(14L, destCounts.get("Mirissa"), "Mirissa count must be 14");
        assertEquals(12L, destCounts.get("Colombo"), "Colombo count must be 12");
        assertEquals(14L, destCounts.get("ALL"), "Universal equipment (ALL) count must be 14");
    }

    @Test
    @DisplayName("4. Verify 14 universal equipment records have destination ALL and non-zero weight")
    void testUniversalEquipment() {
        List<ResourceOptionEntity> equipment = repository.findAll().stream()
                .filter(r -> r.getCategory() == ResourceCategory.PHYSICAL_ITEM)
                .toList();

        assertEquals(14, equipment.size());
        for (ResourceOptionEntity eq : equipment) {
            assertEquals("ALL", eq.getDestination(), "Universal equipment must have destination 'ALL'");
            assertTrue(eq.getWeightKg() > 0.0, "Physical equipment must have weight > 0 kg");
            assertTrue(eq.isAvailable(), "Physical equipment must be available");
        }
    }

    @Test
    @DisplayName("5. Verify destination filtering retrieves destination items plus universal equipment")
    void testDestinationFiltering() {
        // Ella: 24 local + 14 ALL = 38
        List<ResourceOption> ellaOptions = dataProvider.getCandidateOptions("Ella");
        assertEquals(38, ellaOptions.size(), "Ella candidates must be 24 destination items + 14 universal equipment");

        // Galle: 18 local + 14 ALL = 32
        List<ResourceOption> galleOptions = dataProvider.getCandidateOptions("Galle");
        assertEquals(32, galleOptions.size(), "Galle candidates must be 18 destination items + 14 universal equipment");

        // Kandy: 18 local + 14 ALL = 32
        List<ResourceOption> kandyOptions = dataProvider.getCandidateOptions("Kandy");
        assertEquals(32, kandyOptions.size(), "Kandy candidates must be 18 destination items + 14 universal equipment");
    }

    @Test
    @DisplayName("6. Pipeline Verification: Run optimization pipeline for Ella, Galle, and Kandy using database-loaded resources")
    void testPipelineWithDatabaseLoadedResources() {
        String[] testDestinations = {"Ella", "Galle", "Kandy"};

        for (String destination : testDestinations) {
            ResourceAllocationRequest request = ResourceAllocationRequest.builder()
                    .destination(destination)
                    .tripDurationDays(3)
                    .travellerCount(2)
                    .totalBudget(100000.0)
                    .emergencyReserve(10000.0)
                    .availableHours(30.0)
                    .luggageCapacity(25.0)
                    .selectedAlgorithm("PIPELINE")
                    .build();

            ResourceAllocationResponse response = allocationService.allocateResources(request);

            assertNotNull(response, "Response for " + destination + " must not be null");
            assertTrue(response.isFeasible(), "Pipeline must find feasible allocation for " + destination);

            // Verify all 4 categories are covered in the allocated plan
            assertNotNull(response.getSelectedTransportation(), "Plan must have transportation for " + destination);
            assertFalse(response.getSelectedTransportation().isEmpty(), "Transportation list must not be empty for " + destination);

            assertNotNull(response.getSelectedAccommodation(), "Plan must have accommodation for " + destination);
            assertFalse(response.getSelectedAccommodation().isEmpty(), "Accommodation list must not be empty for " + destination);

            assertNotNull(response.getSelectedActivities(), "Plan must have activities for " + destination);
            assertFalse(response.getSelectedActivities().isEmpty(), "Activities list must not be empty for " + destination);

            assertNotNull(response.getSelectedEquipment(), "Plan must have equipment for " + destination);
            assertFalse(response.getSelectedEquipment().isEmpty(), "Equipment list must not be empty for " + destination);

            // Verify budget constraint: usable budget is 100000 - 10000 = 90000
            assertTrue(response.getTotalCost() <= 90000.0, "Total cost must not exceed usable budget");
        }
    }
}
