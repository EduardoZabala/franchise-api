package com.franchise.infrastructure.entrypoints.reactiveweb.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(
        @NotBlank(message = "must not be blank")
        @Size(max = 100, message = "must have at most 100 characters")
        String name,

        @NotNull(message = "must not be null")
        @PositiveOrZero(message = "must be greater than or equal to 0")
        Integer stock
) {
}
