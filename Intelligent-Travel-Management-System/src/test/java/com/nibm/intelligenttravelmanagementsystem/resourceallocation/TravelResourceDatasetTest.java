package com.nibm.intelligenttravelmanagementsystem.resourceallocation;

import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.ResourceCategory;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.ResourceOption;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.service.JsonResourceDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class TravelResourceDatasetTest {

    private JsonResourceDataProvider dataProvider;

    @BeforeEach
    void setUp() {
        dataProvider = new JsonResourceDataProvider();
    }

    @Test
    @DisplayName("1. Dataset loads properly and contains all 5 mandatory destinations plus equipment")
    void testDatasetLoadingAllDestinations() {
        List<ResourceOption> allCandidates = dataProvider.getCandidateOptions();
        assertNotNull(allCandidates);
        assertTrue(allCandidates.size() >= 50, "Dataset should contain at least 50 rich resources across Sri Lanka.");

        String[] mandatoryDestinations = {"Ella", "Kandy", "Galle", "Nuwara Eliya", "Sigiriya"};
        for (String dest : mandatoryDestinations) {
            List<ResourceOption> destOptions = dataProvider.getCandidateOptions(dest);
            assertFalse(destOptions.isEmpty(), "Destination '" + dest + "' should have candidate resources.");
            assertTrue(destOptions.size() >= 15, "Destination '" + dest + "' should have at least 15 alternatives.");
        }
    }

    @Test
    @DisplayName("2. Each mandatory destination contains alternatives for Transportation, Accommodation, Activities, and Equipment")
    void testFourCategoriesPerDestination() {
        String[] mandatoryDestinations = {"Ella", "Kandy", "Galle", "Nuwara Eliya", "Sigiriya"};

        for (String dest : mandatoryDestinations) {
            List<ResourceOption> destOptions = dataProvider.getCandidateOptions(dest);

            Map<ResourceCategory, Long> categoryCounts = destOptions.stream()
                    .collect(Collectors.groupingBy(ResourceOption::getCategory, Collectors.counting()));

            assertTrue(categoryCounts.getOrDefault(ResourceCategory.TRANSPORTATION, 0L) >= 4,
                    "Destination '" + dest + "' must provide at least 4 transportation alternatives.");

            assertTrue(categoryCounts.getOrDefault(ResourceCategory.ACCOMMODATION, 0L) >= 4,
                    "Destination '" + dest + "' must provide at least 4 accommodation alternatives.");

            assertTrue(categoryCounts.getOrDefault(ResourceCategory.ACTIVITY, 0L) >= 6,
                    "Destination '" + dest + "' must provide at least 6 activity alternatives.");

            assertTrue(categoryCounts.getOrDefault(ResourceCategory.PHYSICAL_ITEM, 0L) >= 7,
                    "Destination '" + dest + "' must provide at least 7 physical equipment alternatives.");
        }
    }

    @Test
    @DisplayName("3. Transportation options provide multi-tier passenger capacities (2, 3, 4, 6 pax)")
    void testTransportationCapacities() {
        String[] mandatoryDestinations = {"Ella", "Kandy", "Galle", "Nuwara Eliya", "Sigiriya"};

        for (String dest : mandatoryDestinations) {
            List<ResourceOption> transports = dataProvider.getCandidateOptions(dest).stream()
                    .filter(o -> o.getCategory() == ResourceCategory.TRANSPORTATION)
                    .toList();

            boolean hasSoloOrCouple = transports.stream().anyMatch(t -> t.getCapacity() != null && t.getCapacity() <= 2);
            boolean hasMediumOrVan = transports.stream().anyMatch(t -> t.getCapacity() != null && t.getCapacity() >= 4);

            assertTrue(hasSoloOrCouple, "Destination '" + dest + "' should have 1-2 passenger transit alternatives.");
            assertTrue(hasMediumOrVan, "Destination '" + dest + "' should have 4+ passenger van/taxi alternatives.");
        }
    }

    @Test
    @DisplayName("4. Accommodation options provide diverse budget, boutique, and family choices")
    void testAccommodationTiering() {
        String[] mandatoryDestinations = {"Ella", "Kandy", "Galle", "Nuwara Eliya", "Sigiriya"};

        for (String dest : mandatoryDestinations) {
            List<ResourceOption> stays = dataProvider.getCandidateOptions(dest).stream()
                    .filter(o -> o.getCategory() == ResourceCategory.ACCOMMODATION)
                    .toList();

            boolean hasBudget = stays.stream().anyMatch(s -> s.getCost() <= 6000.0);
            boolean hasLuxuryOrFamily = stays.stream().anyMatch(s -> s.getCost() >= 18000.0);

            assertTrue(hasBudget, "Destination '" + dest + "' should have budget accommodation <= 6,000 LKR.");
            assertTrue(hasLuxuryOrFamily, "Destination '" + dest + "' should have premium/family accommodation >= 18,000 LKR.");
        }
    }

    @Test
    @DisplayName("5. Physical equipment options provide diverse weight and usefulness trade-offs")
    void testEquipmentKnapsackSuitability() {
        List<ResourceOption> equipment = dataProvider.getCandidateOptions("ALL").stream()
                .filter(o -> o.getCategory() == ResourceCategory.PHYSICAL_ITEM)
                .toList();

        assertTrue(equipment.size() >= 10, "Should have at least 10 physical equipment alternatives.");

        // Check weight variation for 0/1 knapsack
        double minWeight = equipment.stream().mapToDouble(ResourceOption::getWeightKg).min().orElse(0.0);
        double maxWeight = equipment.stream().mapToDouble(ResourceOption::getWeightKg).max().orElse(0.0);

        assertTrue(minWeight <= 0.25, "Equipment should have lightweight gear <= 0.25 kg.");
        assertTrue(maxWeight >= 1.0, "Equipment should have substantial gear >= 1.0 kg (e.g. Backpack, boots).");

        for (ResourceOption eq : equipment) {
            assertTrue(eq.getUsefulness() >= 70.0 && eq.getUsefulness() <= 100.0,
                    "Equipment usefulness should be normalized between 70 and 100.");
            assertTrue(eq.getCost() > 0, "Equipment cost must be positive.");
            assertTrue(eq.isAvailable(), "Equipment must be available.");
        }
    }
}
