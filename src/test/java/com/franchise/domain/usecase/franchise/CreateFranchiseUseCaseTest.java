package com.franchise.domain.usecase.franchise;

import com.franchise.domain.model.Franchise;
import com.franchise.domain.model.exception.BusinessException;
import com.franchise.domain.model.exception.BusinessMessage;
import com.franchise.domain.model.gateways.FranchiseGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateFranchiseUseCaseTest {

    @Mock
    private FranchiseGateway franchiseGateway;

    private CreateFranchiseUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateFranchiseUseCase(franchiseGateway);
    }

    @Test
    void shouldCreateAndSaveFranchise() {
        when(franchiseGateway.saveFranchise(any(Franchise.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(useCase.execute("  Burger House  "))
                .assertNext(franchise -> {
                    assertThat(franchise.id()).isNotBlank();
                    assertThat(franchise.name()).isEqualTo("Burger House");
                    assertThat(franchise.branches()).isEmpty();
                })
                .verifyComplete();
    }

    @Test
    void shouldSendTheCreatedFranchiseToTheGateway() {
        ArgumentCaptor<Franchise> captor = ArgumentCaptor.forClass(Franchise.class);
        when(franchiseGateway.saveFranchise(captor.capture()))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(useCase.execute("Burger House"))
                .expectNextCount(1)
                .verifyComplete();

        assertThat(captor.getValue().name()).isEqualTo("Burger House");
    }

    @Test
    void shouldEmitBusinessErrorAndNotSaveWhenNameIsBlank() {
        StepVerifier.create(useCase.execute(" "))
                .expectErrorSatisfies(error -> assertThat(error)
                        .isInstanceOf(BusinessException.class)
                        .extracting(e -> ((BusinessException) e).getBusinessMessage())
                        .isEqualTo(BusinessMessage.FRANCHISE_NAME_REQUIRED))
                .verify();

        verify(franchiseGateway, never()).saveFranchise(any());
    }

    @Test
    void shouldPropagateGatewayErrors() {
        when(franchiseGateway.saveFranchise(any(Franchise.class)))
                .thenReturn(Mono.error(new IllegalStateException("DynamoDB unavailable")));

        StepVerifier.create(useCase.execute("Burger House"))
                .expectErrorMessage("DynamoDB unavailable")
                .verify();
    }
}
