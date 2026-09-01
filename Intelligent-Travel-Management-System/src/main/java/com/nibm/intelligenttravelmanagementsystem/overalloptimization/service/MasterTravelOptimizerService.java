package com.nibm.intelligenttravelmanagementsystem.overalloptimization.service;

import com.nibm.intelligenttravelmanagementsystem.overalloptimization.dto.OverallTravelPlanRequest;
import com.nibm.intelligenttravelmanagementsystem.overalloptimization.dto.OverallTravelPlanResponse;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.ResourceAllocationResponse;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.service.ResourceAllocationService;
import org.springframework.stereotype.Service;

@Service
public class MasterTravelOptimizerService {

    private final ResourceAllocationService resourceAllocationService;

    public MasterTravelOptimizerService(ResourceAllocationService resourceAllocationService) {
        this.resourceAllocationService = resourceAllocationService;
    }

    /**
     * Module 5 consumes Module 2 resource allocation result as one input to overall travel optimization.
     */
    public OverallTravelPlanResponse optimizeTravelPlan(OverallTravelPlanRequest planRequest) {
        if (planRequest == null || planRequest.getResourceAllocationRequest() == null) {
            return OverallTravelPlanResponse.builder()
                    .planName(planRequest != null ? planRequest.getPlanName() : "Unknown Plan")
                    .planOptimized(false)
                    .optimizationSummary("Failed: Missing resource allocation request input.")
                    .build();
        }

        // Module 5 invokes Module 2 service to obtain optimal resource allocation
        ResourceAllocationResponse allocationResponse = resourceAllocationService.allocateResources(
                planRequest.getResourceAllocationRequest()
        );

        boolean isSuccess = allocationResponse != null && allocationResponse.isFeasible();

        String summary = isSuccess
                ? "Overall travel plan successfully optimized incorporating Module 2 allocated resources."
                : "Travel plan optimization partially constrained: Resource allocation was infeasible.";

        return OverallTravelPlanResponse.builder()
                .planName(planRequest.getPlanName())
                .planOptimized(isSuccess)
                .allocatedResources(allocationResponse)
                .totalBudgetUsed(allocationResponse != null ? allocationResponse.getTotalCost() : 0.0)
                .totalUtilityScore(allocationResponse != null ? allocationResponse.getOverallScore() : 0.0)
                .optimizationSummary(summary)
                .build();
    }
}
