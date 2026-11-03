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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteProductUseCaseTest {

    private static final String FRANCHISE_ID = "f-1";
    private static final String BRANCH_ID = "b-1";
    private static final String PRODUCT_ID = "p-1";

    @Mock
    private FranchiseGateway franchiseGateway;

    private DeleteProductUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteProductUseCase(franchiseGateway);
    }

    @Test
    void shouldDeleteExistingProduct() {
        Branch branch = new Branch(BRANCH_ID, "Centro", List.of(new Product(PRODUCT_ID, "Papas", 5)));
        when(franchiseGateway.findBranch(FRANCHISE_ID, BRANCH_ID)).thenReturn(Mono.just(branch));
        when(franchiseGateway.deleteProduct(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID))
                .verifyComplete();

        verify(franchiseGateway).deleteProduct(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID);
    }

    @Test
    void shouldFailWhenBranchDoesNotExist() {
        when(franchiseGateway.findBranch(FRANCHISE_ID, BRANCH_ID)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID))
                .expectErrorMatches(businessError(BusinessMessage.BRANCH_NOT_FOUND))
                .verify();

        verify(franchiseGateway, never()).deleteProduct(anyString(), anyString(), anyString());
    }

    @Test
    void shouldFailWhenProductDoesNotExist() {
        Branch branch = new Branch(BRANCH_ID, "Centro", List.of());
        when(franchiseGateway.findBranch(FRANCHISE_ID, BRANCH_ID)).thenReturn(Mono.just(branch));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID))
                .expectErrorMatches(businessError(BusinessMessage.PRODUCT_NOT_FOUND))
                .verify();

        verify(franchiseGateway, never()).deleteProduct(anyString(), anyString(), anyString());
    }
}
