package com.nibm.intelligenttravelmanagementsystem.networkanalysis.service;

import com.nibm.intelligenttravelmanagementsystem.networkanalysis.model.TravelGraph;
import com.nibm.intelligenttravelmanagementsystem.networkanalysis.model.TravelGraph.GraphEdge;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service implementing Brandes' Algorithm for computing betweenness and closeness
 * centrality on a weighted, undirected graph.
 *
 * <h2>Algorithm Overview — Brandes' Algorithm (2001)</h2>
 *
 * <p>Brandes' algorithm computes exact betweenness centrality in O(V·E) for unweighted
 * graphs or O(V·(V+E)·log V) for weighted graphs (using Dijkstra's algorithm as the
 * SSSP subroutine). This is the optimal known algorithm for exact betweenness centrality
 * on general graphs — it is the same algorithm used by NetworkX, igraph, and Neo4j GDS.
 *
 * <h3>Why Brandes' Algorithm was selected over alternatives:</h3>
 * <ul>
 *   <li><b>vs Floyd-Warshall (O(V³)):</b> Floyd-Warshall ignores graph sparsity entirely.
 *       For a sparse travel network with V=500, E≈1500, Brandes costs ~750K operations
 *       while Floyd-Warshall costs ~125M — a 167× difference.</li>
 *   <li><b>vs Naive path enumeration:</b> Exponential worst case. Never practical.</li>
 *   <li><b>vs Separate BFS/Dijkstra per node:</b> Computes distances only, not betweenness.
 *       Would need a separate (more expensive) pass for betweenness.</li>
 *   <li><b>vs Sampled/approximate betweenness:</b> Unnecessary at this graph scale
 *       (hundreds of nodes, not millions). Sacrifices accuracy for no real performance gain.</li>
 * </ul>
 *
 * <h3>Algorithm structure (for each source node s):</h3>
 * <ol>
 *   <li><b>Forward phase (Dijkstra with PriorityQueue):</b>
 *       Computes shortest distances, path counts (σ), and predecessor lists
 *       from source s to all reachable nodes.</li>
 *   <li><b>Closeness accumulation:</b>
 *       Sums distances from s to all reachable nodes (used for closeness centrality).</li>
 *   <li><b>Backward phase (dependency accumulation):</b>
 *       Traverses nodes in reverse distance order, accumulating betweenness
 *       dependencies using the formula:
 *       δ_s(v) += (σ(v)/σ(w)) · (1 + δ_s(w)) for each predecessor v of w.</li>
 * </ol>
 *
 * <h3>Complexity analysis:</h3>
 * <ul>
 *   <li><b>Time:</b> O(V · (V + E) · log V) for weighted graphs (Dijkstra per source)</li>
 *   <li><b>Space:</b> O(V + E) — dominated by the graph itself and per-source working arrays</li>
 * </ul>
 *
 * @see <a href="https://doi.org/10.1080/0022250X.2001.9990249">
 *      Brandes, U. (2001). A faster algorithm for betweenness centrality.
 *      Journal of Mathematical Sociology, 25(2), 163-177.</a>
 */
@Service
public class CentralityService {

    /**
     * Holds the results of centrality computation.
     *
     * @param betweenness map of nodeId → betweenness centrality score
     * @param closeness   map of nodeId → closeness centrality score
     */
    public record CentralityResult(
            Map<String, Double> betweenness,
            Map<String, Double> closeness
    ) {
    }

