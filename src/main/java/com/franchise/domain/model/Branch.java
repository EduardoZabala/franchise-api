package com.franchise.domain.model;

import com.franchise.domain.model.exception.BusinessException;
import com.franchise.domain.model.exception.BusinessMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.franchise.domain.model.DomainValidations.requireText;

/**
 * A branch of a franchise with the products it offers.
 * Immutable: every change returns a new instance.
 */
public record Branch(String id, String name, List<Product> products) {

    public Branch {
        id = requireText(id, BusinessMessage.BRANCH_ID_REQUIRED);
        name = requireText(name, BusinessMessage.BRANCH_NAME_REQUIRED);
        products = products == null ? List.of() : List.copyOf(products);
    }

    public static Branch create(String name) {
        return new Branch(UUID.randomUUID().toString(), name, List.of());
    }

    public Branch rename(String newName) {
        return new Branch(id, newName, products);
    }

    public Branch addProduct(Product product) {
        if (product == null) {
            throw new BusinessException(BusinessMessage.BRANCH_PRODUCT_REQUIRED);
        }
        ensureNameIsAvailable(product);
        List<Product> updated = new ArrayList<>(products);
        updated.add(product);
        return new Branch(id, name, updated);
    }

    /**
     * Renames one of the branch products, keeping product names unique within the branch.
     *
     * @return the renamed product
     */
    public Product renameProduct(String productId, String newName) {
        Product renamed = findProduct(productId)
                .orElseThrow(() -> new BusinessException(BusinessMessage.PRODUCT_NOT_FOUND))
                .rename(newName);
        ensureNameIsAvailable(renamed);
        return renamed;
    }

    public Optional<Product> findProduct(String productId) {
        return products.stream()
                .filter(product -> product.id().equals(productId))
                .findFirst();
    }

    /**
     * Returns the product with the highest stock. On a tie, the first one added wins.
     */
    public Optional<Product> productWithMostStock() {
        return products.stream()
                .reduce((current, candidate) -> candidate.stock() > current.stock() ? candidate : current);
    }

    private void ensureNameIsAvailable(Product candidate) {
        boolean taken = products.stream()
                .anyMatch(other -> !other.id().equals(candidate.id())
                        && other.name().equalsIgnoreCase(candidate.name()));
        if (taken) {
            throw new BusinessException(BusinessMessage.BRANCH_PRODUCT_ALREADY_EXISTS);
        }
    }
}
