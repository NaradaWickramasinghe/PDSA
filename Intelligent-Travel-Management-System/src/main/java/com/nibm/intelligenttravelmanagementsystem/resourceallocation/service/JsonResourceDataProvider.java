package com.nibm.intelligenttravelmanagementsystem.resourceallocation.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.ResourceOption;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JsonResourceDataProvider implements ResourceDataProvider {

    private final ObjectMapper objectMapper;
    private List<ResourceOption> cachedOptions;

    public JsonResourceDataProvider() {
        this(new ObjectMapper());
    }

    public JsonResourceDataProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    @Override
    public List<ResourceOption> getCandidateOptions() {
        return getCandidateOptions(null);
    }

    @Override
    public List<ResourceOption> getCandidateOptions(String destination) {
        List<ResourceOption> allOptions = loadAllOptions();
        if (allOptions.isEmpty()) {
            return Collections.emptyList();
        }

        if (destination == null || destination.trim().isEmpty()) {
            return allOptions.stream()
                    .filter(ResourceOption::isAvailable)
                    .collect(Collectors.toList());
        }

        String targetDest = destination.trim().toLowerCase();

        return allOptions.stream()
                .filter(ResourceOption::isAvailable)
                .filter(opt -> opt.getDestination() == null 
                        || opt.getDestination().equalsIgnoreCase("ALL")
                        || opt.getDestination().toLowerCase().contains(targetDest)
                        || targetDest.contains(opt.getDestination().toLowerCase()))
                .collect(Collectors.toList());
    }

    private synchronized List<ResourceOption> loadAllOptions() {
        if (cachedOptions != null) {
            return cachedOptions;
        }

        try {
            ClassPathResource resource = new ClassPathResource("data/travel-resources.json");
            if (!resource.exists()) {
                return Collections.emptyList();
            }

            try (InputStream inputStream = resource.getInputStream()) {
                cachedOptions = objectMapper.readValue(inputStream, new TypeReference<List<ResourceOption>>() {});
                return cachedOptions;
            }
        } catch (Exception e) {
            System.err.println("Warning: Unable to parse data/travel-resources.json: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
