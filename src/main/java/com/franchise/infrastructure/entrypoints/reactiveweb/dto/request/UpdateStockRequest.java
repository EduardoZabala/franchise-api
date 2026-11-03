package com.franchise.infrastructure.entrypoints.reactiveweb.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateStockRequest(
        @NotNull(message = "must not be null")
        @PositiveOrZero(message = "must be greater than or equal to 0")
        Integer stock
) {
}
