package com.franchise.infrastructure.entrypoints.reactiveweb.handler;

import com.franchise.domain.usecase.branch.AddBranchUseCase;
import com.franchise.domain.usecase.branch.UpdateBranchNameUseCase;
import com.franchise.infrastructure.entrypoints.reactiveweb.dto.request.NameRequest;
import com.franchise.infrastructure.entrypoints.reactiveweb.dto.response.BranchResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class BranchHandler {

    private final AddBranchUseCase addBranchUseCase;
    private final UpdateBranchNameUseCase updateBranchNameUseCase;
    private final RequestValidator requestValidator;

    public BranchHandler(AddBranchUseCase addBranchUseCase,
                         UpdateBranchNameUseCase updateBranchNameUseCase,
                         RequestValidator requestValidator) {
        this.addBranchUseCase = addBranchUseCase;
        this.updateBranchNameUseCase = updateBranchNameUseCase;
        this.requestValidator = requestValidator;
    }

    public Mono<ServerResponse> addBranch(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        return requestValidator.body(request, NameRequest.class)
                .flatMap(body -> addBranchUseCase.execute(franchiseId, body.name()))
                .map(BranchResponse::from)
                .flatMap(response -> ServerResponse
                        .created(URI.create("/api/v1/franchises/" + franchiseId + "/branches/" + response.id()))
                        .bodyValue(response));
    }

    public Mono<ServerResponse> updateBranchName(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        String branchId = request.pathVariable("branchId");
        return requestValidator.body(request, NameRequest.class)
                .flatMap(body -> updateBranchNameUseCase.execute(franchiseId, branchId, body.name()))
                .map(BranchResponse::from)
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }
}
