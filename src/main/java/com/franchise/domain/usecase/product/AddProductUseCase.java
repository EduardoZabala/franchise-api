package com.franchise.domain.usecase.product;

import com.franchise.domain.model.Product;
import com.franchise.domain.model.exception.BusinessException;
import com.franchise.domain.model.exception.BusinessMessage;
import com.franchise.domain.model.gateways.FranchiseGateway;
import reactor.core.publisher.Mono;

/**
 * Criterion 4: adds a new product to a branch.
 */
public class AddProductUseCase {

    private final FranchiseGateway franchiseGateway;

    public AddProductUseCase(FranchiseGateway franchiseGateway) {
        this.franchiseGateway = franchiseGateway;
    }

    public Mono<Product> execute(String franchiseId, String branchId, String productName, int stock) {
        return franchiseGateway.findBranch(franchiseId, branchId)
                .switchIfEmpty(Mono.error(new BusinessException(BusinessMessage.BRANCH_NOT_FOUND)))
                .flatMap(branch -> {
                    Product product = Product.create(productName, stock);
                    branch.addProduct(product);
                    return franchiseGateway.saveProduct(franchiseId, branchId, product);
                });
    }
}
