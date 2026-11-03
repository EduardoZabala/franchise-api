package com.franchise.domain.model;

import com.franchise.domain.model.exception.BusinessException;
import com.franchise.domain.model.exception.BusinessMessage;

/**
 * Shared invariant checks for the domain model.
 */
final class DomainValidations {

    private DomainValidations() {
    }

    /**
     * Ensures the value is not null or blank and returns it without surrounding whitespace.
     *
     * @throws BusinessException with the given error when the value is blank
     */
    static String requireText(String value, BusinessMessage error) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(error);
        }
        return value.strip();
    }
}
