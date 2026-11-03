package com.franchise.infrastructure.entrypoints.reactiveweb.router;

import com.franchise.domain.model.Branch;
import com.franchise.domain.model.BranchTopProduct;
import com.franchise.domain.model.Franchise;
import com.franchise.domain.model.Product;
import com.franchise.domain.model.exception.BusinessException;
import com.franchise.domain.model.exception.BusinessMessage;
import com.franchise.domain.usecase.branch.AddBranchUseCase;
import com.franchise.domain.usecase.branch.UpdateBranchNameUseCase;
import com.franchise.domain.usecase.franchise.CreateFranchiseUseCase;
import com.franchise.domain.usecase.franchise.UpdateFranchiseNameUseCase;
import com.franchise.domain.usecase.product.AddProductUseCase;
import com.franchise.domain.usecase.product.DeleteProductUseCase;
import com.franchise.domain.usecase.product.GetTopStockProductsUseCase;
import com.franchise.domain.usecase.product.UpdateProductNameUseCase;
import com.franchise.domain.usecase.product.UpdateProductStockUseCase;
import com.franchise.infrastructure.entrypoints.reactiveweb.error.ApiErrorHandler;
import com.franchise.infrastructure.entrypoints.reactiveweb.handler.BranchHandler;
import com.franchise.infrastructure.entrypoints.reactiveweb.handler.FranchiseHandler;
import com.franchise.infrastructure.entrypoints.reactiveweb.handler.ProductHandler;
import com.franchise.infrastructure.entrypoints.reactiveweb.handler.RequestValidator;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Web layer test: routes, status codes, JSON contract, validation and error mapping.
 * Use cases are mocked; their logic is covered by their own tests.
 */
@ExtendWith(MockitoExtension.class)
class FranchiseRouterTest {

    private static final String BASE = "/api/v1/franchises";

