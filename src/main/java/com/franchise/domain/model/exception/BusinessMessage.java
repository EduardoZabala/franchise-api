package com.franchise.domain.model.exception;

/**
 * Catalog of business errors. Single source of truth for codes and messages.
 */
public enum BusinessMessage {

    PRODUCT_ID_REQUIRED("PRD-001", "Product id must not be blank"),
    PRODUCT_NAME_REQUIRED("PRD-002", "Product name must not be blank"),
    PRODUCT_STOCK_NEGATIVE("PRD-003", "Product stock must not be negative"),

    FRANCHISE_ID_REQUIRED("FRN-001","Franchise id must not be blank"),
    FRANCHISE_NAME_REQUIRED("FRN-002","Franchise name must not be blank"),
    FRANCHISE_BRANCH_REQUIRED("FRN-003","Branch to add must not be null"),
    FRANCHISE_BRANCH_ALREADY_EXISTS("FRN-004","A branch with the same name already exists in the franchise"),

    BRANCH_ID_REQUIRED("BRN-001", "Branch id must not be blank"),
    BRANCH_NAME_REQUIRED("BRN-002", "Branch name must not be blank"),
    BRANCH_PRODUCT_REQUIRED("BRN-003", "Product to add must not be null"),
    BRANCH_PRODUCT_ALREADY_EXISTS("BRN-004", "A product with the same name already exists in the branch");

    private final String code;
    private final String message;

    BusinessMessage(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
