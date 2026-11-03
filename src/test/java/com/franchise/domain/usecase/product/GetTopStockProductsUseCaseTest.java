package com.franchise.domain.usecase.product;

import com.franchise.domain.model.Branch;
import com.franchise.domain.model.BranchTopProduct;
import com.franchise.domain.model.Franchise;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTopStockProductsUseCaseTest {

    private static final String FRANCHISE_ID = "f-1";

    @Mock
    private FranchiseGateway franchiseGateway;

    private GetTopStockProductsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetTopStockProductsUseCase(franchiseGateway);
    }

    @Test
    void shouldReturnTopProductOfEachBranchSkippingEmptyBranches() {
        Product gaseosa = new Product("p-2", "Gaseosa", 50);
        Product helado = new Product("p-3", "Helado", 30);
        Branch centro = new Branch("b-1", "Centro", List.of(new Product("p-1", "Papas", 10), gaseosa));
        Branch vacia = new Branch("b-2", "Vacia", List.of());
        Branch norte = new Branch("b-3", "Norte", List.of(helado));
        Franchise franchise = new Franchise(FRANCHISE_ID, "Burger House", List.of(centro, vacia, norte));
        when(franchiseGateway.findFranchiseById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));

        StepVerifier.create(useCase.execute(FRANCHISE_ID))
                .expectNext(new BranchTopProduct("b-1", "Centro", gaseosa))
                .expectNext(new BranchTopProduct("b-3", "Norte", helado))
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenFranchiseHasNoBranches() {
        Franchise franchise = new Franchise(FRANCHISE_ID, "Burger House", List.of());
        when(franchiseGateway.findFranchiseById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));

        StepVerifier.create(useCase.execute(FRANCHISE_ID))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenFranchiseDoesNotExist() {
        when(franchiseGateway.findFranchiseById(FRANCHISE_ID)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(FRANCHISE_ID))
                .expectErrorMatches(businessError(BusinessMessage.FRANCHISE_NOT_FOUND))
                .verify();
    }
}
