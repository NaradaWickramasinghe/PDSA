package com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceAllocationRequest {

    private String destination; // e.g., Ella, Galle, Kandy, Nuwara Eliya, Sigiriya

    @NotNull(message = "Total budget is required")
    @Min(value = 0, message = "Total budget must be greater than or equal to 0")
    private Double totalBudget;

    @NotNull(message = "Emergency reserve is required")
    @Min(value = 0, message = "Emergency reserve must be greater than or equal to 0")
    private Double emergencyReserve;

    @NotNull(message = "Available hours is required")
    @Min(value = 0, message = "Available hours must be greater than or equal to 0")
    private Double availableHours;

    @NotNull(message = "Luggage capacity is required")
    @Min(value = 0, message = "Luggage capacity must be greater than or equal to 0")
    private Double luggageCapacity;

    @Min(value = 1, message = "Traveller count must be at least 1")
    private Integer travellerCount;

    @Min(value = 1, message = "Trip duration must be at least 1 day")
    private Integer tripDurationDays;

    private String selectedAlgorithm; // PIPELINE (default), GREEDY, DYNAMIC_PROGRAMMING, GENETIC

    public boolean isEmergencyReserveValid() {
        return totalBudget != null && emergencyReserve != null && emergencyReserve <= totalBudget;
    }
}
