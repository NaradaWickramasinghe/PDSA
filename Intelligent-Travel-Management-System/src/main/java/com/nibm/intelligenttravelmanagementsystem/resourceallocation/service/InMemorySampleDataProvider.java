package com.nibm.intelligenttravelmanagementsystem.resourceallocation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.ResourceOption;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InMemorySampleDataProvider implements ResourceDataProvider {

    private final JsonResourceDataProvider jsonDataProvider;

    public InMemorySampleDataProvider() {
        this(new JsonResourceDataProvider(new ObjectMapper()));
    }

    public InMemorySampleDataProvider(JsonResourceDataProvider jsonDataProvider) {
        this.jsonDataProvider = jsonDataProvider;
    }

    @Override
    public List<ResourceOption> getCandidateOptions() {
        return jsonDataProvider.getCandidateOptions(null);
    }

    @Override
    public List<ResourceOption> getCandidateOptions(String destination) {
        return jsonDataProvider.getCandidateOptions(destination);
    }
}
