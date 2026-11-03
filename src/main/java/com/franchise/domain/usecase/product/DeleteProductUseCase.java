package com.franchise.domain.usecase.product;

import com.franchise.domain.model.exception.BusinessException;
import com.franchise.domain.model.exception.BusinessMessage;
import com.franchise.domain.model.gateways.FranchiseGateway;
import reactor.core.publisher.Mono;

/**
 * Criterion 5: removes a product from a branch.
 */
public class DeleteProductUseCase {

    private final FranchiseGateway franchiseGateway;

    public DeleteProductUseCase(FranchiseGateway franchiseGateway) {
        this.franchiseGateway = franchiseGateway;
    }

    public Mono<Void> execute(String franchiseId, String branchId, String productId) {
        return franchiseGateway.findBranch(franchiseId, branchId)
                .switchIfEmpty(Mono.error(new BusinessException(BusinessMessage.BRANCH_NOT_FOUND)))
                .flatMap(branch -> Mono.justOrEmpty(branch.findProduct(productId)))
                .switchIfEmpty(Mono.error(new BusinessException(BusinessMessage.PRODUCT_NOT_FOUND)))
                .flatMap(product -> franchiseGateway.deleteProduct(franchiseId, branchId, product.id()));
    }
}
