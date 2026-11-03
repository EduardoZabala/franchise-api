package com.franchise.infrastructure.entrypoints.reactiveweb.router;

import com.franchise.infrastructure.entrypoints.reactiveweb.error.ApiErrorHandler;
import com.franchise.infrastructure.entrypoints.reactiveweb.handler.BranchHandler;
import com.franchise.infrastructure.entrypoints.reactiveweb.handler.FranchiseHandler;
import com.franchise.infrastructure.entrypoints.reactiveweb.handler.ProductHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * HTTP routes of the API (WebFlux functional endpoints).
 */
@Configuration
public class FranchiseRouter {

    private static final String FRANCHISES = "/api/v1/franchises";
    private static final String FRANCHISE = FRANCHISES + "/{franchiseId}";
    private static final String BRANCHES = FRANCHISE + "/branches";
    private static final String BRANCH = BRANCHES + "/{branchId}";
    private static final String PRODUCTS = BRANCH + "/products";
    private static final String PRODUCT = PRODUCTS + "/{productId}";

    @Bean
    public RouterFunction<ServerResponse> franchiseRoutes(FranchiseHandler franchiseHandler,
                                                          BranchHandler branchHandler,
                                                          ProductHandler productHandler,
                                                          ApiErrorHandler errorHandler) {
        return route()
                .POST(FRANCHISES, franchiseHandler::createFranchise)
                .PATCH(FRANCHISE, franchiseHandler::updateFranchiseName)
                .POST(BRANCHES, branchHandler::addBranch)
                .PATCH(BRANCH, branchHandler::updateBranchName)
                .POST(PRODUCTS, productHandler::addProduct)
                .DELETE(PRODUCT, productHandler::deleteProduct)
                .PATCH(PRODUCT + "/stock", productHandler::updateProductStock)
                .PATCH(PRODUCT, productHandler::updateProductName)
                .GET(FRANCHISE + "/products/top-stock", productHandler::getTopStockProducts)
                .onError(Throwable.class, errorHandler::handle)
                .build();
    }
}
