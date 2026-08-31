package com.nibm.intelligenttravelmanagementsystem.networkanalysis.dto;

public record MstEdgeDTO(
        String fromLocationId,
        String toLocationId,
        double weight
) {}