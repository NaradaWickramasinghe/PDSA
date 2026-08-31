package com.nibm.intelligenttravelmanagementsystem.networkanalysis.dto;

import java.util.List;

public record PrimMstResponseDTO(
        int totalNodeCount,
        int totalTreeCount,
        double totalForestWeight,
        long computationTimeMs,
        boolean graphWasFullyConnected,
        List<MstTreeDTO> trees
) {}