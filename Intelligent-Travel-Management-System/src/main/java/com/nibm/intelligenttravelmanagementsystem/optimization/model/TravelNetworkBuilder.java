package com.nibm.intelligenttravelmanagementsystem.optimization.model;

import com.nibm.intelligenttravelmanagementsystem.shared.db.models.Edge;
import com.nibm.intelligenttravelmanagementsystem.shared.db.models.Node;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TravelNetworkBuilder {
    private TravelNetworkBuilder() {
    }

    public static TravelNetwork buildDefaultNetwork() {
        Map<String, Node> nodes = new LinkedHashMap<>();
        nodes.put("A", new Node("A", 0.0, 0.0));
        nodes.put("B", new Node("B", 1.0, 1.0));
        nodes.put("C", new Node("C", 2.0, 1.5));
        nodes.put("D", new Node("D", 3.0, 2.5));
        nodes.put("E", new Node("E", 4.0, 3.5));
        nodes.put("F", new Node("F", 5.0, 4.5));

        Map<String, List<Edge>> adjacency = new LinkedHashMap<>();
        addEdge(adjacency, "A", "B", 50.0, 40.0, 20.0);
        addEdge(adjacency, "A", "C", 75.0, 60.0, 35.0);
        addEdge(adjacency, "B", "C", 20.0, 15.0, 8.0);
        addEdge(adjacency, "B", "D", 65.0, 45.0, 30.0);
        addEdge(adjacency, "B", "E", 80.0, 65.0, 45.0);
        addEdge(adjacency, "C", "D", 30.0, 20.0, 15.0);
        addEdge(adjacency, "C", "E", 55.0, 35.0, 25.0);
        addEdge(adjacency, "C", "F", 60.0, 48.0, 28.0);
        addEdge(adjacency, "D", "E", 25.0, 18.0, 12.0);
        addEdge(adjacency, "D", "F", 90.0, 70.0, 40.0);
        addEdge(adjacency, "E", "F", 35.0, 25.0, 15.0);

        return new TravelNetwork(nodes, adjacency);
    }

    private static void addEdge(Map<String, List<Edge>> adjacency, String source, String target,
            double distance, double travelTime, double cost) {
        adjacency.computeIfAbsent(source, ignored -> new ArrayList<>())
                .add(new Edge(source, target, distance, travelTime, cost));
        adjacency.computeIfAbsent(target, ignored -> new ArrayList<>());
    }
}
