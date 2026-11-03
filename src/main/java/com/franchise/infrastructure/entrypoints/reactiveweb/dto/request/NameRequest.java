package com.franchise.infrastructure.entrypoints.reactiveweb.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body to create or rename a franchise or a branch, or to rename a product.
 * Messages are explicit so responses do not depend on the server locale.
 */
public record NameRequest(
        @NotBlank(message = "must not be blank")
        @Size(max = 100, message = "must have at most 100 characters")
        String name
) {
}
