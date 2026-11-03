package com.franchise.infrastructure.drivenadapters.dynamodb;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * Single-table item. One partition per franchise holds every element of the aggregate:
 * <pre>
 * pk                 | sk                          | type      | attributes
 * FRANCHISE#{fid}    | METADATA                    | FRANCHISE | id, name
 * FRANCHISE#{fid}    | BRANCH#{bid}                | BRANCH    | id, name
 * FRANCHISE#{fid}    | BRANCH#{bid}#PRODUCT#{pid}  | PRODUCT   | id, branchId, name, stock
 * </pre>
 * Mutable JavaBean because the DynamoDB enhanced client requires it; never leaves this adapter.
 */
@DynamoDbBean
public class FranchiseTableItem {

    private String pk;
    private String sk;
    private String type;
    private String id;
    private String branchId;
    private String name;
    private Integer stock;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("pk")
    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("sk")
    public String getSk() {
        return sk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}
