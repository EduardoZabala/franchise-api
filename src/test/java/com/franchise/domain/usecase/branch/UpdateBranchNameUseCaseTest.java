package com.franchise.domain.usecase.branch;

import com.franchise.domain.model.Branch;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateBranchNameUseCaseTest {

    private static final String FRANCHISE_ID = "f-1";
    private static final String BRANCH_ID = "b-1";

    @Mock
    private FranchiseGateway franchiseGateway;

    private UpdateBranchNameUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateBranchNameUseCase(franchiseGateway);
    }

    @Test
    void shouldSaveRenamedBranch() {
        List<Product> products = List.of(new Product("p-1", "Papas", 5));
        Franchise franchise = new Franchise(FRANCHISE_ID, "Burger House",
                List.of(new Branch(BRANCH_ID, "Centro", products)));
        when(franchiseGateway.findFranchiseById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));
        when(franchiseGateway.saveBranch(eq(FRANCHISE_ID), any(Branch.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(1)));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, "Norte"))
                .expectNext(new Branch(BRANCH_ID, "Norte", products))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenFranchiseDoesNotExist() {
        when(franchiseGateway.findFranchiseById(FRANCHISE_ID)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, "Norte"))
                .expectErrorMatches(businessError(BusinessMessage.FRANCHISE_NOT_FOUND))
                .verify();
    }

    @Test
    void shouldFailWhenBranchDoesNotExist() {
        Franchise franchise = new Franchise(FRANCHISE_ID, "Burger House", List.of());
        when(franchiseGateway.findFranchiseById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, "Norte"))
                .expectErrorMatches(businessError(BusinessMessage.BRANCH_NOT_FOUND))
                .verify();
    }

    @Test
    void shouldFailWhenNameIsUsedByAnotherBranch() {
        Franchise franchise = new Franchise(FRANCHISE_ID, "Burger House", List.of(
                new Branch(BRANCH_ID, "Centro", List.of()),
                new Branch("b-2", "Norte", List.of())));
        when(franchiseGateway.findFranchiseById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, "NORTE"))
                .expectErrorMatches(businessError(BusinessMessage.FRANCHISE_BRANCH_ALREADY_EXISTS))
                .verify();

        verify(franchiseGateway, never()).saveBranch(anyString(), any());
    }
}
