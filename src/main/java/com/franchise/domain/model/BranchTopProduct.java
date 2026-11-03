package com.franchise.domain.model;

/**
 * The product with the most stock of a branch, together with the branch it belongs to.
 */
public record BranchTopProduct(String branchId, String branchName, Product product) {
}
