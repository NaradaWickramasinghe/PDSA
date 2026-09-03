package com.nibm.intelligenttravelmanagementsystem.networkanalysis.controller;

import com.nibm.intelligenttravelmanagementsystem.networkanalysis.dto.ApiErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralized exception handler for the network analysis module.
 *
 * <p>Catches specific exceptions thrown by the network analysis services
 * and converts them into consistent {@link ApiErrorDTO} JSON responses.
 * This ensures all error responses from this module share the same shape,
 * making the API predictable for frontend consumers.
 *
 * <p>Scoped to the {@code networkanalysis} package via {@code basePackages}
 * to avoid interfering with exception handling in other modules.
 */
@RestControllerAdvice(basePackages = "com.nibm.intelligenttravelmanagementsystem.networkanalysis")
public class NetworkExceptionHandler {

    /**
     * Handles LocationNotFoundException — returned when a requested node_id
     * does not exist in the graph.
     *
     * @param ex  the caught exception
     * @param req the HTTP request that triggered the exception
     * @return 404 Not Found response with ApiErrorDTO body
     */
    @ExceptionHandler(LocationNotFoundException.class)
    public ResponseEntity<ApiErrorDTO> handleLocationNotFound(
            LocationNotFoundException ex, HttpServletRequest req) {

        ApiErrorDTO error = new ApiErrorDTO(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                req.getRequestURI(),
                System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handles IllegalStateException — returned when the graph is empty
     * (no nodes in the database) and centrality cannot be computed.
     *
     * @param ex  the caught exception
     * @param req the HTTP request that triggered the exception
     * @return 422 Unprocessable Entity response with ApiErrorDTO body
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorDTO> handleEmptyGraph(
            IllegalStateException ex, HttpServletRequest req) {

        ApiErrorDTO error = new ApiErrorDTO(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Unprocessable Entity",
                ex.getMessage(),
                req.getRequestURI(),
                System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    /**
     * Handles IllegalArgumentException — returned when an invalid weight type
     * is provided in the query parameter.
     *
     * @param ex  the caught exception
     * @param req the HTTP request that triggered the exception
     * @return 400 Bad Request response with ApiErrorDTO body
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorDTO> handleInvalidArgument(
            IllegalArgumentException ex, HttpServletRequest req) {

        ApiErrorDTO error = new ApiErrorDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                req.getRequestURI(),
                System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
