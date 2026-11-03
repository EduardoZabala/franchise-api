package com.franchise.domain.usecase.product;

import com.franchise.domain.model.Branch;
import com.franchise.domain.model.Product;
import com.franchise.domain.model.exception.BusinessMessage;
import com.franchise.domain.model.gateways.FranchiseGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static com.franchise.domain.usecase.BusinessErrors.businessError;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddProductUseCaseTest {

    private static final String FRANCHISE_ID = "f-1";
    private static final String BRANCH_ID = "b-1";

    @Mock
    private FranchiseGateway franchiseGateway;

    private AddProductUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AddProductUseCase(franchiseGateway);
    }

    @Test
    void shouldSaveNewProductInBranch() {
        Branch branch = new Branch(BRANCH_ID, "Centro", List.of());
        when(franchiseGateway.findBranch(FRANCHISE_ID, BRANCH_ID)).thenReturn(Mono.just(branch));
        when(franchiseGateway.saveProduct(eq(FRANCHISE_ID), eq(BRANCH_ID), any(Product.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(2)));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, "Papas", 10))
                .assertNext(product -> {
                    assertThat(product.id()).isNotBlank();
                    assertThat(product.name()).isEqualTo("Papas");
                    assertThat(product.stock()).isEqualTo(10);
                })
                .verifyComplete();
    }

    @Test
    void shouldFailWhenBranchDoesNotExist() {
        when(franchiseGateway.findBranch(FRANCHISE_ID, BRANCH_ID)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, "Papas", 10))
                .expectErrorMatches(businessError(BusinessMessage.BRANCH_NOT_FOUND))
                .verify();

        verify(franchiseGateway, never()).saveProduct(anyString(), anyString(), any());
    }

    @Test
    void shouldFailWhenProductNameAlreadyExistsInBranch() {
        Branch branch = new Branch(BRANCH_ID, "Centro", List.of(new Product("p-1", "Papas", 5)));
        when(franchiseGateway.findBranch(FRANCHISE_ID, BRANCH_ID)).thenReturn(Mono.just(branch));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, "PAPAS", 10))
                .expectErrorMatches(businessError(BusinessMessage.BRANCH_PRODUCT_ALREADY_EXISTS))
                .verify();

        verify(franchiseGateway, never()).saveProduct(anyString(), anyString(), any());
    }

    @Test
    void shouldFailWhenStockIsNegative() {
        Branch branch = new Branch(BRANCH_ID, "Centro", List.of());
        when(franchiseGateway.findBranch(FRANCHISE_ID, BRANCH_ID)).thenReturn(Mono.just(branch));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, "Papas", -1))
                .expectErrorMatches(businessError(BusinessMessage.PRODUCT_STOCK_NEGATIVE))
                .verify();

        verify(franchiseGateway, never()).saveProduct(anyString(), anyString(), any());
    }
}
