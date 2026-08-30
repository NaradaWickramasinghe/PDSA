package com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto;

import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.AllocationProblem;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.AllocationResult;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.ResourceCategory;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.ResourceOption;

import java.util.ArrayList;
import java.util.List;

public class ResourceAllocationMapper {

    public static AllocationProblem toProblem(ResourceAllocationRequest request, List<ResourceOption> candidates) {
        if (request == null) {
            return null;
        }

        int travellers = (request.getTravellerCount() != null && request.getTravellerCount() > 0)
                ? request.getTravellerCount()
                : 1;

        int durationDays = (request.getTripDurationDays() != null && request.getTripDurationDays() > 0)
                ? request.getTripDurationDays()
                : (request.getAvailableHours() != null ? Math.max(1, (int) Math.round(request.getAvailableHours() / 6.0)) : 3);

        String destination = (request.getDestination() != null && !request.getDestination().trim().isEmpty())
                ? request.getDestination().trim()
                : "Ella";

        List<ResourceOption> validCandidates = new ArrayList<>();
        if (candidates != null) {
            for (ResourceOption option : candidates) {
                // Filter out vehicle options whose max capacity is strictly smaller than travellerCount
                if (option.getCategory() == ResourceCategory.TRANSPORTATION) {
                    if (option.getCapacity() != null && option.getCapacity() > 0 && option.getCapacity() < travellers) {
                        continue;
                    }
                }

                double adjustedCost = option.getCost();
                Integer adjustedCapacity = option.getCapacity();

                // For accommodation: calculate rooms needed if traveller count exceeds room capacity
                if (option.getCategory() == ResourceCategory.ACCOMMODATION) {
                    int roomCap = (option.getCapacity() != null && option.getCapacity() > 0) ? option.getCapacity() : 2;
                    int rooms = (int) Math.ceil((double) travellers / roomCap);
                    adjustedCost = option.getCost() * rooms;
                    adjustedCapacity = roomCap * rooms;
                } else if (travellers > 1 && option.getCategory() == ResourceCategory.ACTIVITY) {
                    // Scale cost for per-person ticketed activities if travellerCount > 1
                    adjustedCost = option.getCost() * travellers;
                }

                ResourceOption adjustedOption = ResourceOption.builder()
                        .id(option.getId())
                        .destination(option.getDestination())
                        .name(option.getName())
                        .description(option.getDescription())
                        .category(option.getCategory())
                        .cost(adjustedCost)
                        .durationHours(option.getDurationHours())
                        .weightKg(option.getWeightKg())
                        .usefulness(option.getUsefulness())
                        .available(option.isAvailable())
                        .transportType(option.getTransportType())
                        .capacity(adjustedCapacity)
                        .build();

                validCandidates.add(adjustedOption);
            }
        }

        return AllocationProblem.builder()
                .destination(destination)
                .travellerCount(travellers)
                .tripDurationDays(durationDays)
                .totalBudget(request.getTotalBudget() != null ? request.getTotalBudget() : 0.0)
                .emergencyReserve(request.getEmergencyReserve() != null ? request.getEmergencyReserve() : 0.0)
                .maxAvailableHours(request.getAvailableHours() != null ? request.getAvailableHours() : 0.0)
                .maxCarryingCapacityKg(request.getLuggageCapacity() != null ? request.getLuggageCapacity() : 0.0)
                .candidateOptions(validCandidates)
                .build();
    }

    public static SelectedResourceResponse toSelectedResourceResponse(ResourceOption option) {
        if (option == null) {
            return null;
        }

        return SelectedResourceResponse.builder()
                .id(option.getId())
                .destination(option.getDestination())
                .name(option.getName())
                .description(option.getDescription())
                .category(option.getCategory() != null ? option.getCategory().name() : null)
                .cost(option.getCost())
                .durationHours(option.getDurationHours())
                .weightKg(option.getWeightKg())
                .usefulness(option.getUsefulness())
                .transportType(option.getTransportType())
                .capacity(option.getCapacity())
                .build();
    }

    public static ResourceAllocationResponse toResponse(AllocationResult result) {
        return toResponse(null, result);
    }

