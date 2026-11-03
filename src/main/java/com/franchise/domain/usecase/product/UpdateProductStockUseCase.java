package com.franchise.domain.usecase.product;

import com.franchise.domain.model.Product;
import com.franchise.domain.model.exception.BusinessException;
import com.franchise.domain.model.exception.BusinessMessage;
import com.franchise.domain.model.gateways.FranchiseGateway;
import reactor.core.publisher.Mono;

/**
 * Criterion 6: changes the stock of a product.
 */
public class UpdateProductStockUseCase {

    private final FranchiseGateway franchiseGateway;

    public UpdateProductStockUseCase(FranchiseGateway franchiseGateway) {
        this.franchiseGateway = franchiseGateway;
    }

    public Mono<Product> execute(String franchiseId, String branchId, String productId, int newStock) {
        return franchiseGateway.findBranch(franchiseId, branchId)
                .switchIfEmpty(Mono.error(new BusinessException(BusinessMessage.BRANCH_NOT_FOUND)))
                .flatMap(branch -> Mono.justOrEmpty(branch.findProduct(productId)))
                .switchIfEmpty(Mono.error(new BusinessException(BusinessMessage.PRODUCT_NOT_FOUND)))
                .map(product -> product.withStock(newStock))
                .flatMap(updated -> franchiseGateway.saveProduct(franchiseId, branchId, updated));
    }
}
