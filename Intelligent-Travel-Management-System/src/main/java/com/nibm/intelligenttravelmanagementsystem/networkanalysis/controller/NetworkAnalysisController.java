package com.nibm.intelligenttravelmanagementsystem.networkanalysis.controller;

import com.nibm.intelligenttravelmanagementsystem.networkanalysis.dto.CentralityScoreDTO;
import com.nibm.intelligenttravelmanagementsystem.networkanalysis.dto.NetworkAnalysisResponseDTO;
import com.nibm.intelligenttravelmanagementsystem.networkanalysis.dto.PrimMstResponseDTO;
import com.nibm.intelligenttravelmanagementsystem.networkanalysis.service.NetworkAnalysisService;
import com.nibm.intelligenttravelmanagementsystem.networkanalysis.service.PrimMstAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing the Network Analysis module's endpoints.
 *
 * <p>All endpoints are under the base path {@code /api/network} and return JSON.
 * Error responses use a consistent {@link com.nibm.intelligenttravelmanagementsystem.networkanalysis.dto.ApiErrorDTO}
 * shape, handled centrally by {@link NetworkExceptionHandler}.
 *
 * <h2>Endpoint summary:</h2>
 * <table>
 *   <tr><th>Method</th><th>Path</th><th>Purpose</th><th>Success</th><th>Failure</th></tr>
 *   <tr><td>GET</td><td>/api/network/analysis</td><td>Full analysis (both rankings)</td><td>200</td><td>422, 400</td></tr>
 *   <tr><td>GET</td><td>/api/network/betweenness</td><td>Top N by betweenness</td><td>200</td><td>422, 400</td></tr>
 *   <tr><td>GET</td><td>/api/network/closeness</td><td>Top N by closeness</td><td>200</td><td>422, 400</td></tr>
 *   <tr><td>GET</td><td>/api/network/location/{id}</td><td>Single destination's scores</td><td>200</td><td>404, 400</td></tr>
 *   <tr><td>GET</td><td>/api/network/mst-prim</td><td>Minimum Spanning Forest using Prim's algorithm</td><td>200</td><td>500, 400</td></tr>
 * </table>
 *
 * <p><b>Note on the {@code weight} parameter:</b> Centrality endpoints accept an optional
 * {@code weight} query parameter to select which edge attribute is used for centrality
 * computation. Valid values: {@code distance_km} (default), {@code travel_time_minutes},
 * {@code estimated_cost_lkr}. This allows comparative analysis — e.g., "which town is
 * the best hub by distance vs. by travel cost?"
 *
 * <p><b>Note on the {@code limit} parameter:</b> The {@code limit} parameter on
 * {@code /betweenness} and {@code /closeness} controls response size only — it does
 * <em>not</em> reduce the underlying O(V·(V+E)·log V) computation, since the full
 * ranking must be computed before it can be trimmed.
 */
@RestController
@RequestMapping("/api/network")
@RequiredArgsConstructor
public class NetworkAnalysisController {

    private final NetworkAnalysisService networkAnalysisService;
    private final PrimMstAnalysisService primMstAnalysisService;

    /**
     * Full network analysis — returns both betweenness and closeness rankings
     * in a single response. This is the primary endpoint for the dashboard.
     *
     * @param weight which edge weight to use for computation (default: distance_km)
     * @return 200 OK with NetworkAnalysisResponseDTO containing both rankings
     */
    @GetMapping("/analysis")
    public ResponseEntity<NetworkAnalysisResponseDTO> getFullAnalysis(
            @RequestParam(defaultValue = "distance_km") String weight) {

        return ResponseEntity.ok(networkAnalysisService.analyzeNetwork(weight));
    }

    /**
     * Betweenness ranking only — a lighter payload for widgets that only need
     * "top gateway towns."
     *
     * @param weight which edge weight to use (default: distance_km)
     * @param limit  maximum number of results to return (default: 10)
     * @return 200 OK with list of CentralityScoreDTO sorted by betweenness descending
     */
    @GetMapping("/betweenness")
    public ResponseEntity<List<CentralityScoreDTO>> getBetweennessRanking(
            @RequestParam(defaultValue = "distance_km") String weight,
            @RequestParam(defaultValue = "10") int limit) {

        NetworkAnalysisResponseDTO fullResult = networkAnalysisService.analyzeNetwork(weight);
        List<CentralityScoreDTO> topN = fullResult.rankedByBetweenness().stream()
                .limit(limit)
                .toList();
        return ResponseEntity.ok(topN);
    }

    /**
     * Closeness ranking only — for a "best base to stay" widget.
     *
     * @param weight which edge weight to use (default: distance_km)
     * @param limit  maximum number of results to return (default: 10)
     * @return 200 OK with list of CentralityScoreDTO sorted by closeness descending
     */
    @GetMapping("/closeness")
    public ResponseEntity<List<CentralityScoreDTO>> getClosenessRanking(
            @RequestParam(defaultValue = "distance_km") String weight,
            @RequestParam(defaultValue = "10") int limit) {

        NetworkAnalysisResponseDTO fullResult = networkAnalysisService.analyzeNetwork(weight);
        List<CentralityScoreDTO> topN = fullResult.rankedByCloseness().stream()
                .limit(limit)
                .toList();
        return ResponseEntity.ok(topN);
    }

    /**
     * Centrality scores for a single destination — used by a "destination detail" page.
     * Returns 404 with ApiErrorDTO if the node_id does not exist.
     *
     * @param id     the node_id to look up (e.g. "N001")
     * @param weight which edge weight to use (default: distance_km)
     * @return 200 OK with CentralityScoreDTO for the specified node
     */
    @GetMapping("/location/{id}")
    public ResponseEntity<CentralityScoreDTO> getScoreForLocation(
            @PathVariable String id,
            @RequestParam(defaultValue = "distance_km") String weight) {

        return ResponseEntity.ok(networkAnalysisService.getScoreForLocation(id, weight));
    }

    /**
     * Computes the Minimum Spanning Tree (or Forest, if graph is disconnected)
     * using Prim's algorithm with a binary heap PriorityQueue.
     *
     * @param weight which edge weight to use (default: distance_km)
     * @return 200 OK with PrimMstResponseDTO containing the computed MST structures
     */
    @GetMapping("/mst-prim")
    public ResponseEntity<PrimMstResponseDTO> getMinimumSpanningForest(
            @RequestParam(defaultValue = "distance_km") String weight) {

        return ResponseEntity.ok(primMstAnalysisService.analyzeMst(weight));
    }
}