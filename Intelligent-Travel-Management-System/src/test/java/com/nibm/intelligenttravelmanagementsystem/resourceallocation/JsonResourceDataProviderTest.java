package com.nibm.intelligenttravelmanagementsystem.resourceallocation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.ResourceOption;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.service.JsonResourceDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonResourceDataProviderTest {

    private JsonResourceDataProvider jsonResourceDataProvider;

    @BeforeEach
    void setUp() {
        jsonResourceDataProvider = new JsonResourceDataProvider(new ObjectMapper());
    }

    @Test
    @DisplayName("1. Should load all available resources when destination is null or empty")
    void testLoadAllResources() {
        List<ResourceOption> options = jsonResourceDataProvider.getCandidateOptions();
        assertNotNull(options);
        assertFalse(options.isEmpty(), "JSON dataset should contain candidate resources.");
        assertTrue(options.size() >= 20, "Should load full dataset across destinations.");
    }

    @Test
    @DisplayName("2. Should filter resources dynamically for Ella destination")
    void testFilterForElla() {
        List<ResourceOption> ellaOptions = jsonResourceDataProvider.getCandidateOptions("Ella");
        assertNotNull(ellaOptions);
        assertFalse(ellaOptions.isEmpty());
        assertTrue(ellaOptions.stream().anyMatch(opt -> opt.getName().contains("Ella")));
    }

    @Test
    @DisplayName("3. Should filter resources dynamically for Galle destination")
    void testFilterForGalle() {
        List<ResourceOption> galleOptions = jsonResourceDataProvider.getCandidateOptions("Galle");
        assertNotNull(galleOptions);
        assertFalse(galleOptions.isEmpty());
        assertTrue(galleOptions.stream().anyMatch(opt -> opt.getName().contains("Galle")));
    }

    @Test
    @DisplayName("4. Should filter resources dynamically for Kandy destination")
    void testFilterForKandy() {
        List<ResourceOption> kandyOptions = jsonResourceDataProvider.getCandidateOptions("Kandy");
        assertNotNull(kandyOptions);
        assertFalse(kandyOptions.isEmpty());
        assertTrue(kandyOptions.stream().anyMatch(opt -> opt.getName().contains("Kandy")));
    }

    @Test
    @DisplayName("5. Should filter resources dynamically for Nuwara Eliya destination")
    void testFilterForNuwaraEliya() {
        List<ResourceOption> nuwaraOptions = jsonResourceDataProvider.getCandidateOptions("Nuwara Eliya");
        assertNotNull(nuwaraOptions);
        assertFalse(nuwaraOptions.isEmpty());
        assertTrue(nuwaraOptions.stream().anyMatch(opt -> opt.getName().contains("Nuwara Eliya") || opt.getName().contains("Pedro")));
    }

    @Test
    @DisplayName("6. Should filter resources dynamically for Sigiriya destination")
    void testFilterForSigiriya() {
        List<ResourceOption> sigiriyaOptions = jsonResourceDataProvider.getCandidateOptions("Sigiriya");
        assertNotNull(sigiriyaOptions);
        assertFalse(sigiriyaOptions.isEmpty());
        assertTrue(sigiriyaOptions.stream().anyMatch(opt -> opt.getName().contains("Sigiriya")));
    }
}
