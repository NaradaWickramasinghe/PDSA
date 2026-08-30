package com.nibm.intelligenttravelmanagementsystem.resourceallocation.service;

import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.ResourceOption;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.repository.ResourceOptionEntity;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.repository.ResourceOptionRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            entities = repository.findByAvailableTrue();
        } catch (Exception e) {
            // Fallback to JSON dataset if DB is unreachable
            return jsonDataProvider.getCandidateOptions(destination);
        }

        if (entities == null || entities.isEmpty()) {
            // Seed initial dataset from JSON into database
            List<ResourceOption> jsonOptions = jsonDataProvider.getCandidateOptions(null);
            for (ResourceOption option : jsonOptions) {
                try {
                    repository.save(toEntity(option));
                } catch (Exception ignored) {}
            }
            return jsonDataProvider.getCandidateOptions(destination);
        }

        List<ResourceOption> domainList = new ArrayList<>();
        for (ResourceOptionEntity entity : entities) {
            domainList.add(toDomain(entity));
        }

        if (destination == null || destination.trim().isEmpty()) {
            return domainList;
        }

        String target = destination.trim().toLowerCase();
        return domainList.stream()
                .filter(opt -> opt.getDestination() == null 
                        || opt.getDestination().equalsIgnoreCase("ALL")
                        || opt.getDestination().toLowerCase().contains(target)
                        || target.contains(opt.getDestination().toLowerCase()))
                .collect(Collectors.toList());
    }

    private ResourceOption toDomain(ResourceOptionEntity entity) {
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

    private ResourceOptionEntity toEntity(ResourceOption domain) {
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
