package com.franchise.infrastructure.drivenadapters.dynamodb;

import com.franchise.domain.model.Branch;
import com.franchise.domain.model.Franchise;
import com.franchise.domain.model.Product;
import com.franchise.domain.model.gateways.FranchiseGateway;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.List;

import static com.franchise.infrastructure.drivenadapters.dynamodb.FranchiseItemMapper.branchSk;
import static com.franchise.infrastructure.drivenadapters.dynamodb.FranchiseItemMapper.franchisePk;
import static com.franchise.infrastructure.drivenadapters.dynamodb.FranchiseItemMapper.productSk;

/**
 * {@link FranchiseGateway} backed by a single DynamoDB table, using the non-blocking async client.
 */
@Component
public class DynamoFranchiseAdapter implements FranchiseGateway {

    private final DynamoDbAsyncTable<FranchiseTableItem> table;

    public DynamoFranchiseAdapter(DynamoDbAsyncTable<FranchiseTableItem> table) {
        this.table = table;
    }

    @Override
    public Mono<Franchise> saveFranchise(Franchise franchise) {
        return put(FranchiseItemMapper.toItem(franchise)).thenReturn(franchise);
    }

    @Override
    public Mono<Branch> saveBranch(String franchiseId, Branch branch) {
        return put(FranchiseItemMapper.toItem(franchiseId, branch)).thenReturn(branch);
    }

    @Override
    public Mono<Product> saveProduct(String franchiseId, String branchId, Product product) {
        return put(FranchiseItemMapper.toItem(franchiseId, branchId, product)).thenReturn(product);
    }

    @Override
    public Mono<Void> deleteProduct(String franchiseId, String branchId, String productId) {
        Key key = Key.builder()
                .partitionValue(franchisePk(franchiseId))
                .sortValue(productSk(branchId, productId))
                .build();
        return Mono.fromFuture(() -> table.deleteItem(key)).then();
    }

    @Override
    public Mono<Franchise> findFranchiseById(String franchiseId) {
        QueryConditional wholePartition = QueryConditional.keyEqualTo(key -> key
                .partitionValue(franchisePk(franchiseId)));
        return query(wholePartition)
                .flatMap(items -> Mono.justOrEmpty(FranchiseItemMapper.toFranchise(items)));
    }

    @Override
    public Mono<Branch> findBranch(String franchiseId, String branchId) {
        QueryConditional branchAndProducts = QueryConditional.sortBeginsWith(key -> key
                .partitionValue(franchisePk(franchiseId))
                .sortValue(branchSk(branchId)));
        return query(branchAndProducts)
                .flatMap(items -> Mono.justOrEmpty(FranchiseItemMapper.toBranch(items, branchId)));
    }

    private Mono<Void> put(FranchiseTableItem item) {
        return Mono.fromFuture(() -> table.putItem(item));
    }

    private Mono<List<FranchiseTableItem>> query(QueryConditional condition) {
        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(condition)
                .consistentRead(true)
                .build();
        return Flux.from(table.query(request).items()).collectList();
    }
}
