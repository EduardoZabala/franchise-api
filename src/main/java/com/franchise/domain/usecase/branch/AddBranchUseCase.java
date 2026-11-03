package com.franchise.domain.usecase.branch;

import com.franchise.domain.model.Branch;
import com.franchise.domain.model.exception.BusinessException;
import com.franchise.domain.model.exception.BusinessMessage;
import com.franchise.domain.model.gateways.FranchiseGateway;
import reactor.core.publisher.Mono;

/**
 * Criterion 3: adds a new branch to a franchise.
 */
public class AddBranchUseCase {

    private final FranchiseGateway franchiseGateway;

    public AddBranchUseCase(FranchiseGateway franchiseGateway) {
        this.franchiseGateway = franchiseGateway;
    }

    public Mono<Branch> execute(String franchiseId, String branchName) {
        return franchiseGateway.findFranchiseById(franchiseId)
                .switchIfEmpty(Mono.error(new BusinessException(BusinessMessage.FRANCHISE_NOT_FOUND)))
                .flatMap(franchise -> {
                    Branch branch = Branch.create(branchName);
                    franchise.addBranch(branch);
                    return franchiseGateway.saveBranch(franchiseId, branch);
                });
    }
}
