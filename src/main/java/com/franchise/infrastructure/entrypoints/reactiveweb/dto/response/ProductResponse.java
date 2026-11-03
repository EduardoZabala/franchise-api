package com.franchise.infrastructure.entrypoints.reactiveweb.dto.response;

import com.franchise.domain.model.Product;

public record ProductResponse(String id, String name, int stock) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(product.id(), product.name(), product.stock());
    }
}
