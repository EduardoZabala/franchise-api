package com.franchise.domain.usecase.product;

import com.franchise.domain.model.Product;
import com.franchise.domain.model.exception.BusinessException;
import com.franchise.domain.model.exception.BusinessMessage;
import com.franchise.domain.model.gateways.FranchiseGateway;
import reactor.core.publisher.Mono;

/**
 * Extra: renames a product, keeping names unique within its branch.
 */
public class UpdateProductNameUseCase {

    private final FranchiseGateway franchiseGateway;

    public UpdateProductNameUseCase(FranchiseGateway franchiseGateway) {
        this.franchiseGateway = franchiseGateway;
    }

    public Mono<Product> execute(String franchiseId, String branchId, String productId, String newName) {
        return franchiseGateway.findBranch(franchiseId, branchId)
                .switchIfEmpty(Mono.error(new BusinessException(BusinessMessage.BRANCH_NOT_FOUND)))
                .map(branch -> branch.renameProduct(productId, newName))
                .flatMap(renamed -> franchiseGateway.saveProduct(franchiseId, branchId, renamed));
    }
}
