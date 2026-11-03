package com.franchise.domain.usecase;

import com.franchise.domain.model.exception.BusinessException;
import com.franchise.domain.model.exception.BusinessMessage;

import java.util.function.Predicate;

/**
 * Test helper to match business errors in reactive flows:
 * {@code StepVerifier.create(flow).expectErrorMatches(businessError(X)).verify()}.
 */
public final class BusinessErrors {

    private BusinessErrors() {
    }

    public static Predicate<Throwable> businessError(BusinessMessage expected) {
        return error -> error instanceof BusinessException businessException
                && businessException.getBusinessMessage() == expected;
    }
}
