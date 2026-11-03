package com.franchise.infrastructure.entrypoints.reactiveweb.error;

import com.franchise.domain.model.exception.BusinessException;
import com.franchise.domain.model.exception.BusinessMessage;
import com.franchise.infrastructure.entrypoints.reactiveweb.dto.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

/**
 * Translates any error raised while handling a request into a uniform {@link ErrorResponse}.
 * This is the only place that knows how business errors map to HTTP status codes.
 */
@Component
public class ApiErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiErrorHandler.class);

    public Mono<ServerResponse> handle(Throwable error, ServerRequest request) {
        return switch (error) {
            case BusinessException business -> respond(statusOf(business.getBusinessMessage()),
                    ErrorResponse.of(business.getBusinessMessage().getCode(), business.getMessage()));
            case InvalidRequestException invalid -> respond(HttpStatus.BAD_REQUEST,
                    new ErrorResponse("REQ-001", invalid.getMessage(), invalid.getDetails()));
            case ServerWebInputException input -> respond(HttpStatus.BAD_REQUEST,
                    ErrorResponse.of("REQ-002", "Malformed request body"));
            default -> {
                log.error("Unexpected error on {} {}", request.method(), request.path(), error);
                yield respond(HttpStatus.INTERNAL_SERVER_ERROR,
                        ErrorResponse.of("ERR-500", "Unexpected error"));
            }
        };
    }

    static HttpStatus statusOf(BusinessMessage message) {
        return switch (message) {
            case FRANCHISE_NOT_FOUND, BRANCH_NOT_FOUND, PRODUCT_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case FRANCHISE_BRANCH_ALREADY_EXISTS, BRANCH_PRODUCT_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
    }

    private static Mono<ServerResponse> respond(HttpStatus status, ErrorResponse body) {
        return ServerResponse.status(status).bodyValue(body);
    }
}
