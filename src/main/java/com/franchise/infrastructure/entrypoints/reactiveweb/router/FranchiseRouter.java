package com.franchise.infrastructure.entrypoints.reactiveweb.router;

import com.franchise.infrastructure.entrypoints.reactiveweb.dto.request.CreateProductRequest;
import com.franchise.infrastructure.entrypoints.reactiveweb.dto.request.NameRequest;
import com.franchise.infrastructure.entrypoints.reactiveweb.dto.request.UpdateStockRequest;
import com.franchise.infrastructure.entrypoints.reactiveweb.dto.response.BranchResponse;
import com.franchise.infrastructure.entrypoints.reactiveweb.dto.response.ErrorResponse;
import com.franchise.infrastructure.entrypoints.reactiveweb.dto.response.FranchiseResponse;
import com.franchise.infrastructure.entrypoints.reactiveweb.dto.response.ProductResponse;
import com.franchise.infrastructure.entrypoints.reactiveweb.dto.response.TopStockProductResponse;
import com.franchise.infrastructure.entrypoints.reactiveweb.error.ApiErrorHandler;
import com.franchise.infrastructure.entrypoints.reactiveweb.handler.BranchHandler;
import com.franchise.infrastructure.entrypoints.reactiveweb.handler.FranchiseHandler;
import com.franchise.infrastructure.entrypoints.reactiveweb.handler.ProductHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * HTTP routes of the API (WebFlux functional endpoints).
 * The {@link RouterOperations} annotations only describe the routes for OpenAPI / Swagger UI.
 */
@Configuration
public class FranchiseRouter {

    private static final String FRANCHISES = "/api/v1/franchises";
    private static final String FRANCHISE = FRANCHISES + "/{franchiseId}";
    private static final String BRANCHES = FRANCHISE + "/branches";
    private static final String BRANCH = BRANCHES + "/{branchId}";
    private static final String PRODUCTS = BRANCH + "/products";
    private static final String PRODUCT = PRODUCTS + "/{productId}";
    private static final String PRODUCT_STOCK = PRODUCT + "/stock";
    private static final String TOP_STOCK = FRANCHISE + "/products/top-stock";

    private static final String TAG_FRANCHISES = "Franchises";
    private static final String TAG_BRANCHES = "Branches";
    private static final String TAG_PRODUCTS = "Products";

