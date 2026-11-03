package com.franchise.domain.usecase.franchise;

import com.franchise.domain.model.Franchise;
import com.franchise.domain.model.gateways.FranchiseGateway;
import reactor.core.publisher.Mono;

/**
 * Criterion 2: registers a new franchise without branches.
 */
public class CreateFranchiseUseCase {

    private final FranchiseGateway franchiseGateway;

    public CreateFranchiseUseCase(FranchiseGateway franchiseGateway) {
        this.franchiseGateway = franchiseGateway;
    }

    public Mono<Franchise> execute(String name) {
        return Mono.fromCallable(() -> Franchise.create(name))
                .flatMap(franchiseGateway::saveFranchise);
    }
}
