package com.franchise.domain.usecase.franchise;

import com.franchise.domain.model.Franchise;
import com.franchise.domain.model.exception.BusinessException;
import com.franchise.domain.model.exception.BusinessMessage;
import com.franchise.domain.model.gateways.FranchiseGateway;
import reactor.core.publisher.Mono;

/**
 * Extra: renames a franchise.
 */
public class UpdateFranchiseNameUseCase {

    private final FranchiseGateway franchiseGateway;

    public UpdateFranchiseNameUseCase(FranchiseGateway franchiseGateway) {
        this.franchiseGateway = franchiseGateway;
    }

    public Mono<Franchise> execute(String franchiseId, String newName) {
        return franchiseGateway.findFranchiseById(franchiseId)
                .switchIfEmpty(Mono.error(new BusinessException(BusinessMessage.FRANCHISE_NOT_FOUND)))
                .map(franchise -> franchise.rename(newName))
                .flatMap(franchiseGateway::saveFranchise);
    }
}
