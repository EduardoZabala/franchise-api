package com.franchise.infrastructure.drivenadapters.dynamodb;

import com.franchise.domain.model.Branch;
import com.franchise.domain.model.Franchise;
import com.franchise.domain.model.Product;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.localstack.LocalStackContainer;
import reactor.test.StepVerifier;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test against a real DynamoDB API emulated by LocalStack.
 */
@Testcontainers
class DynamoFranchiseAdapterTest {

    @Container
    private static final LocalStackContainer LOCALSTACK =
            new LocalStackContainer("localstack/localstack:4.9").withServices("dynamodb");

    private static DynamoDbAsyncClient client;
    private static DynamoFranchiseAdapter adapter;

    @BeforeAll
    static void setUp() {
        client = DynamoDbAsyncClient.builder()
                .endpointOverride(LOCALSTACK.getEndpoint())
                .region(Region.of(LOCALSTACK.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(LOCALSTACK.getAccessKey(), LOCALSTACK.getSecretKey())))
                .build();
        DynamoDbAsyncTable<FranchiseTableItem> table = DynamoDbEnhancedAsyncClient.builder()
                .dynamoDbClient(client)
                .build()
                .table("franchises", TableSchema.fromBean(FranchiseTableItem.class));
        table.createTable().join();
        adapter = new DynamoFranchiseAdapter(table);
    }

    @AfterAll
    static void tearDown() {
        client.close();
    }

    @Test
    void shouldRebuildWholeFranchiseFromItsItems() {
        Franchise franchise = Franchise.create("Burger House");
        Branch centro = Branch.create("Centro");
        Branch norte = Branch.create("Norte");
        Product papas = Product.create("Papas", 10);
        Product helado = Product.create("Helado", 4);

        adapter.saveFranchise(franchise)
                .then(adapter.saveBranch(franchise.id(), centro))
                .then(adapter.saveBranch(franchise.id(), norte))
                .then(adapter.saveProduct(franchise.id(), centro.id(), papas))
                .then(adapter.saveProduct(franchise.id(), norte.id(), helado))
                .block();

        StepVerifier.create(adapter.findFranchiseById(franchise.id()))
                .assertNext(found -> {
                    assertThat(found.id()).isEqualTo(franchise.id());
                    assertThat(found.name()).isEqualTo("Burger House");
                    assertThat(found.branches()).containsExactlyInAnyOrder(
                            new Branch(centro.id(), "Centro", List.of(papas)),
                            new Branch(norte.id(), "Norte", List.of(helado)));
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenFranchiseDoesNotExist() {
        StepVerifier.create(adapter.findFranchiseById("unknown"))
                .verifyComplete();
    }

    @Test
    void shouldFindBranchWithOnlyItsOwnProducts() {
        Franchise franchise = Franchise.create("Burger House");
        Branch centro = Branch.create("Centro");
        Branch norte = Branch.create("Norte");
        Product papas = Product.create("Papas", 10);

        adapter.saveFranchise(franchise)
                .then(adapter.saveBranch(franchise.id(), centro))
                .then(adapter.saveBranch(franchise.id(), norte))
                .then(adapter.saveProduct(franchise.id(), centro.id(), papas))
                .then(adapter.saveProduct(franchise.id(), norte.id(), Product.create("Helado", 4)))
                .block();

        StepVerifier.create(adapter.findBranch(franchise.id(), centro.id()))
                .expectNext(new Branch(centro.id(), "Centro", List.of(papas)))
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenBranchDoesNotExist() {
        Franchise franchise = Franchise.create("Burger House");
        adapter.saveFranchise(franchise).block();

        StepVerifier.create(adapter.findBranch(franchise.id(), "unknown"))
                .verifyComplete();
    }

    @Test
    void shouldOverwriteProductOnSave() {
        Franchise franchise = Franchise.create("Burger House");
        Branch centro = Branch.create("Centro");
        Product papas = Product.create("Papas", 10);

        adapter.saveFranchise(franchise)
                .then(adapter.saveBranch(franchise.id(), centro))
                .then(adapter.saveProduct(franchise.id(), centro.id(), papas))
                .then(adapter.saveProduct(franchise.id(), centro.id(), papas.withStock(99)))
                .block();

        StepVerifier.create(adapter.findBranch(franchise.id(), centro.id()))
                .assertNext(branch -> assertThat(branch.products()).containsExactly(papas.withStock(99)))
                .verifyComplete();
    }

    @Test
    void shouldDeleteProduct() {
        Franchise franchise = Franchise.create("Burger House");
        Branch centro = Branch.create("Centro");
        Product papas = Product.create("Papas", 10);

        adapter.saveFranchise(franchise)
                .then(adapter.saveBranch(franchise.id(), centro))
                .then(adapter.saveProduct(franchise.id(), centro.id(), papas))
                .then(adapter.deleteProduct(franchise.id(), centro.id(), papas.id()))
                .block();

        StepVerifier.create(adapter.findBranch(franchise.id(), centro.id()))
                .assertNext(branch -> assertThat(branch.products()).isEmpty())
                .verifyComplete();
    }

    @Test
    void shouldRenameFranchiseWithoutLosingBranches() {
        Franchise franchise = Franchise.create("Burger House");
        Branch centro = Branch.create("Centro");

        adapter.saveFranchise(franchise)
                .then(adapter.saveBranch(franchise.id(), centro))
                .then(adapter.saveFranchise(franchise.rename("Burger Palace")))
                .block();

        StepVerifier.create(adapter.findFranchiseById(franchise.id()))
                .assertNext(found -> {
                    assertThat(found.name()).isEqualTo("Burger Palace");
                    assertThat(found.branches()).containsExactly(centro);
                })
                .verifyComplete();
    }
}
