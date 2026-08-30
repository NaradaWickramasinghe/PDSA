package com.nibm.intelligenttravelmanagementsystem.resourceallocation.service;

import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.ResourceAllocationMapper;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.ResourceAllocationRequest;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.ResourceAllocationResponse;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.AllocationProblem;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.AllocationResult;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.ResourceOption;
import com.nibm.intelligenttravelmanagementsystem.shared.exception.InvalidAllocationRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ResourceAllocationService {

    private final Map<String, AllocationAlgorithm> algorithmRegistry = new HashMap<>();
    private final ResourceDataProvider dataProvider;

    public ResourceAllocationService(List<AllocationAlgorithm> algorithms) {
        this(algorithms, null);
    }

    @Autowired
    public ResourceAllocationService(List<AllocationAlgorithm> algorithms, ResourceDataProvider dataProvider) {
        if (algorithms != null) {
            for (AllocationAlgorithm algo : algorithms) {
                algorithmRegistry.put(algo.getAlgorithmName().toUpperCase(), algo);
            }
        }
        // Ensure PIPELINE algorithm is always registered
        if (!algorithmRegistry.containsKey("PIPELINE")) {
            GreedyAllocationService greedy = (algorithmRegistry.get("GREEDY") instanceof GreedyAllocationService g) ? g : new GreedyAllocationService();
            DynamicProgrammingAllocationService dp = (algorithmRegistry.get("DYNAMIC_PROGRAMMING") instanceof DynamicProgrammingAllocationService d) ? d : new DynamicProgrammingAllocationService();
            GeneticAllocationService genetic = (algorithmRegistry.get("GENETIC") instanceof GeneticAllocationService ge) ? ge : new GeneticAllocationService();
            algorithmRegistry.put("PIPELINE", new PipelineAllocationService(greedy, dp, genetic));
        }
        this.dataProvider = dataProvider;
    }

    /**
     * Overloaded method accepting DTO request directly (used by Controller).
     */
    public ResourceAllocationResponse allocateResources(ResourceAllocationRequest request) {
        validateRequest(request);

        String targetAlgorithm = (request.getSelectedAlgorithm() == null || request.getSelectedAlgorithm().trim().isEmpty())
                ? "PIPELINE"
                : request.getSelectedAlgorithm().trim().toUpperCase();

        String destination = (request.getDestination() != null && !request.getDestination().trim().isEmpty())
                ? request.getDestination().trim()
                : "Ella";

        List<ResourceOption> candidates = (dataProvider != null) 
                ? dataProvider.getCandidateOptions(destination) 
                : List.of();

        if (candidates.isEmpty()) {
            return ResourceAllocationResponse.builder()
                    .algorithmUsed(targetAlgorithm)
                    .feasible(false)
                    .statusMessage("INFEASIBLE: Resource dataset is empty for destination '" + destination + "'.")
                    .build();
        }

        AllocationProblem problem = ResourceAllocationMapper.toProblem(request, candidates);
        AllocationResult result = allocateResources(problem, targetAlgorithm);
        return ResourceAllocationMapper.toResponse(request, result);
    }

    /**
     * Core strategy selection & execution method.
     */
    public AllocationResult allocateResources(AllocationProblem problem, String algorithmType) {
        if (problem == null) {
            throw new InvalidAllocationRequestException("Allocation problem context cannot be null.");
        }

        String targetAlgorithm = (algorithmType == null || algorithmType.trim().isEmpty()) 
                ? "PIPELINE" 
                : algorithmType.trim().toUpperCase();

        AllocationAlgorithm selectedAlgorithm = algorithmRegistry.get(targetAlgorithm);

        if (selectedAlgorithm == null) {
            throw new InvalidAllocationRequestException(
                    "Invalid algorithm name: '" + algorithmType + "'. Supported algorithms are PIPELINE, GREEDY, DYNAMIC_PROGRAMMING, GENETIC."
            );
        }

        return selectedAlgorithm.allocate(problem);
    }

    /**
     * Validates domain requirements for incoming ResourceAllocationRequest
     */
    private void validateRequest(ResourceAllocationRequest request) {
        if (request == null) {
            throw new InvalidAllocationRequestException("Allocation request payload cannot be null.");
        }
        if (request.getTotalBudget() == null || request.getTotalBudget() < 0) {
            throw new InvalidAllocationRequestException("Total budget must be greater than or equal to 0.");
        }
        if (request.getEmergencyReserve() == null || request.getEmergencyReserve() < 0) {
            throw new InvalidAllocationRequestException("Emergency reserve must be greater than or equal to 0.");
        }
        if (request.getEmergencyReserve() > request.getTotalBudget()) {
            throw new InvalidAllocationRequestException("Emergency reserve cannot exceed total budget.");
        }
        if (request.getAvailableHours() == null || request.getAvailableHours() <= 0) {
            throw new InvalidAllocationRequestException("Available travel hours must be greater than 0.");
        }
        if (request.getLuggageCapacity() == null || request.getLuggageCapacity() < 0) {
            throw new InvalidAllocationRequestException("Luggage capacity must be greater than or equal to 0.");
        }
        if (request.getSelectedAlgorithm() != null && !request.getSelectedAlgorithm().trim().isEmpty()) {
            String algoUpper = request.getSelectedAlgorithm().trim().toUpperCase();
            if (!algorithmRegistry.containsKey(algoUpper)) {
                throw new InvalidAllocationRequestException(
                        "Invalid algorithm name: '" + request.getSelectedAlgorithm() + "'. Supported algorithms are PIPELINE, GREEDY, DYNAMIC_PROGRAMMING, GENETIC."
                );
            }
        }
    }
}
