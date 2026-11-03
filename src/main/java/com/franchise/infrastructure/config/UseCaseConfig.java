package com.franchise.infrastructure.config;

import com.franchise.domain.model.gateways.FranchiseGateway;
import com.franchise.domain.usecase.branch.AddBranchUseCase;
import com.franchise.domain.usecase.branch.UpdateBranchNameUseCase;
import com.franchise.domain.usecase.franchise.CreateFranchiseUseCase;
import com.franchise.domain.usecase.franchise.UpdateFranchiseNameUseCase;
import com.franchise.domain.usecase.product.AddProductUseCase;
import com.franchise.domain.usecase.product.DeleteProductUseCase;
import com.franchise.domain.usecase.product.GetTopStockProductsUseCase;
import com.franchise.domain.usecase.product.UpdateProductNameUseCase;
import com.franchise.domain.usecase.product.UpdateProductStockUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the use cases as Spring beans so the domain stays free of framework annotations.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public CreateFranchiseUseCase createFranchiseUseCase(FranchiseGateway gateway) {
        return new CreateFranchiseUseCase(gateway);
    }

    @Bean
    public UpdateFranchiseNameUseCase updateFranchiseNameUseCase(FranchiseGateway gateway) {
        return new UpdateFranchiseNameUseCase(gateway);
    }

    @Bean
    public AddBranchUseCase addBranchUseCase(FranchiseGateway gateway) {
        return new AddBranchUseCase(gateway);
    }

    @Bean
    public UpdateBranchNameUseCase updateBranchNameUseCase(FranchiseGateway gateway) {
        return new UpdateBranchNameUseCase(gateway);
    }

    @Bean
    public AddProductUseCase addProductUseCase(FranchiseGateway gateway) {
        return new AddProductUseCase(gateway);
    }

    @Bean
    public DeleteProductUseCase deleteProductUseCase(FranchiseGateway gateway) {
        return new DeleteProductUseCase(gateway);
    }

    @Bean
    public UpdateProductStockUseCase updateProductStockUseCase(FranchiseGateway gateway) {
        return new UpdateProductStockUseCase(gateway);
    }

    @Bean
    public UpdateProductNameUseCase updateProductNameUseCase(FranchiseGateway gateway) {
        return new UpdateProductNameUseCase(gateway);
    }

    @Bean
    public GetTopStockProductsUseCase getTopStockProductsUseCase(FranchiseGateway gateway) {
        return new GetTopStockProductsUseCase(gateway);
    }
}
