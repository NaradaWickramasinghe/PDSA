package com.nibm.intelligenttravelmanagementsystem.networkanalysis.controller;

/**
 * Exception thrown when a requested location (node) is not found in the network graph.
 *
 * <p>This is a runtime exception (unchecked) that is caught and handled by
 * {@link NetworkExceptionHandler} to produce a consistent 404 JSON response.
 */
public class LocationNotFoundException extends RuntimeException {

    /**
     * Constructs a new LocationNotFoundException with a descriptive message.
     *
     * @param nodeId the node_id that was not found
     */
    public LocationNotFoundException(String nodeId) {
        super("No location found with node_id: " + nodeId);
    }
}
