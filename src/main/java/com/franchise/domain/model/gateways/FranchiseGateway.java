package com.franchise.domain.model.gateways;

import com.franchise.domain.model.Branch;
import com.franchise.domain.model.Franchise;
import com.franchise.domain.model.Product;
import reactor.core.publisher.Mono;

/**
 * Persistence gateway for the franchise aggregate.
 * <p>
 * Operations are granular: each write touches a single element (franchise,
 * branch or product) instead of rewriting the whole aggregate.
 * Find operations emit an empty {@link Mono} when nothing is found.
 */
public interface FranchiseGateway {

    /**
     * Creates or updates the franchise data only (id and name). Its branches are not written.
     */
    Mono<Franchise> saveFranchise(Franchise franchise);

    /**
     * Creates or updates the branch data only (id and name). Its products are not written.
     */
    Mono<Branch> saveBranch(String franchiseId, Branch branch);

    /**
     * Creates or updates a product of the given branch.
     */
    Mono<Product> saveProduct(String franchiseId, String branchId, Product product);

    Mono<Void> deleteProduct(String franchiseId, String branchId, String productId);

    /**
     * Finds a franchise with all its branches and their products.
     */
    Mono<Franchise> findFranchiseById(String franchiseId);

    /**
     * Finds a branch of the given franchise with all its products.
     */
    Mono<Branch> findBranch(String franchiseId, String branchId);
}
