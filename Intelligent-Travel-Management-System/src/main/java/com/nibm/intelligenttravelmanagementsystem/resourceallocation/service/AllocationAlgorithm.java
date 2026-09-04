package com.nibm.intelligenttravelmanagementsystem.resourceallocation.service;

import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.AllocationProblem;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.AllocationResult;

public interface AllocationAlgorithm {
    AllocationResult allocate(AllocationProblem problem);
    String getAlgorithmName();
}
