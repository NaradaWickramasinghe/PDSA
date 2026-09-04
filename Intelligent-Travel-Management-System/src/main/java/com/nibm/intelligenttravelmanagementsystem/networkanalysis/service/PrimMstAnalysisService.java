package com.nibm.intelligenttravelmanagementsystem.networkanalysis.service;

import com.nibm.intelligenttravelmanagementsystem.networkanalysis.dto.*;
import com.nibm.intelligenttravelmanagementsystem.networkanalysis.model.TravelGraph;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrimMstAnalysisService {

    private final GraphBuilderService graphBuilderService;
    private final PrimMstService primMstService;

    public PrimMstResponseDTO analyzeMst(String weightType, String startNodeId) {
        long start = System.nanoTime();

        // Pass weightType (e.g., "distance_km") to match GraphBuilderService signature
        TravelGraph graph = graphBuilderService.buildGraph(weightType);
        var forest = primMstService.computeMinimumSpanningForest(graph, startNodeId);

        List<MstTreeDTO> treeDtos = forest.trees().stream()
                .map(tree -> new MstTreeDTO(
                        tree.startNodeId(),
                        tree.nodesInTree().size(),
                        tree.totalWeight(),
                        tree.edges().stream()
                                .map(e -> new MstEdgeDTO(e.fromId(), e.toId(), e.weight()))
                                .toList()
                ))
                .toList();

        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        return new PrimMstResponseDTO(
                graph.nodeCount(),
                forest.trees().size(),
                forest.totalWeight(),
                elapsedMs,
                forest.trees().size() == 1,
                treeDtos
        );
    }

    // Default overload if called without arguments
    public PrimMstResponseDTO analyzeMst(String weightType) {
        return analyzeMst(weightType, null);
    }

    public PrimMstResponseDTO analyzeMst() {
        return analyzeMst("distance_km", null);
    }
}