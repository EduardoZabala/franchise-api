package com.franchise.domain.model;

import com.franchise.domain.model.exception.BusinessException;
import com.franchise.domain.model.exception.BusinessMessage;

import java.util.UUID;

import static com.franchise.domain.model.DomainValidations.requireText;

/**
 * A product offered in a branch. Immutable: every change returns a new instance.
 */
public record Product(String id, String name, int stock) {

    public Product {
        id = requireText(id, BusinessMessage.PRODUCT_ID_REQUIRED);
        name = requireText(name, BusinessMessage.PRODUCT_NAME_REQUIRED);
        if (stock < 0) {
            throw new BusinessException(BusinessMessage.PRODUCT_STOCK_NEGATIVE);
        }
    }

    public static Product create(String name, int stock) {
        return new Product(UUID.randomUUID().toString(), name, stock);
    }

    public Product withStock(int newStock) {
        return new Product(id, name, newStock);
    }

    public Product rename(String newName) {
        return new Product(id, newName, stock);
    }
}