    @Bean
    @RouterOperations({
            @RouterOperation(path = FRANCHISES, method = RequestMethod.POST,
                    beanClass = FranchiseHandler.class, beanMethod = "createFranchise",
                    operation = @Operation(operationId = "createFranchise", tags = TAG_FRANCHISES,
                            summary = "Create a franchise",
                            requestBody = @RequestBody(required = true,
                                    content = @Content(schema = @Schema(implementation = NameRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Franchise created",
                                            content = @Content(schema = @Schema(implementation = FranchiseResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Invalid request",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),
            @RouterOperation(path = FRANCHISE, method = RequestMethod.PATCH,
                    beanClass = FranchiseHandler.class, beanMethod = "updateFranchiseName",
                    operation = @Operation(operationId = "updateFranchiseName", tags = TAG_FRANCHISES,
                            summary = "Rename a franchise",
                            parameters = @Parameter(in = ParameterIn.PATH, name = "franchiseId", required = true),
                            requestBody = @RequestBody(required = true,
                                    content = @Content(schema = @Schema(implementation = NameRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Franchise renamed",
                                            content = @Content(schema = @Schema(implementation = FranchiseResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Invalid request",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Franchise not found",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),
            @RouterOperation(path = BRANCHES, method = RequestMethod.POST,
                    beanClass = BranchHandler.class, beanMethod = "addBranch",
                    operation = @Operation(operationId = "addBranch", tags = TAG_BRANCHES,
                            summary = "Add a branch to a franchise",
                            parameters = @Parameter(in = ParameterIn.PATH, name = "franchiseId", required = true),
                            requestBody = @RequestBody(required = true,
                                    content = @Content(schema = @Schema(implementation = NameRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Branch created",
                                            content = @Content(schema = @Schema(implementation = BranchResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Invalid request",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Franchise not found",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "409", description = "Branch name already exists",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),
            @RouterOperation(path = BRANCH, method = RequestMethod.PATCH,
                    beanClass = BranchHandler.class, beanMethod = "updateBranchName",
                    operation = @Operation(operationId = "updateBranchName", tags = TAG_BRANCHES,
                            summary = "Rename a branch",
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "franchiseId", required = true),
                                    @Parameter(in = ParameterIn.PATH, name = "branchId", required = true)
                            },
                            requestBody = @RequestBody(required = true,
                                    content = @Content(schema = @Schema(implementation = NameRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Branch renamed",
                                            content = @Content(schema = @Schema(implementation = BranchResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Invalid request",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Franchise or branch not found",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "409", description = "Branch name already exists",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),
            @RouterOperation(path = PRODUCTS, method = RequestMethod.POST,
                    beanClass = ProductHandler.class, beanMethod = "addProduct",
                    operation = @Operation(operationId = "addProduct", tags = TAG_PRODUCTS,
                            summary = "Add a product to a branch",
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "franchiseId", required = true),
                                    @Parameter(in = ParameterIn.PATH, name = "branchId", required = true)
                            },
                            requestBody = @RequestBody(required = true,
                                    content = @Content(schema = @Schema(implementation = CreateProductRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Product created",
                                            content = @Content(schema = @Schema(implementation = ProductResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Invalid request",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Branch not found",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "409", description = "Product name already exists",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),
            @RouterOperation(path = PRODUCT, method = RequestMethod.DELETE,
                    beanClass = ProductHandler.class, beanMethod = "deleteProduct",
                    operation = @Operation(operationId = "deleteProduct", tags = TAG_PRODUCTS,
                            summary = "Delete a product from a branch",
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "franchiseId", required = true),
                                    @Parameter(in = ParameterIn.PATH, name = "branchId", required = true),
                                    @Parameter(in = ParameterIn.PATH, name = "productId", required = true)
                            },
                            responses = {
                                    @ApiResponse(responseCode = "204", description = "Product deleted",
                                            content = @Content),
                                    @ApiResponse(responseCode = "404", description = "Branch or product not found",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),
            @RouterOperation(path = PRODUCT_STOCK, method = RequestMethod.PATCH,
                    beanClass = ProductHandler.class, beanMethod = "updateProductStock",
                    operation = @Operation(operationId = "updateProductStock", tags = TAG_PRODUCTS,
                            summary = "Change the stock of a product",
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "franchiseId", required = true),
                                    @Parameter(in = ParameterIn.PATH, name = "branchId", required = true),
                                    @Parameter(in = ParameterIn.PATH, name = "productId", required = true)
                            },
                            requestBody = @RequestBody(required = true,
                                    content = @Content(schema = @Schema(implementation = UpdateStockRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Stock updated",
                                            content = @Content(schema = @Schema(implementation = ProductResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Invalid request",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Branch or product not found",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),
            @RouterOperation(path = PRODUCT, method = RequestMethod.PATCH,
                    beanClass = ProductHandler.class, beanMethod = "updateProductName",
                    operation = @Operation(operationId = "updateProductName", tags = TAG_PRODUCTS,
                            summary = "Rename a product",
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "franchiseId", required = true),
                                    @Parameter(in = ParameterIn.PATH, name = "branchId", required = true),
                                    @Parameter(in = ParameterIn.PATH, name = "productId", required = true)
                            },
                            requestBody = @RequestBody(required = true,
                                    content = @Content(schema = @Schema(implementation = NameRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Product renamed",
                                            content = @Content(schema = @Schema(implementation = ProductResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Invalid request",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Branch or product not found",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "409", description = "Product name already exists",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),
            @RouterOperation(path = TOP_STOCK, method = RequestMethod.GET,
                    beanClass = ProductHandler.class, beanMethod = "getTopStockProducts",
                    operation = @Operation(operationId = "getTopStockProducts", tags = TAG_PRODUCTS,
                            summary = "Product with the most stock of each branch of a franchise",
                            description = "Branches without products are omitted.",
                            parameters = @Parameter(in = ParameterIn.PATH, name = "franchiseId", required = true),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "One product per branch",
                                            content = @Content(array = @ArraySchema(
                                                    schema = @Schema(implementation = TopStockProductResponse.class)))),
                                    @ApiResponse(responseCode = "404", description = "Franchise not found",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            }))
    })
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
                .PATCH(PRODUCT_STOCK, productHandler::updateProductStock)
                .PATCH(PRODUCT, productHandler::updateProductName)
                .GET(TOP_STOCK, productHandler::getTopStockProducts)
                .onError(Throwable.class, errorHandler::handle)
                .build();
    }
}
