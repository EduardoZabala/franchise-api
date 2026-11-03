package com.franchise.infrastructure.entrypoints.reactiveweb.dto.response;

import com.franchise.domain.model.Branch;

public record BranchResponse(String id, String name) {

    public static BranchResponse from(Branch branch) {
        return new BranchResponse(branch.id(), branch.name());
    }
}
