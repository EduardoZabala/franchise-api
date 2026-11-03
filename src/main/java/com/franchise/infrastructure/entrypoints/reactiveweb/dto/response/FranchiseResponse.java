package com.franchise.infrastructure.entrypoints.reactiveweb.dto.response;

import com.franchise.domain.model.Franchise;

public record FranchiseResponse(String id, String name) {

    public static FranchiseResponse from(Franchise franchise) {
        return new FranchiseResponse(franchise.id(), franchise.name());
    }
}
