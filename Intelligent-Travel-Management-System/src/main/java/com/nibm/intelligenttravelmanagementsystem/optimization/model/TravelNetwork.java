package com.nibm.intelligenttravelmanagementsystem.optimization.model;

import com.nibm.intelligenttravelmanagementsystem.shared.db.models.Edge;
import com.nibm.intelligenttravelmanagementsystem.shared.db.models.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TravelNetwork {
    private final Map<String, Node> nodes;
    private final Map<String, List<Edge>> adjacency;

    public TravelNetwork(Map<String, Node> nodes, Map<String, List<Edge>> adjacency) {
        this.nodes = new HashMap<>(nodes);
        this.adjacency = new HashMap<>();
        adjacency.forEach((key, value) -> this.adjacency.put(key, new ArrayList<>(value)));
    }

    public Map<String, Node> getNodes() {
        return nodes;
    }

    public Map<String, List<Edge>> getAdjacency() {
        return adjacency;
    }

    public boolean containsNode(String nodeName) {
        return nodes.containsKey(nodeName);
    }

    public List<Edge> getOutgoing(String nodeName) {
        return adjacency.getOrDefault(nodeName, List.of());
    }
}