    @Mock private CreateFranchiseUseCase createFranchiseUseCase;
    @Mock private UpdateFranchiseNameUseCase updateFranchiseNameUseCase;
    @Mock private AddBranchUseCase addBranchUseCase;
    @Mock private UpdateBranchNameUseCase updateBranchNameUseCase;
    @Mock private AddProductUseCase addProductUseCase;
    @Mock private DeleteProductUseCase deleteProductUseCase;
    @Mock private UpdateProductStockUseCase updateProductStockUseCase;
    @Mock private UpdateProductNameUseCase updateProductNameUseCase;
    @Mock private GetTopStockProductsUseCase getTopStockProductsUseCase;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        RequestValidator validator = new RequestValidator(
                Validation.buildDefaultValidatorFactory().getValidator());
        var routes = new FranchiseRouter().franchiseRoutes(
                new FranchiseHandler(createFranchiseUseCase, updateFranchiseNameUseCase, validator),
                new BranchHandler(addBranchUseCase, updateBranchNameUseCase, validator),
                new ProductHandler(addProductUseCase, deleteProductUseCase, updateProductStockUseCase,
                        updateProductNameUseCase, getTopStockProductsUseCase, validator),
                new ApiErrorHandler());
        client = WebTestClient.bindToRouterFunction(routes).build();
    }

    @Nested
    @DisplayName("POST /franchises")
    class CreateFranchise {

        @Test
        void shouldReturn201WithCreatedFranchise() {
            when(createFranchiseUseCase.execute("Burger House"))
                    .thenReturn(Mono.just(new Franchise("f-1", "Burger House", List.of())));

            client.post().uri(BASE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("name", "Burger House"))
                    .exchange()
                    .expectStatus().isCreated()
                    .expectHeader().location(BASE + "/f-1")
                    .expectBody()
                    .jsonPath("$.id").isEqualTo("f-1")
                    .jsonPath("$.name").isEqualTo("Burger House");
        }

        @Test
        void shouldReturn400WhenNameIsBlank() {
            client.post().uri(BASE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("name", " "))
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("REQ-001")
                    .jsonPath("$.details[0]").isEqualTo("name: must not be blank");

            verify(createFranchiseUseCase, never()).execute(anyString());
        }

        @Test
        void shouldReturn400WhenBodyIsMissing() {
            client.post().uri(BASE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("REQ-001");
        }

        @Test
        void shouldReturn400WhenJsonIsMalformed() {
            client.post().uri(BASE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{ \"name\": ")
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("REQ-002");
        }

        @Test
        void shouldReturn500WithoutLeakingDetailsOnUnexpectedError() {
            when(createFranchiseUseCase.execute("Burger House"))
                    .thenReturn(Mono.error(new IllegalStateException("DynamoDB connection refused")));

            client.post().uri(BASE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("name", "Burger House"))
                    .exchange()
                    .expectStatus().is5xxServerError()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("ERR-500")
                    .jsonPath("$.message").isEqualTo("Unexpected error");
        }
    }

    @Nested
    @DisplayName("PATCH /franchises/{id}")
    class UpdateFranchiseName {

        @Test
        void shouldReturn200WithRenamedFranchise() {
            when(updateFranchiseNameUseCase.execute("f-1", "Burger Palace"))
                    .thenReturn(Mono.just(new Franchise("f-1", "Burger Palace", List.of())));

            client.patch().uri(BASE + "/f-1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("name", "Burger Palace"))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.name").isEqualTo("Burger Palace");
        }

        @Test
        void shouldReturn404WhenFranchiseDoesNotExist() {
            when(updateFranchiseNameUseCase.execute("f-x", "Burger Palace"))
                    .thenReturn(Mono.error(new BusinessException(BusinessMessage.FRANCHISE_NOT_FOUND)));

            client.patch().uri(BASE + "/f-x")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("name", "Burger Palace"))
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("FRN-005")
                    .jsonPath("$.message").isEqualTo("Franchise not found");
        }
    }

    @Nested
    @DisplayName("Branches")
    class Branches {

        @Test
        void shouldReturn201WhenBranchIsAdded() {
            when(addBranchUseCase.execute("f-1", "Centro"))
                    .thenReturn(Mono.just(new Branch("b-1", "Centro", List.of())));

            client.post().uri(BASE + "/f-1/branches")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("name", "Centro"))
                    .exchange()
                    .expectStatus().isCreated()
                    .expectHeader().location(BASE + "/f-1/branches/b-1")
                    .expectBody()
                    .jsonPath("$.id").isEqualTo("b-1")
                    .jsonPath("$.name").isEqualTo("Centro");
        }

        @Test
        void shouldReturn409WhenBranchNameAlreadyExists() {
            when(addBranchUseCase.execute("f-1", "Centro"))
                    .thenReturn(Mono.error(new BusinessException(BusinessMessage.FRANCHISE_BRANCH_ALREADY_EXISTS)));

            client.post().uri(BASE + "/f-1/branches")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("name", "Centro"))
                    .exchange()
                    .expectStatus().isEqualTo(409)
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("FRN-004");
        }

        @Test
        void shouldReturn200WhenBranchIsRenamed() {
            when(updateBranchNameUseCase.execute("f-1", "b-1", "Norte"))
                    .thenReturn(Mono.just(new Branch("b-1", "Norte", List.of())));

            client.patch().uri(BASE + "/f-1/branches/b-1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("name", "Norte"))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.name").isEqualTo("Norte");
        }
    }

    @Nested
    @DisplayName("Products")
    class Products {

        private static final String PRODUCTS = BASE + "/f-1/branches/b-1/products";

        @Test
        void shouldReturn201WhenProductIsAdded() {
            when(addProductUseCase.execute("f-1", "b-1", "Papas", 10))
                    .thenReturn(Mono.just(new Product("p-1", "Papas", 10)));

            client.post().uri(PRODUCTS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("name", "Papas", "stock", 10))
                    .exchange()
                    .expectStatus().isCreated()
                    .expectHeader().location(PRODUCTS + "/p-1")
                    .expectBody()
                    .jsonPath("$.id").isEqualTo("p-1")
                    .jsonPath("$.stock").isEqualTo(10);
        }

        @Test
        void shouldReturn400WithAllFieldErrors() {
            client.post().uri(PRODUCTS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("name", "", "stock", -5))
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.details.length()").isEqualTo(2);
        }

        @Test
        void shouldReturn204WhenProductIsDeleted() {
            when(deleteProductUseCase.execute("f-1", "b-1", "p-1")).thenReturn(Mono.empty());

            client.delete().uri(PRODUCTS + "/p-1")
                    .exchange()
                    .expectStatus().isNoContent()
                    .expectBody().isEmpty();
        }

        @Test
        void shouldReturn404WhenDeletingUnknownProduct() {
            when(deleteProductUseCase.execute("f-1", "b-1", "p-x"))
                    .thenReturn(Mono.error(new BusinessException(BusinessMessage.PRODUCT_NOT_FOUND)));

            client.delete().uri(PRODUCTS + "/p-x")
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("PRD-004");
        }

        @Test
        void shouldReturn200WhenStockIsUpdated() {
            when(updateProductStockUseCase.execute("f-1", "b-1", "p-1", 40))
                    .thenReturn(Mono.just(new Product("p-1", "Papas", 40)));

            client.patch().uri(PRODUCTS + "/p-1/stock")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("stock", 40))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.stock").isEqualTo(40);
        }

        @Test
        void shouldReturn400WhenStockIsMissing() {
            client.patch().uri(PRODUCTS + "/p-1/stock")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of())
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.details[0]").isEqualTo("stock: must not be null");
        }

        @Test
        void shouldReturn200WhenProductIsRenamed() {
            when(updateProductNameUseCase.execute("f-1", "b-1", "p-1", "Papas fritas"))
                    .thenReturn(Mono.just(new Product("p-1", "Papas fritas", 10)));

            client.patch().uri(PRODUCTS + "/p-1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("name", "Papas fritas"))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.name").isEqualTo("Papas fritas");
        }
    }

    @Nested
    @DisplayName("GET /franchises/{id}/products/top-stock")
    class TopStock {

        @Test
        void shouldReturnTopProductPerBranch() {
            when(getTopStockProductsUseCase.execute("f-1")).thenReturn(Flux.just(
                    new BranchTopProduct("b-1", "Centro", new Product("p-2", "Gaseosa", 50)),
                    new BranchTopProduct("b-2", "Norte", new Product("p-3", "Helado", 30))));

            client.get().uri(BASE + "/f-1/products/top-stock")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.length()").isEqualTo(2)
                    .jsonPath("$[0].branchId").isEqualTo("b-1")
                    .jsonPath("$[0].branchName").isEqualTo("Centro")
                    .jsonPath("$[0].productId").isEqualTo("p-2")
                    .jsonPath("$[0].productName").isEqualTo("Gaseosa")
                    .jsonPath("$[0].stock").isEqualTo(50)
                    .jsonPath("$[1].branchName").isEqualTo("Norte");
        }

        @Test
        void shouldReturnEmptyListWhenNoBranchHasProducts() {
            when(getTopStockProductsUseCase.execute("f-1")).thenReturn(Flux.empty());

            client.get().uri(BASE + "/f-1/products/top-stock")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .json("[]");
        }

        @Test
        void shouldReturn404WhenFranchiseDoesNotExist() {
            when(getTopStockProductsUseCase.execute("f-x"))
                    .thenReturn(Flux.error(new BusinessException(BusinessMessage.FRANCHISE_NOT_FOUND)));

            client.get().uri(BASE + "/f-x/products/top-stock")
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }
}
