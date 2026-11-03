package com.franchise.domain.usecase.branch;

import com.franchise.domain.model.Branch;
import com.franchise.domain.model.exception.BusinessException;
import com.franchise.domain.model.exception.BusinessMessage;
import com.franchise.domain.model.gateways.FranchiseGateway;
import reactor.core.publisher.Mono;

/**
 * Extra: renames a branch, keeping names unique within its franchise.
 */
public class UpdateBranchNameUseCase {

    private final FranchiseGateway franchiseGateway;

    public UpdateBranchNameUseCase(FranchiseGateway franchiseGateway) {
        this.franchiseGateway = franchiseGateway;
    }

    public Mono<Branch> execute(String franchiseId, String branchId, String newName) {
        return franchiseGateway.findFranchiseById(franchiseId)
                .switchIfEmpty(Mono.error(new BusinessException(BusinessMessage.FRANCHISE_NOT_FOUND)))
                .map(franchise -> franchise.renameBranch(branchId, newName))
                .flatMap(renamed -> franchiseGateway.saveBranch(franchiseId, renamed));
    }
}
