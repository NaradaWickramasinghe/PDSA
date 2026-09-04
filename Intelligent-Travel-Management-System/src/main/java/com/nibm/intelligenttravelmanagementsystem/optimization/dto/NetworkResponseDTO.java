package com.nibm.intelligenttravelmanagementsystem.optimization.dto;

import com.nibm.intelligenttravelmanagementsystem.optimization.model.TravelEdge;
import com.nibm.intelligenttravelmanagementsystem.optimization.model.TravelNode;

import java.util.Collection;
import java.util.List;

public class NetworkResponseDTO {
    private int nodeCount;
    private int edgeCount;
    private Collection<TravelNode> nodes;
    private List<TravelEdge> edges;

    public NetworkResponseDTO() {}

    public static NetworkResponseDTOBuilder builder() { return new NetworkResponseDTOBuilder(); }

    public static class NetworkResponseDTOBuilder {
        private int nodeCount;
        private int edgeCount;
        private Collection<TravelNode> nodes;
        private List<TravelEdge> edges;

        public NetworkResponseDTOBuilder nodeCount(int count) { this.nodeCount = count; return this; }
        public NetworkResponseDTOBuilder edgeCount(int count) { this.edgeCount = count; return this; }
        public NetworkResponseDTOBuilder nodes(Collection<TravelNode> nodes) { this.nodes = nodes; return this; }
        public NetworkResponseDTOBuilder edges(List<TravelEdge> edges) { this.edges = edges; return this; }

        public NetworkResponseDTO build() {
            NetworkResponseDTO dto = new NetworkResponseDTO();
            dto.nodeCount = this.nodeCount;
            dto.edgeCount = this.edgeCount;
            dto.nodes = this.nodes;
            dto.edges = this.edges;
            return dto;
        }
    }

    public int getNodeCount() { return nodeCount; }
    public int getEdgeCount() { return edgeCount; }
    public Collection<TravelNode> getNodes() { return nodes; }
    public List<TravelEdge> getEdges() { return edges; }
}
