package com.franchise.infrastructure.entrypoints.reactiveweb.dto.response;

import java.util.List;

/**
 * Uniform error body returned by every endpoint.
 *
 * @param code    stable error code (e.g. FRN-005), safe for clients to rely on
 * @param message human readable description
 * @param details field level problems, empty when not applicable
 */
public record ErrorResponse(String code, String message, List<String> details) {

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, List.of());
    }
}
