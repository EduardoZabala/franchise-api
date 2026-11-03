package com.franchise.infrastructure.entrypoints.reactiveweb.handler;

import com.franchise.infrastructure.entrypoints.reactiveweb.error.InvalidRequestException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

/**
 * Reads the request body and applies Bean Validation, as functional endpoints do not do it automatically.
 */
@Component
public class RequestValidator {

    private final Validator validator;

    public RequestValidator(Validator validator) {
        this.validator = validator;
    }

    public <T> Mono<T> body(ServerRequest request, Class<T> type) {
        return request.bodyToMono(type)
                .switchIfEmpty(Mono.error(() -> new InvalidRequestException(List.of("Request body is required"))))
                .doOnNext(this::validate);
    }

    private <T> void validate(T body) {
        Set<ConstraintViolation<T>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            throw new InvalidRequestException(violations.stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .sorted()
                    .toList());
        }
    }
}
