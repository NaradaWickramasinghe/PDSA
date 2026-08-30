package com.nibm.intelligenttravelmanagementsystem.networkanalysis.dto;

/**
 * Standard error response shape for all error responses in the network analysis module.
 *
 * <p>Provides a consistent JSON structure for error responses, matching
 * standard REST API conventions. Used by {@code NetworkExceptionHandler}
 * to format all error responses uniformly.
 *
 * @param status    HTTP status code (e.g. 404, 422)
 * @param error     HTTP status reason phrase (e.g. "Not Found", "Unprocessable Entity")
 * @param message   human-readable error description
 * @param path      the request URI that caused the error
 * @param timestamp epoch milliseconds when the error occurred
 */
public record ApiErrorDTO(
        int status,
        String error,
        String message,
        String path,
        long timestamp
) {
}
