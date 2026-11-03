package com.franchise.domain.usecase.branch;

import com.franchise.domain.model.Branch;
import com.franchise.domain.model.Franchise;
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
class AddBranchUseCaseTest {

    private static final String FRANCHISE_ID = "f-1";

    @Mock
    private FranchiseGateway franchiseGateway;

    private AddBranchUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AddBranchUseCase(franchiseGateway);
    }

    @Test
    void shouldSaveNewBranchInFranchise() {
        Franchise franchise = new Franchise(FRANCHISE_ID, "Burger House", List.of());
        when(franchiseGateway.findFranchiseById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));
        when(franchiseGateway.saveBranch(eq(FRANCHISE_ID), any(Branch.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(1)));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, "Centro"))
                .assertNext(branch -> {
                    assertThat(branch.id()).isNotBlank();
                    assertThat(branch.name()).isEqualTo("Centro");
                    assertThat(branch.products()).isEmpty();
                })
                .verifyComplete();
    }

    @Test
    void shouldFailWhenFranchiseDoesNotExist() {
        when(franchiseGateway.findFranchiseById(FRANCHISE_ID)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(FRANCHISE_ID, "Centro"))
                .expectErrorMatches(businessError(BusinessMessage.FRANCHISE_NOT_FOUND))
                .verify();

        verify(franchiseGateway, never()).saveBranch(anyString(), any());
    }

    @Test
    void shouldFailWhenBranchNameAlreadyExists() {
        Franchise franchise = new Franchise(FRANCHISE_ID, "Burger House",
                List.of(new Branch("b-1", "Centro", List.of())));
        when(franchiseGateway.findFranchiseById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, "centro"))
                .expectErrorMatches(businessError(BusinessMessage.FRANCHISE_BRANCH_ALREADY_EXISTS))
                .verify();

        verify(franchiseGateway, never()).saveBranch(anyString(), any());
    }
}
