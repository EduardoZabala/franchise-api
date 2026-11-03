package com.franchise.domain.model;

import com.franchise.domain.model.exception.BusinessException;
import com.franchise.domain.model.exception.BusinessMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.franchise.domain.model.DomainValidations.requireText;

/**
 * A franchise with its branches. Immutable: every change returns a new instance.
 */
public record Franchise(String id, String name, List<Branch> branches) {

    public Franchise {
        id = requireText(id, BusinessMessage.FRANCHISE_ID_REQUIRED);
        name = requireText(name, BusinessMessage.FRANCHISE_NAME_REQUIRED);
        branches = branches == null ? List.of() : List.copyOf(branches);
    }

    public static Franchise create(String name) {
        return new Franchise(UUID.randomUUID().toString(), name, List.of());
    }

    public Franchise rename(String newName) {
        return new Franchise(id, newName, branches);
    }

    public Franchise addBranch(Branch branch) {
        if (branch == null) {
            throw new BusinessException(BusinessMessage.FRANCHISE_BRANCH_REQUIRED);
        }
        if (hasBranchNamed(branch.name())) {
            throw new BusinessException(BusinessMessage.FRANCHISE_BRANCH_ALREADY_EXISTS);
        }
        List<Branch> updated = new ArrayList<>(branches);
        updated.add(branch);
        return new Franchise(id, name, updated);
    }

    private boolean hasBranchNamed(String branchName) {
        return branches.stream()
                .anyMatch(existing -> existing.name().equalsIgnoreCase(branchName));
    }
}
