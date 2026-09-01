# Minimum Spanning Tree (MST) Implementation Details

> **Module:** `networkanalysis`  
> **Package:** `com.nibm.intelligenttravelmanagementsystem.networkanalysis`  
> **Purpose:** Find the Minimum Spanning Forest of the travel network to determine the most efficient way to connect all destinations with the minimum total travel cost/distance/time.

---

## Table of Contents

1. [Overview: What is an MST?](#1-overview-what-is-an-mst)
2. [The Algorithm: Prim's Algorithm](#2-the-algorithm-prims-algorithm)
3. [Core Data Structures](#3-core-data-structures)
4. [Implementation Details](#4-implementation-details)
5. [Complexity Analysis](#5-complexity-analysis)
6. [API Endpoint Flow](#6-api-endpoint-flow)

---

## 1. Overview: What is an MST?

In graph theory, a **Minimum Spanning Tree (MST)** is a subset of the edges of a connected, edge-weighted undirected graph that connects all the vertices together, without any cycles, and with the minimum possible total edge weight.

### Relevance to Travel-IDSS
In the context of Sri Lanka's travel network:
- **Vertices (Nodes):** Travel destinations (cities, attractions).
- **Edges:** Travel routes connecting these destinations.
- **Weights:** Distance (km), travel time (mins), or cost (LKR).

Finding the MST answers the question: 
*"What is the most efficient set of routes that connects all destinations such that the total travel distance (or time/cost) is minimized?"*
This is highly useful for planning comprehensive tours (like a cross-country road trip) where the goal is to visit all key regions without redundant travel.

**Note on Forests:** If the travel graph is disconnected (e.g., some nodes cannot be reached from others), the algorithm computes a **Minimum Spanning Forest (MSF)**, which is a collection of MSTs—one for each connected component.

---

## 2. The Algorithm: Prim's Algorithm

To compute the MST, the module uses **Prim's Algorithm**. 

### How Prim's Algorithm Works (Conceptual)
Prim's algorithm is a greedy algorithm that builds the MST one vertex at a time.
1. **Initialize:** Start with an empty tree. Pick any arbitrary node as the starting point.
2. **Grow:** Look at all edges that connect the current tree to vertices not yet in the tree.
3. **Select:** Pick the edge with the minimum weight and add it (and its target vertex) to the tree.
4. **Repeat:** Continue this process until all vertices are included in the tree.

---

## 3. Core Data Structures

To make Prim's algorithm efficient, specific data structures are used in `PrimMstService.java`:

### 1. Priority Queue (Min-Heap)
- **Java Class:** `java.util.PriorityQueue<CandidateEdge>`
- **Role:** Keeps track of the "frontier" edges (edges connecting the current MST to unvisited nodes). It is configured as a min-heap based on edge weight.
- **Why?** It allows us to efficiently extract the minimum-weight edge connecting to an unvisited node in $O(\log V)$ time.

### 2. Visited Sets (HashSets)
- **Java Class:** `java.util.HashSet<String>`
- **`visitedInThisTree`:** Tracks nodes added to the current MST being built. Ensures we don't form cycles.
- **`globallyVisited`:** Tracks nodes across all trees in the forest. Ensures that if the graph is disconnected, we can find unvisited components and start new trees.

### 3. Graph Model
- **Java Class:** `TravelGraph` (Adjacency List)
- **Role:** Provides efficient $O(1)$ access to a node's neighbors via the `neighborsOf(nodeId)` method.

---

## 4. Implementation Details

The implementation is primarily contained within `PrimMstService.java`.

### `computeMinimumSpanningForest(TravelGraph graph)`
This is the entry point. It handles disconnected graphs by checking if all nodes have been visited. If not, it starts a new Prim's run from an unvisited node, building a "Forest" of trees.

```java
Set<String> globallyVisited = new HashSet<>();
List<MstTree> trees = new ArrayList<>();
double totalForestWeight = 0.0;

for (String vertex : graph.allNodeIds()) {
    if (!globallyVisited.contains(vertex)) {
        // Start a new tree for unvisited components
        MstTree tree = runPrimFrom(vertex, graph, globallyVisited);
        trees.add(tree);
        totalForestWeight += tree.totalWeight();
    }
}
return new ForestResult(trees, totalForestWeight);
```

### `runPrimFrom(startVertex, graph, globallyVisited)`
This executes the core Prim's logic for a single connected component.

1. **Initialization:**
   - Add `startVertex` to `visitedInThisTree`.
   - Add all neighbors of `startVertex` to the `PriorityQueue`.
2. **The Greedy Loop:**
   - While the Priority Queue is not empty and the tree doesn't contain all nodes:
     - `poll()` the minimum edge (`CandidateEdge`) from the Priority Queue.
     - Check if the target node (`candidate.toId()`) is already visited. If yes, **skip it** (this prevents cycles).
     - If not visited:
       - Add it to the tree (record the edge, update weight).
       - Mark it as visited.
       - Add all its unvisited neighbors to the Priority Queue.

---

## 5. Complexity Analysis

| Aspect | Complexity | Explanation |
| :--- | :--- | :--- |
| **Time Complexity** | $O(E \log V)$ | We iterate through edges. For each edge, we might add it to the Priority Queue. Inserting or extracting from a Priority Queue takes $O(\log V)$. In the worst case, we process all $E$ edges, leading to $O(E \log V)$. |
| **Space Complexity** | $O(V + E)$ | The `TravelGraph` adjacency list takes $O(V + E)$ space. The Priority Queue can hold at most $O(E)$ edges. The visited sets take $O(V)$ space. |

*Where $V$ = number of vertices (destinations) and $E$ = number of edges (routes).*

This is highly efficient for sparse graphs (like travel networks), ensuring the MST is computed in sub-millisecond times even for hundreds of locations.

---

## 6. API Endpoint Flow

The API request flows through the architecture as follows:

1. **Controller (`NetworkAnalysisController.java`):**
   - Receives `GET /api/network/mst-prim?weight={weightType}`
   - Passes the `weightType` (e.g., "distance_km", "estimated_cost_lkr") to the analysis service.

2. **Orchestrator (`PrimMstAnalysisService.java`):**
   - Calls `GraphBuilderService.buildGraph(weightType)` to construct the in-memory graph from the database using the specified metric.
   - Calls `PrimMstService.computeMinimumSpanningForest(graph)` to run the algorithm.
   - Maps the internal `ForestResult` and `MstTree` records into front-end friendly DTOs (`PrimMstResponseDTO`, `MstTreeDTO`, `MstEdgeDTO`).
   - Tracks computation time.

3. **Response DTO:**
   - Returns a structured JSON response detailing the forest, individual trees, their total weights, and the exact edges that comprise the MST.
