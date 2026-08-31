# Network Analysis Module — Full Implementation Documentation

> **Module:** `networkanalysis`  
> **Package:** `com.nibm.intelligenttravelmanagementsystem.networkanalysis`  
> **Purpose:** Identify the most strategically important destinations in Sri Lanka's travel network using graph centrality analysis.

---

## Table of Contents

1. [Module Overview](#1-module-overview)
2. [Architecture & Package Structure](#2-architecture--package-structure)
3. [Data Model — Database Entities](#3-data-model--database-entities)
4. [Core Data Structure — TravelGraph (Adjacency List)](#4-core-data-structure--travelgraph-adjacency-list)
5. [Algorithm #1 — Dijkstra's Shortest Path (SSSP Subroutine)](#5-algorithm-1--dijkstras-shortest-path-sssp-subroutine)
6. [Algorithm #2 — Brandes' Betweenness Centrality Algorithm](#6-algorithm-2--brandes-betweenness-centrality-algorithm)
7. [Centrality Metrics Explained](#7-centrality-metrics-explained)
8. [Service Layer — How Everything Connects](#8-service-layer--how-everything-connects)
9. [REST API — Controller & Endpoints](#9-rest-api--controller--endpoints)
10. [Error Handling Strategy](#10-error-handling-strategy)
11. [Data Transfer Objects (DTOs)](#11-data-transfer-objects-dtos)
12. [Complexity Summary](#12-complexity-summary)
13. [End-to-End Request Flow](#13-end-to-end-request-flow)
14. [Data Structures Cheat Sheet](#14-data-structures-cheat-sheet)

---

## 1. Module Overview

### What Does This Module Do?

The Network Analysis module models Sri Lanka's travel destinations as a **weighted, undirected graph** and computes **centrality metrics** to answer two key questions:

| Question | Centrality Metric | Real-World Meaning |
|----------|-------------------|--------------------|
| *"Which town is the best gateway/transit hub?"* | **Betweenness Centrality** | A town with high betweenness sits on many shortest paths between other towns — it's a natural transfer point. |
| *"Which town is the best base to stay?"* | **Closeness Centrality** | A town with high closeness can reach all other towns quickly — it minimizes total travel effort. |

### Why Graph-Based Analysis?

A travel network is naturally a graph:
- **Nodes** = Destinations (cities, towns, attractions)  
- **Edges** = Travel routes between them  
- **Edge Weights** = Distance (km), travel time (minutes), or cost (LKR)

By running centrality algorithms on this graph, the system provides **data-driven strategic insights** rather than relying on intuition.

---

## 2. Architecture & Package Structure

```
networkanalysis/
├── controller/                          ← REST API Layer
│   ├── NetworkAnalysisController.java   ← 4 REST endpoints
│   ├── NetworkExceptionHandler.java     ← Centralized error handling
│   └── LocationNotFoundException.java   ← Custom 404 exception
├── dto/                                 ← Data Transfer Objects
│   ├── NetworkAnalysisResponseDTO.java  ← Full analysis response shape
│   ├── CentralityScoreDTO.java          ← Per-destination score
│   └── ApiErrorDTO.java                 ← Error response shape
├── model/                               ← In-Memory Graph Structure
│   └── TravelGraph.java                 ← Adjacency list graph
├── repository/                          ← Database Access (JPA)
│   ├── NetworkNodeRepository.java       ← Access to 'nodes' table
│   └── NetworkEdgeRepository.java       ← Access to 'edges' table
└── service/                             ← Business Logic
    ├── NetworkAnalysisService.java      ← Orchestrator (entry point)
    ├── GraphBuilderService.java         ← DB → Graph converter
    └── CentralityService.java           ← Brandes' algorithm implementation
```

### Layered Architecture Pattern

```
┌─────────────────────────────────────────────────────────────┐
│                    REST Controller Layer                     │
│              NetworkAnalysisController.java                  │
│         (Receives HTTP requests, returns JSON)               │
└──────────────────────────┬──────────────────────────────────┘
                           │ calls
┌──────────────────────────▼──────────────────────────────────┐
│                  Orchestration Service Layer                  │
│              NetworkAnalysisService.java                      │
│      (Coordinates graph building + analysis + DTO assembly)  │
└──────────┬──────────────────────────────────┬───────────────┘
           │ calls                            │ calls
┌──────────▼──────────────┐    ┌──────────────▼──────────────┐
│   GraphBuilderService   │    │     CentralityService        │
│  (DB rows → TravelGraph)│    │  (Brandes' Algorithm)        │
└──────────┬──────────────┘    └─────────────────────────────┘
           │ reads from
┌──────────▼──────────────────────────────────────────────────┐
│                   Repository Layer (JPA)                      │
│        NetworkNodeRepository + NetworkEdgeRepository          │
│              (Spring Data JPA auto-generated)                 │
└──────────┬──────────────────────────────────────────────────┘
           │ queries
┌──────────▼──────────────────────────────────────────────────┐
│                     MySQL Database                            │
│                 Tables: nodes, edges                          │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. Data Model — Database Entities

### Node Entity (`nodes` table)

Represents a travel destination (city, town, tourist attraction).

| Column      | Type         | Description                          |
|-------------|--------------|--------------------------------------|
| `node_id`   | VARCHAR(20)  | **Primary Key** — e.g., "N001"       |
| `name`      | VARCHAR(100) | Display name — e.g., "Colombo"       |
| `node_type` | VARCHAR(30)  | Category — e.g., "city", "attraction"|
| `province`  | VARCHAR(50)  | Province name                        |
| `district`  | VARCHAR(50)  | District name                        |
| `latitude`  | DOUBLE       | GPS latitude for map visualization   |
| `longitude` | DOUBLE       | GPS longitude for map visualization  |
| `description` | TEXT       | Optional description                 |

**Java Class:** `com.nibm.intelligenttravelmanagementsystem.shared.db.models.Node`

### Edge Entity (`edges` table)

Represents a travel route between two destinations.

| Column                 | Type        | Description                               |
|------------------------|-------------|-------------------------------------------|
| `edge_id`              | VARCHAR(20) | **Primary Key** — e.g., "E001"            |
| `source`               | VARCHAR(20) | Source node_id (FK → nodes)               |
| `destination`          | VARCHAR(20) | Destination node_id (FK → nodes)          |
| `distance_km`          | DOUBLE      | Geographic distance in kilometers          |
| `travel_time_minutes`  | INTEGER     | Estimated travel time in minutes           |
| `estimated_cost_lkr`   | INTEGER     | Estimated travel cost in Sri Lankan Rupees |
| `road_quality`         | SMALLINT    | Road quality rating (1-5)                  |
| `traffic_level`        | SMALLINT    | Traffic congestion level (1-5)             |
| `transport_mode`       | VARCHAR(20) | Mode of transport (bus, train, etc.)       |
| `accessibility`        | SMALLINT    | Accessibility rating (1-5)                 |
| `risk_level`           | SMALLINT    | Risk level rating (1-5)                    |

**Java Class:** `com.nibm.intelligenttravelmanagementsystem.shared.db.models.Edge`

---

## 4. Core Data Structure — TravelGraph (Adjacency List)

**File:** `model/TravelGraph.java`

### What is it?

`TravelGraph` is a custom **in-memory graph** data structure that stores the travel network using an **adjacency list** representation. It is NOT a database entity — it exists only in memory during computation.

### Why Adjacency List (Not Adjacency Matrix)?

| Criteria | Adjacency List | Adjacency Matrix |
|----------|---------------|-----------------|
| **Space** | O(V + E) | O(V²) |
| **Iterate neighbors** | O(degree) ✅ | O(V) ❌ |
| **Check edge exists** | O(degree) | O(1) |
| **Best for** | Sparse graphs ✅ | Dense graphs |

**Our travel network is sparse**: a city typically connects to 3–8 neighbors, not to every other city. With V = 500 nodes:
- Adjacency List: ~500 + ~1,500 = **2,000 entries**
- Adjacency Matrix: 500 × 500 = **250,000 entries**

The adjacency list is **125× more memory efficient** for our use case.

### Internal Structure

```java
public class TravelGraph {

    // THE CORE DATA STRUCTURE: Adjacency List
    // Key   = node ID (String, e.g., "N001")
    // Value = list of edges from that node to its neighbors
    private final Map<String, List<GraphEdge>> adjacency = new HashMap<>();

    // Set of all node IDs for fast O(1) membership checks
    private final Set<String> nodeIds = new HashSet<>();

    // Edge counter (tracks directed entries; divide by 2 for undirected count)
    private int directedEdgeCount = 0;

    // Immutable edge record: (target node ID, weight)
    public record GraphEdge(String targetId, double weight) {}
}
```

### Visual Example

For a small network with 4 cities:

```
     30km        45km
 A --------- B --------- C
              |
              | 20km
              |
              D
```

The adjacency list in memory looks like:

```
adjacency = {
    "A" → [ GraphEdge("B", 30.0) ],
    "B" → [ GraphEdge("A", 30.0), GraphEdge("C", 45.0), GraphEdge("D", 20.0) ],
    "C" → [ GraphEdge("B", 45.0) ],
    "D" → [ GraphEdge("B", 20.0) ]
}
```

**Key Point:** Each undirected edge is stored **twice** (once in each direction). This is essential for Dijkstra's algorithm to discover paths from any starting node.

### Operations & Their Time Complexities

| Operation | Method | Time Complexity | How It Works |
|-----------|--------|-----------------|--------------|
| Add a node | `addNode(id)` | O(1) amortized | Adds to `nodeIds` HashSet + creates empty list in `adjacency` HashMap |
| Add an edge | `addEdge(from, to, weight)` | O(1) amortized | Appends `GraphEdge` to both `adjacency.get(from)` and `adjacency.get(to)` |
| Get neighbors | `neighborsOf(nodeId)` | O(1) lookup | Returns the `List<GraphEdge>` directly from the HashMap |
| Get all node IDs | `allNodeIds()` | O(1) | Returns an unmodifiable view of the `nodeIds` set |
| Get node count | `nodeCount()` | O(1) | Returns `nodeIds.size()` |
| Get edge count | `edgeCount()` | O(1) | Returns `directedEdgeCount / 2` |

### Java Data Structures Used Inside TravelGraph

| Java Class | Role | Why This Choice |
|------------|------|-----------------|
| `HashMap<String, List<GraphEdge>>` | Main adjacency list | O(1) average lookup by node ID; auto-resizes |
| `HashSet<String>` | Track all node IDs | O(1) add/contains; prevents duplicate nodes |
| `ArrayList<GraphEdge>` | Neighbor list per node | O(1) amortized append; cache-friendly sequential iteration |
| `record GraphEdge` | Immutable edge data | Zero boilerplate; auto-generates equals/hashCode/toString; lightweight |

---

## 5. Algorithm #1 — Dijkstra's Shortest Path (SSSP Subroutine)

### What is Dijkstra's Algorithm?

Dijkstra's algorithm finds the **shortest path from a single source node to all other nodes** in a graph with **non-negative edge weights**. It is the most widely used shortest-path algorithm.

### Why Dijkstra's (Not BFS)?

- **BFS** works only for **unweighted** graphs (all edges cost 1).
- Our edges have **real-world weights** (30 km, 45 minutes, 1200 LKR), so we **must** use Dijkstra's.

### How It Works (Step-by-Step)

**Goal:** Starting from node S, find the shortest distance to every other node.

**Core Idea:** Always process the closest unvisited node next (greedy strategy).

```
ALGORITHM: Dijkstra's Shortest Path (Modified for Brandes')
=========================================================
INPUT:  Graph G, source node S
OUTPUT: dist[] (shortest distances), sigma[] (path counts), predecessors[]

1. INITIALIZE:
   - dist[v] = ∞ for all nodes v           ← "we haven't reached v yet"
   - dist[S] = 0                            ← "source is distance 0 from itself"
   - sigma[v] = 0 for all nodes v           ← "no shortest paths found yet"
   - sigma[S] = 1                           ← "there is exactly 1 path from S to S"
   - predecessors[v] = empty list           ← "no predecessors known yet"
   - PQ = priority queue (min-heap), insert S with priority 0
   - settled = empty set                    ← tracks finalized nodes

2. MAIN LOOP (while PQ is not empty):
   a. v = PQ.poll()                         ← extract node with smallest distance
   b. if v is already in settled → skip     ← stale entry, ignore it
   c. Mark v as settled
   d. Push v onto visitOrder stack           ← for the backward phase later
   
   e. For each edge (v → w) with weight edge.weight:
      - newDist = dist[v] + edge.weight
      
      - IF newDist < dist[w]:               ← found a SHORTER path to w
          dist[w] = newDist
          sigma[w] = sigma[v]               ← all shortest paths to w come through v
          predecessors[w] = [v]             ← v is the only predecessor on shortest paths
          PQ.add(w)                         ← enqueue w with new distance
      
      - ELSE IF newDist == dist[w]:         ← found an EQUALLY SHORT path to w
          sigma[w] += sigma[v]              ← add v's path count to w's count
          predecessors[w].add(v)            ← v is another predecessor

3. RETURN dist, sigma, predecessors, visitOrder
```

### Worked Example

```
Graph:            dist after processing from source A:
                  
     30        45      Node  │ dist │ sigma │ predecessors
 A ──── B ──── C        A   │   0  │   1   │  []
         \                B   │  30  │   1   │  [A]
       20 \               C   │  75  │   1   │  [B]
           D              D   │  50  │   1   │  [B]

Processing order: A(0) → B(30) → D(50) → C(75)
```

### Data Structures Used in Dijkstra's

| Data Structure | Java Class | Purpose | Why This Choice |
|----------------|------------|---------|-----------------|
| **Distance map** | `HashMap<String, Double>` | `dist[v]` = shortest known distance from source to v | O(1) lookup/update per node |
| **Path count map** | `HashMap<String, Double>` | `sigma[v]` = number of shortest paths from source to v | Uses Double to avoid integer overflow for large graphs |
| **Predecessor map** | `HashMap<String, List<String>>` | `predecessors[v]` = which nodes immediately precede v on shortest paths | Needed for backward phase |
| **Priority Queue (Min-Heap)** | `PriorityQueue<String>` | Always extracts the node with smallest `dist` value | Ensures greedy order; O(log V) extract-min |
| **Settled set** | `HashSet<String>` | Tracks nodes whose shortest distance is finalized | O(1) contains check; prevents re-processing |
| **Visit order stack** | `ArrayDeque<String>` | Records order nodes are settled (used later in backward phase) | O(1) push/pop; LIFO order needed for reverse traversal |

### Priority Queue — Deep Dive

The `PriorityQueue` (min-heap) is **the critical data structure** that makes Dijkstra efficient:

```java
// Ordered by current shortest distance — smallest first
PriorityQueue<String> pq = new PriorityQueue<>(
    Comparator.comparingDouble(dist::get)
);
```

**How it works internally:**
- Built on a **binary heap** (array-based complete binary tree)
- `pq.add(node)` → O(log V) — inserts and bubbles up
- `pq.poll()` → O(log V) — extracts minimum and sifts down
- Guarantees we always process the globally closest node next

**Why not just sort?** Sorting is O(V log V) upfront, but distances change as we discover shorter paths. A priority queue handles dynamic priorities efficiently.

**Stale entries:** When we find a shorter path to a node already in the PQ, we simply add it again with the new distance. The old (stale) entry will be skipped when polled because the node will already be in `settled`. This is called **lazy deletion** — simpler than decrease-key.

### Complexity of Dijkstra's

| Aspect | Complexity | Explanation |
|--------|-----------|-------------|
| **Time** | O((V + E) · log V) | Each node extracted once: O(V log V). Each edge relaxed once: O(E log V). Total: O((V + E) log V) |
| **Space** | O(V) | For dist, sigma, predecessors, PQ, settled, visitOrder |

---

## 6. Algorithm #2 — Brandes' Betweenness Centrality Algorithm

**File:** `service/CentralityService.java`

### What is Brandes' Algorithm?

Brandes' Algorithm (2001) is the **standard algorithm for computing exact betweenness centrality** on graphs. It is used by NetworkX, igraph, Neo4j GDS, and every major graph library.

**Published in:** Brandes, U. (2001). *A faster algorithm for betweenness centrality.* Journal of Mathematical Sociology, 25(2), 163-177.

### Why Brandes' Algorithm? (Comparison with Alternatives)

| Algorithm | Time Complexity | Practical Cost (V=500, E=1500) | Verdict |
|-----------|-----------------|-------------------------------|---------|
| **Brandes' (our choice)** | O(V · (V+E) · log V) | ~750K ops | ✅ **Optimal** |
| Floyd-Warshall | O(V³) | ~125M ops | ❌ 167× slower |
| Naive path enumeration | O(2^V) exponential | Intractable | ❌ Never practical |
| Separate Dijkstra per node | O(V · (V+E) · log V) | ~750K ops (distances only) | ❌ Still need betweenness pass |
| Approximate/Sampled | O(k · (V+E) · log V) | Less, but sacrifices accuracy | ❌ Unnecessary at this scale |

### The Algorithm — Complete Pseudocode

```
ALGORITHM: Brandes' Betweenness Centrality (Weighted Variant)
=============================================================
INPUT:  Graph G = (V, E) with positive edge weights
OUTPUT: betweenness[v] for all v ∈ V, closeness[v] for all v ∈ V

1. INITIALIZE GLOBAL ACCUMULATORS:
   betweenness[v] = 0          for all v ∈ V
   closenessSum[v] = 0         for all v ∈ V
   reachableCount[v] = 0       for all v ∈ V

2. FOR EACH SOURCE NODE s ∈ V:              ← O(V) iterations

   ┌──────────────────────────────────────────────────────────┐
   │  PHASE 1: FORWARD PASS (Dijkstra + Path Counting)       │
   │                                                          │
   │  Run modified Dijkstra from s.                           │
   │  This produces:                                          │
   │    • dist[v]          — shortest distance from s to v    │
   │    • sigma[v]         — number of shortest paths s → v   │
   │    • predecessors[v]  — nodes just before v on s→v paths │
   │    • visitOrder        — stack of nodes in settle order   │
   │                                                          │
   │  (See Dijkstra's algorithm in Section 5 for details)     │
   └──────────────────────────────────────────────────────────┘

   ┌──────────────────────────────────────────────────────────┐
   │  PHASE 2: CLOSENESS ACCUMULATION                         │
   │                                                          │
   │  For each node v ≠ s where dist[v] < ∞:                 │
   │    closenessSum[s] += dist[v]                            │
   │    reachableCount[s] += 1                                │
   └──────────────────────────────────────────────────────────┘

   ┌──────────────────────────────────────────────────────────┐
   │  PHASE 3: BACKWARD PASS (Dependency Accumulation)        │
   │  *** This is the KEY INSIGHT of Brandes' Algorithm ***   │
   │                                                          │
   │  delta[v] = 0              for all v ∈ V                 │
   │                                                          │
   │  While visitOrder is not empty:                          │
   │    w = visitOrder.pop()    ← process farthest first      │
   │                                                          │
   │    For each predecessor v of w:                          │
   │      contribution = (sigma[v] / sigma[w]) × (1 + δ(w))  │
   │      delta[v] += contribution                            │
   │                                                          │
   │    If w ≠ s:                                             │
   │      betweenness[w] += delta[w]                          │
   └──────────────────────────────────────────────────────────┘

3. POST-PROCESSING:
   a. Undirected graph correction:
      betweenness[v] = betweenness[v] / 2     for all v
      (Each pair (s,t) was counted from both s-side and t-side)
   
   b. Closeness centrality:
      closeness[v] = reachableCount[v] / closenessSum[v]
      (If reachableCount[v] = 0, then closeness[v] = 0)

4. RETURN betweenness, closeness
```

### The Backward Phase — Why It Works (Intuitive Explanation)

The **backward phase** is what makes Brandes' algorithm special. Here's the intuition:

**Betweenness centrality** of node v = "How many shortest paths between OTHER pairs of nodes pass through v?"

**Naively**, you'd enumerate all pairs (s,t), find all shortest paths between them, and check if v is on any of them. This is exponentially expensive.

**Brandes' insight:** You don't need to enumerate paths explicitly. Instead, for each source s:

1. Dijkstra gives you the shortest-path **tree** from s (encoded in `predecessors`).
2. Walking this tree **backward** (from leaves to root), you can compute each node's **dependency score** — the fraction of shortest paths from s that pass through it.
3. The formula `δ_s(v) += (σ(v)/σ(w)) × (1 + δ_s(w))` propagates dependencies from far nodes back toward s.

```
Example: Computing backward pass from source A

Graph:     A ──30── B ──45── C
                     \──20── D

After Dijkstra from A:
  sigma: A=1, B=1, C=1, D=1
  visitOrder (stack): [A, B, D, C]  ← A at bottom, C at top

Backward pass (pop order: C, D, B, A):
  Process C: predecessors = [B]
    contribution = (sigma[B]/sigma[C]) × (1 + delta[C])
                 = (1/1) × (1 + 0) = 1.0
    delta[B] += 1.0   → delta[B] = 1.0
    betweenness[C] += 0  (delta[C] = 0)
  
  Process D: predecessors = [B]
    contribution = (1/1) × (1 + 0) = 1.0
    delta[B] += 1.0   → delta[B] = 2.0
    betweenness[D] += 0  (delta[D] = 0)
  
  Process B: predecessors = [A]
    contribution = (1/1) × (1 + 2.0) = 3.0
    delta[A] += 3.0
    betweenness[B] += 2.0  ← B sits on paths A→C and A→D
  
  Process A: skip (A == source)

After this source iteration: betweenness[B] = 2.0
(B sits on all shortest paths from A to {C, D})
```

### Why Divide by 2 at the End?

In an **undirected** graph, Brandes' algorithm processes each source s and counts how many shortest paths from s to other nodes pass through each node v. But the pair (A, C) is counted twice:
- Once when source = A (path A → B → C, credits B)
- Once when source = C (path C → B → A, credits B)

So `betweenness[B]` is double the correct value. Dividing by 2 corrects this.

```java
betweenness.replaceAll((key, value) -> value / 2.0);
```

### Data Structures Used in the Full Algorithm

| Data Structure | Java Class | Scope | Purpose |
|----------------|------------|-------|---------|
| `betweenness` | `HashMap<String, Double>` | Global (across all sources) | Accumulates betweenness score for each node |
| `closenessSum` | `HashMap<String, Double>` | Global | Sum of distances from node to all reachable nodes |
| `reachableCount` | `HashMap<String, Integer>` | Global | Count of nodes reachable from each node |
| `dist` | `HashMap<String, Double>` | Per-source (reset each iteration) | Shortest distance from current source |
| `sigma` | `HashMap<String, Double>` | Per-source | Number of shortest paths from source |
| `predecessors` | `HashMap<String, List<String>>` | Per-source | Predecessor lists for backward phase |
| `pq` | `PriorityQueue<String>` | Per-source | Dijkstra's min-heap |
| `visitOrder` | `ArrayDeque<String>` (as Stack) | Per-source | Settlement order for backward traversal |
| `settled` | `HashSet<String>` | Per-source | Finalized nodes (prevents re-processing) |
| `delta` | `HashMap<String, Double>` | Per-source | Dependency accumulator for backward phase |

### Complete Complexity Analysis

| Aspect | Complexity | Breakdown |
|--------|-----------|-----------|
| **Time (Total)** | **O(V · (V + E) · log V)** | V sources × Dijkstra per source |
| Time per source (Dijkstra) | O((V + E) · log V) | Extract-min: O(V log V), edge relaxation: O(E log V) |
| Time per source (Backward) | O(V + E) | Visit each node once, iterate predecessors |
| Time (Closeness accum.) | O(V) per source | Sum distances to reachable nodes |
| **Space (Total)** | **O(V + E)** | Graph: O(V+E), per-source arrays: O(V) each (reused) |

**Practical performance:** For V = 500, E = 1500 (typical national travel network):
- Total operations ≈ 500 × (500 + 1500) × 10 ≈ **10 million** → completes in **< 100ms**

---

## 7. Centrality Metrics Explained

### Betweenness Centrality

**Formula:**

```
                    σ_st(v)
C_B(v) = Σ         ───────
        s≠v≠t       σ_st

Where:
  σ_st   = total number of shortest paths from s to t
  σ_st(v) = number of those shortest paths that pass through v
```

**Interpretation:**
- **High betweenness** → The node is a "bridge" or "gateway" — removing it would disconnect or significantly lengthen many routes.
- **Example:** Kandy might have high betweenness because most routes between the southern coast and the cultural triangle pass through it.

**Use case for travelers:** "If I'm passing through Sri Lanka, which towns will I inevitably pass through?"

### Closeness Centrality

**Formula:**

```
           |reachable(v)|
C_C(v) = ─────────────────
          Σ d(v, u)
          u∈reachable(v)

Where:
  |reachable(v)| = number of nodes reachable from v
  d(v, u)        = shortest distance from v to u
```

**Interpretation:**
- **High closeness** → The node can reach all other nodes quickly — it's centrally located.
- **Example:** A city in the geographic center of Sri Lanka would have high closeness because it minimizes total travel distance to everywhere else.

**Use case for travelers:** "Where should I base myself to minimize day-trip distances?"

### Undirected Graph Correction

Since the travel network is **undirected** (a road from A to B implies a road from B to A), betweenness scores are divided by 2 after computation. This is because Brandes' algorithm counts each pair (s, t) twice in an undirected graph:

```java
// Line 225 in CentralityService.java
betweenness.replaceAll((key, value) -> value / 2.0);
```

### Disconnected Graph Handling

If the graph has disconnected components (unreachable nodes), closeness centrality handles this gracefully:

```java
// If no nodes are reachable, closeness = 0 (not undefined)
closeness.put(node, reach > 0 ? (double) reach / sum : 0.0);
```

---

## 8. Service Layer — How Everything Connects

### GraphBuilderService — Database to Graph Converter

**File:** `service/GraphBuilderService.java`

**Purpose:** Reads all nodes and edges from the MySQL database and constructs an in-memory `TravelGraph`.

**Why separate from CentralityService?**
- **Single Responsibility:** One service builds the graph, another runs algorithms on it.
- **Testability:** You can test graph building independently of centrality computation.
- **Flexibility:** The graph builder could be swapped to read from a file, API, or cache without touching the algorithm.

**Flow:**
```
1. Validate weightType (must be one of 3 valid options)
2. nodeRepository.findAll() → add each node to TravelGraph
3. Validate graph is non-empty
4. edgeRepository.findAll() → add each edge with selected weight
5. Return the populated TravelGraph
```

**Weight Type Selection:**

```java
private double extractWeight(Edge edge, String weightType) {
    return switch (weightType) {
        case "distance_km"         → edge.getDistanceKm();
        case "travel_time_minutes" → edge.getTravelTimeMinutes().doubleValue();
        case "estimated_cost_lkr"  → edge.getEstimatedCostLkr().doubleValue();
        default → edge.getDistanceKm();  // fallback (unreachable due to validation)
    };
}
```

This allows the same graph to be analyzed from three perspectives:
- **Distance:** "Which hub minimizes total kilometers traveled?"
- **Time:** "Which hub minimizes total travel time?"
- **Cost:** "Which hub minimizes total travel expenses?"

### CentralityService — The Algorithm Engine

**File:** `service/CentralityService.java`

Implements Brandes' algorithm as described in Section 6. Returns a `CentralityResult` record:

```java
public record CentralityResult(
    Map<String, Double> betweenness,  // nodeId → betweenness score
    Map<String, Double> closeness     // nodeId → closeness score
) {}
```

### NetworkAnalysisService — The Orchestrator

**File:** `service/NetworkAnalysisService.java`

This is the **single entry point** for all controller endpoints. It coordinates the full pipeline:

```
Step 1: Build graph          → graphBuilderService.buildGraph(weightType)
Step 2: Run Brandes'         → centralityService.computeCentrality(graph)
Step 3: Lookup node names    → nodeRepository.findAll() → Map<nodeId, Node>
Step 4: Assemble DTOs        → Combine algorithm results with display names + coordinates
Step 5: Sort & return        → Two rankings: by betweenness (desc), by closeness (desc)
```

**Performance tracking:** The service measures wall-clock computation time:

```java
long startNanos = System.nanoTime();
// ... all computation ...
long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
```

This is included in the response so the frontend can display "Computed in 45ms".

---

## 9. REST API — Controller & Endpoints

**File:** `controller/NetworkAnalysisController.java`  
**Base Path:** `/api/network`

### Endpoint Reference

#### 1. Full Network Analysis

```
GET /api/network/analysis?weight={weightType}
```

| Parameter | Required | Default | Values |
|-----------|----------|---------|--------|
| `weight` | No | `distance_km` | `distance_km`, `travel_time_minutes`, `estimated_cost_lkr` |

**Response:** `200 OK` with `NetworkAnalysisResponseDTO`

```json
{
  "nodeCount": 50,
  "edgeCount": 150,
  "weightUsed": "distance_km",
  "computationTimeMs": 45,
  "rankedByBetweenness": [
    {
      "nodeId": "N012",
      "name": "Kandy",
      "betweenness": 245.5,
      "closeness": 0.0078,
      "latitude": 7.2906,
      "longitude": 80.6337
    }
  ],
  "rankedByCloseness": [
    {
      "nodeId": "N005",
      "name": "Dambulla",
      "betweenness": 120.3,
      "closeness": 0.0092,
      "latitude": 7.8600,
      "longitude": 80.6517
    }
  ]
}
```

---

#### 2. Top N by Betweenness

```
GET /api/network/betweenness?weight={weightType}&limit={n}
```

| Parameter | Required | Default | Description |
|-----------|----------|---------|-------------|
| `weight` | No | `distance_km` | Edge weight to use |
| `limit` | No | `10` | Max results to return |

**Response:** `200 OK` with `List<CentralityScoreDTO>` (sorted by betweenness descending)

---

#### 3. Top N by Closeness

```
GET /api/network/closeness?weight={weightType}&limit={n}
```

Same parameters as betweenness endpoint, but sorted by closeness descending.

---

#### 4. Single Destination Score

```
GET /api/network/location/{nodeId}?weight={weightType}
```

| Parameter | Required | Default | Description |
|-----------|----------|---------|-------------|
| `nodeId` (path) | Yes | — | The node_id to look up (e.g., "N001") |
| `weight` | No | `distance_km` | Edge weight to use |

**Response:** `200 OK` with single `CentralityScoreDTO`

**Error:** `404 Not Found` if nodeId doesn't exist:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "No location found with node_id: N999",
  "path": "/api/network/location/N999",
  "timestamp": 1693500000000
}
```

---

## 10. Error Handling Strategy

**File:** `controller/NetworkExceptionHandler.java`

The module uses a **centralized exception handler** (`@RestControllerAdvice`) scoped only to the `networkanalysis` package. This ensures consistent JSON error responses without interfering with other modules.

### Exception → HTTP Status Mapping

| Exception | HTTP Status | When It Happens |
|-----------|-------------|-----------------|
| `LocationNotFoundException` | `404 Not Found` | Requested node_id doesn't exist in the graph |
| `IllegalStateException` | `422 Unprocessable Entity` | No nodes in the database (empty graph) |
| `IllegalArgumentException` | `400 Bad Request` | Invalid weight type parameter |

### Error Response Shape

All errors return the same `ApiErrorDTO` JSON shape:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid weight type: 'invalid'. Must be one of: [distance_km, travel_time_minutes, estimated_cost_lkr]",
  "path": "/api/network/analysis",
  "timestamp": 1693500000000
}
```

### LocationNotFoundException

A custom **unchecked exception** (`extends RuntimeException`):

```java
public class LocationNotFoundException extends RuntimeException {
    public LocationNotFoundException(String nodeId) {
        super("No location found with node_id: " + nodeId);
    }
}
```

**Design decision:** Using an unchecked exception keeps the service method signatures clean and relies on the centralized handler to convert it to a proper HTTP response.

---

## 11. Data Transfer Objects (DTOs)

All DTOs are implemented as Java **records** — immutable, compact, and auto-serializable to JSON.

### CentralityScoreDTO

Represents one destination's centrality scores:

```java
public record CentralityScoreDTO(
    String nodeId,         // "N001"
    String name,           // "Colombo"
    double betweenness,    // 245.5
    double closeness,      // 0.0078
    Double latitude,       // 6.9271 (for map pins)
    Double longitude       // 79.8612
) {}
```

### NetworkAnalysisResponseDTO

The complete analysis response:

```java
public record NetworkAnalysisResponseDTO(
    int nodeCount,                              // 50
    int edgeCount,                              // 150
    String weightUsed,                          // "distance_km"
    long computationTimeMs,                     // 45
    List<CentralityScoreDTO> rankedByBetweenness,  // sorted desc
    List<CentralityScoreDTO> rankedByCloseness     // sorted desc
) {}
```

### ApiErrorDTO

Standard error response:

```java
public record ApiErrorDTO(
    int status,          // 404
    String error,        // "Not Found"
    String message,      // "No location found with node_id: N999"
    String path,         // "/api/network/location/N999"
    long timestamp       // 1693500000000
) {}
```

---

## 12. Complexity Summary

### Time Complexity

| Operation | Complexity | Where |
|-----------|-----------|-------|
| Build graph from DB | O(V + E) | `GraphBuilderService.buildGraph()` |
| Brandes' algorithm (total) | **O(V · (V + E) · log V)** | `CentralityService.computeCentrality()` |
| ├── Dijkstra per source | O((V + E) · log V) | Forward phase with PriorityQueue |
| ├── Backward pass per source | O(V + E) | Dependency accumulation |
| └── Closeness per source | O(V) | Sum distances |
| Assemble DTOs | O(V) | `NetworkAnalysisService.analyzeNetwork()` |
| Sort rankings | O(V · log V) | Java Stream `.sorted()` |
| **Total per request** | **O(V · (V + E) · log V)** | Dominated by Brandes' |

### Space Complexity

| Component | Complexity | What It Stores |
|-----------|-----------|----------------|
| TravelGraph (adjacency list) | O(V + E) | All nodes and edges |
| Global accumulators | O(V) | betweenness, closenessSum, reachableCount |
| Per-source working data | O(V) | dist, sigma, predecessors, delta, settled |
| Priority Queue | O(V) | At most V entries (with stale duplicates) |
| DTO lists | O(V) | CentralityScoreDTO for each node |
| **Total** | **O(V + E)** | Dominated by graph storage |

### Practical Performance Estimate

| Scale | V | E | Brandes' Time | Memory |
|-------|---|---|---------------|--------|
| Small (district) | 50 | 150 | ~5 ms | ~50 KB |
| Medium (province) | 200 | 600 | ~50 ms | ~200 KB |
| **National (Sri Lanka)** | **500** | **1,500** | **~100 ms** | **~500 KB** |
| Large (hypothetical) | 5,000 | 15,000 | ~10 sec | ~5 MB |

---

## 13. End-to-End Request Flow

Here is exactly what happens when the frontend calls `GET /api/network/analysis?weight=distance_km`:

```
┌──────────────────────────────────────────────────────────────────────┐
│ 1. HTTP Request arrives at NetworkAnalysisController                 │
│    GET /api/network/analysis?weight=distance_km                      │
└────────────────────────────┬─────────────────────────────────────────┘
                             │
                             ▼
┌──────────────────────────────────────────────────────────────────────┐
│ 2. Controller calls NetworkAnalysisService.analyzeNetwork("distance_km") │
└────────────────────────────┬─────────────────────────────────────────┘
                             │
                             ▼
┌──────────────────────────────────────────────────────────────────────┐
│ 3. GraphBuilderService.buildGraph("distance_km")                     │
│    a. Validate weight type ✓                                         │
│    b. SELECT * FROM nodes → 500 rows → graph.addNode() × 500        │
│    c. Check graph non-empty ✓                                        │
│    d. SELECT * FROM edges → 1500 rows → graph.addEdge() × 1500      │
│    e. Return TravelGraph (500 nodes, 1500 undirected edges)          │
└────────────────────────────┬─────────────────────────────────────────┘
                             │
                             ▼
┌──────────────────────────────────────────────────────────────────────┐
│ 4. CentralityService.computeCentrality(graph)                        │
│    a. Initialize betweenness[v]=0, closenessSum[v]=0 for all 500 v  │
│    b. FOR each of 500 source nodes:                                  │
│       i.   Run Dijkstra from source (PriorityQueue + HashMap)        │
│       ii.  Accumulate distances for closeness                        │
│       iii. Run backward phase (dependency accumulation)              │
│    c. Divide betweenness by 2 (undirected correction)                │
│    d. Compute closeness = reachable / distanceSum                    │
│    e. Return CentralityResult(betweenness, closeness)                │
└────────────────────────────┬─────────────────────────────────────────┘
                             │
                             ▼
┌──────────────────────────────────────────────────────────────────────┐
│ 5. Assemble DTOs                                                     │
│    a. SELECT * FROM nodes → Map<nodeId, Node> (for names & coords)   │
│    b. For each nodeId: create CentralityScoreDTO(id, name, betw,     │
│       close, lat, lng)                                               │
│    c. Sort by betweenness (descending) → rankedByBetweenness         │
│    d. Sort by closeness (descending) → rankedByCloseness             │
│    e. Measure elapsed time (nanoseconds → milliseconds)              │
└────────────────────────────┬─────────────────────────────────────────┘
                             │
                             ▼
┌──────────────────────────────────────────────────────────────────────┐
│ 6. Return NetworkAnalysisResponseDTO as JSON                         │
│    HTTP 200 OK                                                       │
│    Content-Type: application/json                                    │
│    {                                                                 │
│      "nodeCount": 500,                                               │
│      "edgeCount": 1500,                                              │
│      "weightUsed": "distance_km",                                    │
│      "computationTimeMs": 87,                                        │
│      "rankedByBetweenness": [...],                                   │
│      "rankedByCloseness": [...]                                      │
│    }                                                                 │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 14. Data Structures Cheat Sheet

A quick-reference table of **every** data structure used in this module, why it was chosen, and where:

| Data Structure | Java Class | Used In | Purpose | Time Complexity |
|----------------|------------|---------|---------|-----------------|
| **Adjacency List** | `HashMap<String, List<GraphEdge>>` | `TravelGraph` | Store graph topology | O(1) neighbor lookup |
| **Node ID Set** | `HashSet<String>` | `TravelGraph` | Track all nodes; O(1) membership | O(1) add/contains |
| **Immutable Edge** | `record GraphEdge(targetId, weight)` | `TravelGraph` | Lightweight edge representation | N/A (data only) |
| **Min-Heap (Priority Queue)** | `PriorityQueue<String>` | `CentralityService` (Dijkstra) | Extract closest unvisited node | O(log V) poll/add |
| **Distance Map** | `HashMap<String, Double>` | `CentralityService` (Dijkstra) | Track shortest distances | O(1) get/put |
| **Path Count Map** | `HashMap<String, Double>` | `CentralityService` (Dijkstra) | Count shortest paths (σ) | O(1) get/put |
| **Predecessor Map** | `HashMap<String, List<String>>` | `CentralityService` (Dijkstra) | Track predecessors on shortest paths | O(1) get + O(1) append |
| **Settled Set** | `HashSet<String>` | `CentralityService` (Dijkstra) | Prevent re-processing finalized nodes | O(1) contains/add |
| **Visit Order Stack** | `ArrayDeque<String>` | `CentralityService` (Backward) | LIFO order for reverse traversal | O(1) push/pop |
| **Dependency Map** | `HashMap<String, Double>` | `CentralityService` (Backward) | Accumulate δ values per source | O(1) get/put |
| **Betweenness Accumulator** | `HashMap<String, Double>` | `CentralityService` (Global) | Aggregate betweenness across all sources | O(1) merge |
| **Closeness Sum** | `HashMap<String, Double>` | `CentralityService` (Global) | Sum of distances for closeness calc | O(1) merge |
| **Reachable Count** | `HashMap<String, Integer>` | `CentralityService` (Global) | Count reachable nodes per source | O(1) merge |
| **Node Lookup Map** | `Map<String, Node>` (via Stream) | `NetworkAnalysisService` | Map nodeId → entity (for names) | O(1) get |
| **Sorted Lists** | `List<CentralityScoreDTO>` (via Stream) | `NetworkAnalysisService` | Final sorted rankings | O(V log V) sort |
| **Java Records** | `CentralityScoreDTO`, `NetworkAnalysisResponseDTO`, `ApiErrorDTO` | DTOs | Immutable data carriers for JSON serialization | N/A (data only) |

---

> **Document generated from source code analysis of the `networkanalysis` module.**  
> **All algorithm implementations can be found in the `service/` package.**
