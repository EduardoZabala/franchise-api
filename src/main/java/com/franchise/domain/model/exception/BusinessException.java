package com.franchise.domain.model.exception;

/**
 * Thrown when a business rule is violated. Carries a {@link BusinessMessage}
 * so entry points can map it to a consistent error response.
 */
public class BusinessException extends RuntimeException {

    private final BusinessMessage businessMessage;

    public BusinessException(BusinessMessage businessMessage) {
        super(businessMessage.getMessage());
        this.businessMessage = businessMessage;
    }

    public BusinessMessage getBusinessMessage() {
        return businessMessage;
    }
}
