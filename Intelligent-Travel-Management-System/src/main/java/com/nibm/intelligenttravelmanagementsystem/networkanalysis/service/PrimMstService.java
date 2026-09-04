package com.nibm.intelligenttravelmanagementsystem.networkanalysis.service;

import com.nibm.intelligenttravelmanagementsystem.networkanalysis.model.TravelGraph;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PrimMstService {

    public record MstEdge(String fromId, String toId, double weight) {}
    public record MstTree(String startNodeId, List<MstEdge> edges, Set<String> nodesInTree, double totalWeight) {}
    public record ForestResult(List<MstTree> trees, double totalWeight) {}

    public ForestResult computeMinimumSpanningForest(TravelGraph graph) {
        return computeMinimumSpanningForest(graph, null);
    }

    public ForestResult computeMinimumSpanningForest(TravelGraph graph, String preferredStartNodeId) {
        Set<String> globallyVisited = new HashSet<>();
        List<MstTree> trees = new ArrayList<>();
        double totalForestWeight = 0.0;

        if (preferredStartNodeId != null && !preferredStartNodeId.isBlank() && graph.allNodeIds().contains(preferredStartNodeId)) {
            MstTree tree = runPrimFrom(preferredStartNodeId, graph, globallyVisited);
            trees.add(tree);
            totalForestWeight += tree.totalWeight();
        }

        for (String vertex : graph.allNodeIds()) {
            if (!globallyVisited.contains(vertex)) {
                MstTree tree = runPrimFrom(vertex, graph, globallyVisited);
                trees.add(tree);
                totalForestWeight += tree.totalWeight();
            }
        }
        return new ForestResult(trees, totalForestWeight);
    }

    private MstTree runPrimFrom(String startVertex, TravelGraph graph, Set<String> globallyVisited) {
        Set<String> visitedInThisTree = new HashSet<>();
        List<MstEdge> mstEdges = new ArrayList<>();
        double treeWeight = 0.0;

        PriorityQueue<CandidateEdge> heap = new PriorityQueue<>(
                Comparator.comparingDouble(CandidateEdge::weight)
        );

        visitedInThisTree.add(startVertex);
        globallyVisited.add(startVertex);
        addFrontierEdges(startVertex, graph, visitedInThisTree, heap);

        while (!heap.isEmpty() && visitedInThisTree.size() < graph.nodeCount()) {
            CandidateEdge candidate = heap.poll();

            if (visitedInThisTree.contains(candidate.toId())) {
                continue;
            }

            visitedInThisTree.add(candidate.toId());
            globallyVisited.add(candidate.toId());
            mstEdges.add(new MstEdge(candidate.fromId(), candidate.toId(), candidate.weight()));
            treeWeight += candidate.weight();

            addFrontierEdges(candidate.toId(), graph, visitedInThisTree, heap);
        }

        return new MstTree(startVertex, mstEdges, visitedInThisTree, treeWeight);
    }

    private void addFrontierEdges(String nodeId, TravelGraph graph, Set<String> visitedInThisTree, PriorityQueue<CandidateEdge> heap) {
        // Changed loop type to TravelGraph.GraphEdge to fix the build error
        for (TravelGraph.GraphEdge edge : graph.neighborsOf(nodeId)) {
            if (!visitedInThisTree.contains(edge.targetId())) {
                heap.add(new CandidateEdge(nodeId, edge.targetId(), edge.weight()));
            }
        }
    }

    private record CandidateEdge(String fromId, String toId, double weight) {}
}