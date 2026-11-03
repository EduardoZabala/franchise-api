package com.franchise.domain.usecase.franchise;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateFranchiseNameUseCaseTest {

    private static final String FRANCHISE_ID = "f-1";

    @Mock
    private FranchiseGateway franchiseGateway;

    private UpdateFranchiseNameUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateFranchiseNameUseCase(franchiseGateway);
    }

    @Test
    void shouldSaveRenamedFranchise() {
        when(franchiseGateway.findFranchiseById(FRANCHISE_ID))
                .thenReturn(Mono.just(new Franchise(FRANCHISE_ID, "Burger House", List.of())));
        when(franchiseGateway.saveFranchise(any(Franchise.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, "Burger Palace"))
                .expectNext(new Franchise(FRANCHISE_ID, "Burger Palace", List.of()))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenFranchiseDoesNotExist() {
        when(franchiseGateway.findFranchiseById(FRANCHISE_ID)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(FRANCHISE_ID, "Burger Palace"))
                .expectErrorMatches(businessError(BusinessMessage.FRANCHISE_NOT_FOUND))
                .verify();

        verify(franchiseGateway, never()).saveFranchise(any());
    }

    @Test
    void shouldFailWhenNewNameIsBlank() {
        when(franchiseGateway.findFranchiseById(FRANCHISE_ID))
                .thenReturn(Mono.just(new Franchise(FRANCHISE_ID, "Burger House", List.of())));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, " "))
                .expectErrorMatches(businessError(BusinessMessage.FRANCHISE_NAME_REQUIRED))
                .verify();

        verify(franchiseGateway, never()).saveFranchise(any());
    }
}
