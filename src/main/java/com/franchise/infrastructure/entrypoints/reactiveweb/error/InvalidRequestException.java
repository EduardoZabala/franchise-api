package com.franchise.infrastructure.entrypoints.reactiveweb.error;

import java.util.List;

/**
 * The HTTP request is malformed or fails bean validation. Belongs to the web layer, not to the domain.
 */
public class InvalidRequestException extends RuntimeException {

    private final List<String> details;

    public InvalidRequestException(List<String> details) {
        super("Invalid request");
        this.details = List.copyOf(details);
    }

    public List<String> getDetails() {
        return details;
    }
}