    /**
     * Computes both betweenness and closeness centrality for all nodes in the graph
     * using Brandes' algorithm with Dijkstra's SSSP subroutine (weighted variant).
     *
     * <p>The algorithm runs a modified Dijkstra from every node in the graph.
     * For each source node s:
     * <ol>
     *   <li>A PriorityQueue-based Dijkstra computes shortest distances, counts
     *       shortest paths (σ), and tracks predecessor sets.</li>
     *   <li>Distance sums are accumulated for closeness centrality.</li>
     *   <li>A backward pass over the Dijkstra's settlement order accumulates
     *       betweenness dependencies.</li>
     * </ol>
     *
     * <p>After processing all sources, betweenness scores are divided by 2
     * (undirected graph correction — each pair (s,t) is counted from both
     * s-side and t-side).
     *
     * @param graph the TravelGraph to analyze (must be non-empty)
     * @return CentralityResult containing betweenness and closeness maps
     */
    public CentralityResult computeCentrality(TravelGraph graph) {
        Set<String> nodes = graph.allNodeIds();
        int n = nodes.size();

        // Global accumulators — betweenness is accumulated across all source iterations
        Map<String, Double> betweenness = new HashMap<>(n);
        // Closeness intermediate data — sum of distances and count of reachable nodes per node
        Map<String, Double> closenessSum = new HashMap<>(n);
        Map<String, Integer> reachableCount = new HashMap<>(n);

        // Initialize all accumulators to zero
        for (String node : nodes) {
            betweenness.put(node, 0.0);
            closenessSum.put(node, 0.0);
            reachableCount.put(node, 0);
        }

        // ====================================================================
        // MAIN LOOP: Run Brandes' algorithm from each source node
        // Total time complexity: O(V · (V + E) · log V) for weighted graphs
        // ====================================================================
        for (String source : nodes) {

            // ----------------------------------------------------------------
            // FORWARD PHASE: Dijkstra's algorithm with path counting
            // Uses a PriorityQueue (min-heap) for O((V + E) log V) per source
            // ----------------------------------------------------------------

            // dist[v] = shortest distance from source to v
            Map<String, Double> dist = new HashMap<>(n);
            // sigma[v] = number of shortest paths from source to v
            Map<String, Double> sigma = new HashMap<>(n);
            // predecessors[v] = list of nodes immediately preceding v on shortest paths from source
            Map<String, List<String>> predecessors = new HashMap<>(n);

            // Initialize per-source data structures
            for (String node : nodes) {
                dist.put(node, Double.POSITIVE_INFINITY);
                sigma.put(node, 0.0);
                predecessors.put(node, new ArrayList<>());
            }

            // Source node initialization
            dist.put(source, 0.0);
            sigma.put(source, 1.0);

            // PriorityQueue ordered by current shortest distance (min-heap)
            // This is the Dijkstra priority queue — extracts the closest unsettled node
            PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingDouble(dist::get));
            pq.add(source);

            // Stack recording the order in which nodes are settled (non-decreasing distance)
            // Used in the backward phase — we pop in reverse order (farthest first)
            Deque<String> visitOrder = new ArrayDeque<>();

            // Settled set — once a node is settled, its shortest distance is final
            Set<String> settled = new HashSet<>();

            while (!pq.isEmpty()) {
                String v = pq.poll();

                // Skip stale entries — a node can appear multiple times in the PQ
                // when a shorter path is found after it was already enqueued
                if (settled.contains(v)) {
                    continue;
                }
                settled.add(v);
                visitOrder.push(v);

                // Relax all edges from v
                for (GraphEdge edge : graph.neighborsOf(v)) {
                    String w = edge.targetId();
                    double newDist = dist.get(v) + edge.weight();

                    if (newDist < dist.get(w)) {
                        // Found a strictly shorter path to w through v
                        dist.put(w, newDist);
                        sigma.put(w, sigma.get(v));
                        predecessors.put(w, new ArrayList<>(List.of(v)));
                        pq.add(w); // Enqueue with updated distance
                    } else if (Double.compare(newDist, dist.get(w)) == 0) {
                        // Found another shortest path of equal length — add to path count
                        sigma.put(w, sigma.get(w) + sigma.get(v));
                        predecessors.get(w).add(v);
                    }
                }
            }

            // ----------------------------------------------------------------
            // CLOSENESS ACCUMULATION
            // Sum distances from this source to all reachable nodes
            // ----------------------------------------------------------------
            for (String node : nodes) {
                if (!node.equals(source) && dist.get(node) < Double.POSITIVE_INFINITY) {
                    closenessSum.merge(source, dist.get(node), Double::sum);
                    reachableCount.merge(source, 1, Integer::sum);
                }
            }

            // ----------------------------------------------------------------
            // BACKWARD PHASE: Dependency accumulation (Brandes' key insight)
            //
            // δ_s(v) = Σ_{w: v is predecessor of w} (σ(v)/σ(w)) · (1 + δ_s(w))
            //
            // This computes each node's "share" of shortest paths that pass
            // through it, which is exactly betweenness centrality.
            // ----------------------------------------------------------------
            Map<String, Double> delta = new HashMap<>(n);
            for (String node : nodes) {
                delta.put(node, 0.0);
            }

            // Process nodes in reverse settlement order (farthest from source first)
            while (!visitOrder.isEmpty()) {
                String w = visitOrder.pop();
                for (String v : predecessors.get(w)) {
                    // The fraction of shortest paths from source through v to w
                    double contribution = (sigma.get(v) / sigma.get(w)) * (1.0 + delta.get(w));
                    delta.put(v, delta.get(v) + contribution);
                }
                // Accumulate to global betweenness (skip source itself)
                if (!w.equals(source)) {
                    betweenness.merge(w, delta.get(w), Double::sum);
                }
            }
        }

        // ====================================================================
        // POST-PROCESSING
        // ====================================================================

        // Undirected graph correction: in an undirected graph, each pair (s,t)
        // is counted from both the s-side and t-side traversals, so we halve
        // all betweenness scores to avoid double-counting.
        betweenness.replaceAll((key, value) -> value / 2.0);

        // Compute final closeness centrality:
        // closeness(v) = (number of reachable nodes from v) / (sum of distances to all reachable nodes)
        // This handles disconnected graphs correctly — isolated nodes get closeness = 0
        Map<String, Double> closeness = new HashMap<>(n);
        for (String node : nodes) {
            int reach = reachableCount.get(node);
            double sum = closenessSum.get(node);
            closeness.put(node, reach > 0 ? (double) reach / sum : 0.0);
        }

        return new CentralityResult(betweenness, closeness);
    }
}
