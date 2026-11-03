package com.franchise.infrastructure.entrypoints.reactiveweb.handler;

import com.franchise.domain.usecase.franchise.CreateFranchiseUseCase;
import com.franchise.domain.usecase.franchise.UpdateFranchiseNameUseCase;
import com.franchise.infrastructure.entrypoints.reactiveweb.dto.request.NameRequest;
import com.franchise.infrastructure.entrypoints.reactiveweb.dto.response.FranchiseResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class FranchiseHandler {

    private final CreateFranchiseUseCase createFranchiseUseCase;
    private final UpdateFranchiseNameUseCase updateFranchiseNameUseCase;
    private final RequestValidator requestValidator;

    public FranchiseHandler(CreateFranchiseUseCase createFranchiseUseCase,
                            UpdateFranchiseNameUseCase updateFranchiseNameUseCase,
                            RequestValidator requestValidator) {
        this.createFranchiseUseCase = createFranchiseUseCase;
        this.updateFranchiseNameUseCase = updateFranchiseNameUseCase;
        this.requestValidator = requestValidator;
    }

    public Mono<ServerResponse> createFranchise(ServerRequest request) {
        return requestValidator.body(request, NameRequest.class)
                .flatMap(body -> createFranchiseUseCase.execute(body.name()))
                .map(FranchiseResponse::from)
                .flatMap(response -> ServerResponse
                        .created(URI.create("/api/v1/franchises/" + response.id()))
                        .bodyValue(response));
    }

    public Mono<ServerResponse> updateFranchiseName(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        return requestValidator.body(request, NameRequest.class)
                .flatMap(body -> updateFranchiseNameUseCase.execute(franchiseId, body.name()))
                .map(FranchiseResponse::from)
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }
}
