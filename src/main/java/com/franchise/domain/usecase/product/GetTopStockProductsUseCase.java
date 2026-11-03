package com.franchise.domain.usecase.product;

import com.franchise.domain.model.BranchTopProduct;
import com.franchise.domain.model.Franchise;
import com.franchise.domain.model.exception.BusinessException;
import com.franchise.domain.model.exception.BusinessMessage;
import com.franchise.domain.model.gateways.FranchiseGateway;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Criterion 7: for a franchise, returns the product with the most stock of each branch.
 * Branches without products are skipped.
 */
public class GetTopStockProductsUseCase {

    private final FranchiseGateway franchiseGateway;

    public GetTopStockProductsUseCase(FranchiseGateway franchiseGateway) {
        this.franchiseGateway = franchiseGateway;
    }

    public Flux<BranchTopProduct> execute(String franchiseId) {
        return franchiseGateway.findFranchiseById(franchiseId)
                .switchIfEmpty(Mono.error(new BusinessException(BusinessMessage.FRANCHISE_NOT_FOUND)))
                .flatMapIterable(Franchise::branches)
                .concatMap(branch -> Mono.justOrEmpty(branch.productWithMostStock())
                        .map(product -> new BranchTopProduct(branch.id(), branch.name(), product)));
    }
}
