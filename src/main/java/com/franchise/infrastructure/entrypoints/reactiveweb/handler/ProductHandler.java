package com.franchise.infrastructure.entrypoints.reactiveweb.handler;

import com.franchise.domain.usecase.product.AddProductUseCase;
import com.franchise.domain.usecase.product.DeleteProductUseCase;
import com.franchise.domain.usecase.product.GetTopStockProductsUseCase;
import com.franchise.domain.usecase.product.UpdateProductNameUseCase;
import com.franchise.domain.usecase.product.UpdateProductStockUseCase;
import com.franchise.infrastructure.entrypoints.reactiveweb.dto.request.CreateProductRequest;
import com.franchise.infrastructure.entrypoints.reactiveweb.dto.request.NameRequest;
import com.franchise.infrastructure.entrypoints.reactiveweb.dto.request.UpdateStockRequest;
import com.franchise.infrastructure.entrypoints.reactiveweb.dto.response.ProductResponse;
import com.franchise.infrastructure.entrypoints.reactiveweb.dto.response.TopStockProductResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class ProductHandler {

    private final AddProductUseCase addProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final UpdateProductStockUseCase updateProductStockUseCase;
    private final UpdateProductNameUseCase updateProductNameUseCase;
    private final GetTopStockProductsUseCase getTopStockProductsUseCase;
    private final RequestValidator requestValidator;

    public ProductHandler(AddProductUseCase addProductUseCase,
                          DeleteProductUseCase deleteProductUseCase,
                          UpdateProductStockUseCase updateProductStockUseCase,
                          UpdateProductNameUseCase updateProductNameUseCase,
                          GetTopStockProductsUseCase getTopStockProductsUseCase,
                          RequestValidator requestValidator) {
        this.addProductUseCase = addProductUseCase;
        this.deleteProductUseCase = deleteProductUseCase;
        this.updateProductStockUseCase = updateProductStockUseCase;
        this.updateProductNameUseCase = updateProductNameUseCase;
        this.getTopStockProductsUseCase = getTopStockProductsUseCase;
        this.requestValidator = requestValidator;
    }

    public Mono<ServerResponse> addProduct(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        String branchId = request.pathVariable("branchId");
        return requestValidator.body(request, CreateProductRequest.class)
                .flatMap(body -> addProductUseCase.execute(franchiseId, branchId, body.name(), body.stock()))
                .map(ProductResponse::from)
                .flatMap(response -> ServerResponse
                        .created(URI.create("/api/v1/franchises/" + franchiseId + "/branches/" + branchId
                                + "/products/" + response.id()))
                        .bodyValue(response));
    }

    public Mono<ServerResponse> deleteProduct(ServerRequest request) {
        return deleteProductUseCase.execute(
                        request.pathVariable("franchiseId"),
                        request.pathVariable("branchId"),
                        request.pathVariable("productId"))
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> updateProductStock(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        String branchId = request.pathVariable("branchId");
        String productId = request.pathVariable("productId");
        return requestValidator.body(request, UpdateStockRequest.class)
                .flatMap(body -> updateProductStockUseCase.execute(franchiseId, branchId, productId, body.stock()))
                .map(ProductResponse::from)
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> updateProductName(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        String branchId = request.pathVariable("branchId");
        String productId = request.pathVariable("productId");
        return requestValidator.body(request, NameRequest.class)
                .flatMap(body -> updateProductNameUseCase.execute(franchiseId, branchId, productId, body.name()))
                .map(ProductResponse::from)
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> getTopStockProducts(ServerRequest request) {
        return getTopStockProductsUseCase.execute(request.pathVariable("franchiseId"))
                .map(TopStockProductResponse::from)
                .collectList()
                .flatMap(products -> ServerResponse.ok().bodyValue(products));
    }
}
