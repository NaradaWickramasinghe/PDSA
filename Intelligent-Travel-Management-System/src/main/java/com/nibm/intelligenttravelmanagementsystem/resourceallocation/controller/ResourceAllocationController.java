package com.nibm.intelligenttravelmanagementsystem.resourceallocation.controller;

import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.ResourceAllocationRequest;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.ResourceAllocationResponse;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.service.ResourceAllocationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resource-allocation")
public class ResourceAllocationController {

    private final ResourceAllocationService allocationService;

    public ResourceAllocationController(ResourceAllocationService allocationService) {
        this.allocationService = allocationService;
    }

    @PostMapping("/allocate")
    public ResponseEntity<ResourceAllocationResponse> allocateResources(@Valid @RequestBody ResourceAllocationRequest request) {
        ResourceAllocationResponse response = allocationService.allocateResources(request);
        return ResponseEntity.ok(response);
    }
}
