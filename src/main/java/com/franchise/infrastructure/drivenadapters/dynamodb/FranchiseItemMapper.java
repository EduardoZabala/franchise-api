package com.franchise.infrastructure.drivenadapters.dynamodb;

import com.franchise.domain.model.Branch;
import com.franchise.domain.model.Franchise;
import com.franchise.domain.model.Product;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Translates between the domain model and single-table items.
 */
final class FranchiseItemMapper {

    static final String TYPE_FRANCHISE = "FRANCHISE";
    static final String TYPE_BRANCH = "BRANCH";
    static final String TYPE_PRODUCT = "PRODUCT";
    static final String METADATA_SK = "METADATA";

    private FranchiseItemMapper() {
    }

    static String franchisePk(String franchiseId) {
        return "FRANCHISE#" + franchiseId;
    }

    static String branchSk(String branchId) {
        return "BRANCH#" + branchId;
    }

    static String productSk(String branchId, String productId) {
        return branchSk(branchId) + "#PRODUCT#" + productId;
    }

    static FranchiseTableItem toItem(Franchise franchise) {
        FranchiseTableItem item = new FranchiseTableItem();
        item.setPk(franchisePk(franchise.id()));
        item.setSk(METADATA_SK);
        item.setType(TYPE_FRANCHISE);
        item.setId(franchise.id());
        item.setName(franchise.name());
        return item;
    }

    static FranchiseTableItem toItem(String franchiseId, Branch branch) {
        FranchiseTableItem item = new FranchiseTableItem();
        item.setPk(franchisePk(franchiseId));
        item.setSk(branchSk(branch.id()));
        item.setType(TYPE_BRANCH);
        item.setId(branch.id());
        item.setName(branch.name());
        return item;
    }

    static FranchiseTableItem toItem(String franchiseId, String branchId, Product product) {
        FranchiseTableItem item = new FranchiseTableItem();
        item.setPk(franchisePk(franchiseId));
        item.setSk(productSk(branchId, product.id()));
        item.setType(TYPE_PRODUCT);
        item.setId(product.id());
        item.setBranchId(branchId);
        item.setName(product.name());
        item.setStock(product.stock());
        return item;
    }

    /**
     * Rebuilds a franchise from all the items of its partition.
     * Empty when the partition has no franchise metadata item.
     */
    static Optional<Franchise> toFranchise(List<FranchiseTableItem> items) {
        return items.stream()
                .filter(item -> TYPE_FRANCHISE.equals(item.getType()))
                .findFirst()
                .map(metadata -> new Franchise(metadata.getId(), metadata.getName(), toBranches(items)));
    }

    /**
     * Rebuilds one branch with its products. Empty when the branch item is not among the items.
     */
    static Optional<Branch> toBranch(List<FranchiseTableItem> items, String branchId) {
        return toBranches(items).stream()
                .filter(branch -> branch.id().equals(branchId))
                .findFirst();
    }

    private static List<Branch> toBranches(List<FranchiseTableItem> items) {
        Map<String, FranchiseTableItem> branchItems = new LinkedHashMap<>();
        Map<String, List<Product>> productsByBranch = new LinkedHashMap<>();
        for (FranchiseTableItem item : items) {
            if (TYPE_BRANCH.equals(item.getType())) {
                branchItems.put(item.getId(), item);
            } else if (TYPE_PRODUCT.equals(item.getType())) {
                productsByBranch.computeIfAbsent(item.getBranchId(), key -> new ArrayList<>())
                        .add(new Product(item.getId(), item.getName(), item.getStock()));
            }
        }
        return branchItems.values().stream()
                .map(branch -> new Branch(branch.getId(), branch.getName(),
                        productsByBranch.getOrDefault(branch.getId(), List.of())))
                .toList();
    }
}