    public static ResourceAllocationResponse toResponse(ResourceAllocationRequest request, AllocationResult result) {
        if (result == null) {
            return null;
        }

        List<SelectedResourceResponse> selectedDtos = new ArrayList<>();
        List<SelectedResourceResponse> transportationList = new ArrayList<>();
        List<SelectedResourceResponse> accommodationList = new ArrayList<>();
        List<SelectedResourceResponse> activitiesList = new ArrayList<>();
        List<SelectedResourceResponse> equipmentList = new ArrayList<>();

        double transportationTime = 0.0;
        double activityTime = 0.0;

        if (result.getSelectedResources() != null) {
            for (ResourceOption option : result.getSelectedResources()) {
                SelectedResourceResponse dto = toSelectedResourceResponse(option);
                selectedDtos.add(dto);

                if ("TRANSPORTATION".equalsIgnoreCase(dto.getCategory())) {
                    transportationList.add(dto);
                    transportationTime += dto.getDurationHours();
                } else if ("ACCOMMODATION".equalsIgnoreCase(dto.getCategory())) {
                    accommodationList.add(dto);
                } else if ("ACTIVITY".equalsIgnoreCase(dto.getCategory())) {
                    activitiesList.add(dto);
                    activityTime += dto.getDurationHours();
                } else if ("PHYSICAL_ITEM".equalsIgnoreCase(dto.getCategory())) {
                    equipmentList.add(dto);
                }
            }
        }

        String dest = (request != null && request.getDestination() != null && !request.getDestination().trim().isEmpty())
                ? request.getDestination() 
                : "Sri Lanka";
        double totalBudgetVal = (request != null && request.getTotalBudget() != null) ? request.getTotalBudget() : result.getTotalCost() + result.getRemainingBudget();
        double emergencyReserveVal = (request != null && request.getEmergencyReserve() != null) ? request.getEmergencyReserve() : 0.0;
        double availableHoursVal = (request != null && request.getAvailableHours() != null) ? request.getAvailableHours() : result.getTotalTime() + result.getRemainingTime();
        double luggageCapacityVal = (request != null && request.getLuggageCapacity() != null) ? request.getLuggageCapacity() : result.getTotalWeight() + result.getRemainingCapacity();
        int travellersVal = (request != null && request.getTravellerCount() != null) ? request.getTravellerCount() : 1;

        int durationDaysVal = (request != null && request.getTripDurationDays() != null && request.getTripDurationDays() > 0)
                ? request.getTripDurationDays()
                : (int) Math.max(1, Math.round(availableHoursVal / 6.0));

        TripInformationSummary tripSummary = TripInformationSummary.builder()
                .destination(dest)
                .durationHours(availableHoursVal)
                .travellerCount(travellersVal)
                .tripDurationDays(durationDaysVal)
                .build();

        FinancialSummary financialSummary = FinancialSummary.builder()
                .totalBudget(totalBudgetVal)
                .emergencyReserve(emergencyReserveVal)
                .availableAllocationBudget(Math.max(0.0, totalBudgetVal - emergencyReserveVal))
                .totalAllocatedCost(result.getTotalCost())
                .remainingBudget(result.getRemainingBudget())
                .build();

        TimeSummary timeSummary = TimeSummary.builder()
                .totalAvailableTime(availableHoursVal)
                .transportationTime(transportationTime)
                .activityTime(activityTime)
                .totalTimeUsed(result.getTotalTime())
                .remainingTime(result.getRemainingTime())
                .build();

        PhysicalResourceSummary physicalSummary = PhysicalResourceSummary.builder()
                .carryingCapacity(luggageCapacityVal)
                .equipmentWeightUsed(result.getTotalWeight())
                .remainingCapacity(result.getRemainingCapacity())
                .build();

        return ResourceAllocationResponse.builder()
                .algorithmUsed(result.getAlgorithmName())
                .feasible(result.isFeasible())
                .selectedResources(selectedDtos)
                .totalCost(result.getTotalCost())
                .remainingBudget(result.getRemainingBudget())
                .totalTimeUsed(result.getTotalTime())
                .remainingTime(result.getRemainingTime())
                .totalWeight(result.getTotalWeight())
                .remainingCapacity(result.getRemainingCapacity())
                .overallScore(result.getOverallScore())
                .executionTimeMs(result.getExecutionTimeMs())
                .statusMessage(result.getStatusMessage())
                .tripInformation(tripSummary)
                .financialSummary(financialSummary)
                .timeSummary(timeSummary)
                .physicalResourceSummary(physicalSummary)
                .selectedTransportation(transportationList)
                .selectedAccommodation(accommodationList)
                .selectedActivities(activitiesList)
                .selectedEquipment(equipmentList)
                .build();
    }
}
