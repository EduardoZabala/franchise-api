package com.franchise.infrastructure.entrypoints.reactiveweb.dto.response;

import com.franchise.domain.model.BranchTopProduct;

/**
 * Criterion 7 item: the product with the most stock of a branch, saying which branch it belongs to.
 */
public record TopStockProductResponse(
        String branchId,
        String branchName,
        String productId,
        String productName,
        int stock
) {

    public static TopStockProductResponse from(BranchTopProduct topProduct) {
        return new TopStockProductResponse(
                topProduct.branchId(),
                topProduct.branchName(),
                topProduct.product().id(),
                topProduct.product().name(),
                topProduct.product().stock());
    }
}
