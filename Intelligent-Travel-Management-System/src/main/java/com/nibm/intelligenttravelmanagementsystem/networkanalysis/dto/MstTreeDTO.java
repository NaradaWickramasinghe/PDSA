package com.nibm.intelligenttravelmanagementsystem.networkanalysis.dto;

import java.util.List;

public record MstTreeDTO(
        String startedFromLocationId,
        int nodeCount,
        double totalWeight,
        List<MstEdgeDTO> edges
) {}