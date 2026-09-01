package com.nibm.intelligenttravelmanagementsystem.resourceallocation.service;

import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.ResourceOption;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.repository.ResourceOptionEntity;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.repository.ResourceOptionRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Primary
public class DatabaseResourceDataProvider implements ResourceDataProvider {

    private final ResourceOptionRepository repository;
    private final JsonResourceDataProvider jsonDataProvider;

    public DatabaseResourceDataProvider(ResourceOptionRepository repository, JsonResourceDataProvider jsonDataProvider) {
        this.repository = repository;
        this.jsonDataProvider = jsonDataProvider;
    }

    @Override
    public List<ResourceOption> getCandidateOptions() {
        return getCandidateOptions(null);
    }

    @Override
    public List<ResourceOption> getCandidateOptions(String destination) {
        List<ResourceOptionEntity> entities;
        try {
            if (destination == null || destination.trim().isEmpty()) {
                entities = repository.findByAvailableTrue();
            } else {
                entities = repository.findCandidateOptionsForDestination(destination.trim());
            }
        } catch (Exception e) {
            // Graceful fallback to JSON dataset if database query fails or DB is unreachable
            return jsonDataProvider.getCandidateOptions(destination);
        }

        if (entities == null || entities.isEmpty()) {
            // Fallback to JSON dataset if database is not yet populated
            return jsonDataProvider.getCandidateOptions(destination);
        }

        List<ResourceOption> domainList = new ArrayList<>(entities.size());
        for (ResourceOptionEntity entity : entities) {
            domainList.add(toDomain(entity));
        }

        return domainList;
    }

    public ResourceOption toDomain(ResourceOptionEntity entity) {
        if (entity == null) return null;
        return ResourceOption.builder()
                .id(entity.getId())
                .destination(entity.getDestination())
                .name(entity.getName())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .cost(entity.getCost())
                .durationHours(entity.getDurationHours())
                .weightKg(entity.getWeightKg())
                .usefulness(entity.getUsefulness())
                .available(entity.isAvailable())
                .transportType(entity.getTransportType())
                .capacity(entity.getCapacity())
                .build();
    }

    public ResourceOptionEntity toEntity(ResourceOption domain) {
        if (domain == null) return null;
        return ResourceOptionEntity.builder()
                .id(domain.getId())
                .destination(domain.getDestination())
                .name(domain.getName())
                .description(domain.getDescription())
                .category(domain.getCategory())
                .cost(domain.getCost())
                .durationHours(domain.getDurationHours())
                .weightKg(domain.getWeightKg())
                .usefulness(domain.getUsefulness())
                .available(domain.isAvailable())
                .transportType(domain.getTransportType())
                .capacity(domain.getCapacity())
                .build();
    }
}
