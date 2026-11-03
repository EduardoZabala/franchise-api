package com.franchise.domain.model;

import com.franchise.domain.model.exception.BusinessException;
import com.franchise.domain.model.exception.BusinessMessage;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BranchTest {

    private static void assertBusinessError(ThrowingCallable call, BusinessMessage expected) {
        assertThatThrownBy(call)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getBusinessMessage())
                .isEqualTo(expected);
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        void shouldCreateEmptyBranchWithGeneratedIdAndTrimmedName() {
            Branch branch = Branch.create("  Sucursal Centro  ");

            assertThat(branch.id()).isNotBlank();
            assertThat(branch.name()).isEqualTo("Sucursal Centro");
            assertThat(branch.products()).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        void shouldRejectBlankName(String name) {
            assertBusinessError(() -> Branch.create(name), BusinessMessage.BRANCH_NAME_REQUIRED);
        }

        @Test
        void shouldRejectBlankId() {
            assertBusinessError(() -> new Branch(" ", "Centro", List.of()), BusinessMessage.BRANCH_ID_REQUIRED);
        }

        @Test
        void shouldTreatNullProductsAsEmptyList() {
            assertThat(new Branch("b-1", "Centro", null).products()).isEmpty();
        }

        @Test
        void shouldNotBeAffectedByChangesToTheOriginalList() {
            List<Product> source = new ArrayList<>();
            Branch branch = new Branch("b-1", "Centro", source);

            source.add(Product.create("Papas", 1));

            assertThat(branch.products()).isEmpty();
        }

        @Test
        void shouldExposeUnmodifiableProducts() {
            Branch branch = Branch.create("Centro");

            assertThatThrownBy(() -> branch.products().add(Product.create("Papas", 1)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("findProduct")
    class FindProduct {

        @Test
        void shouldReturnProductWhenIdExists() {
            Product papas = Product.create("Papas", 10);
            Branch branch = Branch.create("Centro").addProduct(papas).addProduct(Product.create("Gaseosa", 5));

            assertThat(branch.findProduct(papas.id())).contains(papas);
        }

        @Test
        void shouldReturnEmptyWhenIdDoesNotExist() {
            Branch branch = Branch.create("Centro").addProduct(Product.create("Papas", 10));

            assertThat(branch.findProduct("unknown-id")).isEmpty();
        }

        @Test
        void shouldReturnEmptyWhenIdIsNull() {
            Branch branch = Branch.create("Centro").addProduct(Product.create("Papas", 10));

            assertThat(branch.findProduct(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("renameProduct")
    class RenameProduct {

        private final Product papas = new Product("p-1", "Papas", 10);
        private final Product gaseosa = new Product("p-2", "Gaseosa", 5);
        private final Branch branch = new Branch("b-1", "Centro", List.of(papas, gaseosa));

        @Test
        void shouldReturnRenamedProduct() {
            assertThat(branch.renameProduct("p-1", "Papas fritas"))
                    .isEqualTo(new Product("p-1", "Papas fritas", 10));
        }

        @Test
        void shouldAllowChangingCaseOfItsOwnName() {
            assertThat(branch.renameProduct("p-1", "PAPAS").name()).isEqualTo("PAPAS");
        }

        @Test
        void shouldRejectNameUsedByAnotherProduct() {
            assertBusinessError(() -> branch.renameProduct("p-1", "gaseosa"),
                    BusinessMessage.BRANCH_PRODUCT_ALREADY_EXISTS);
        }

        @Test
        void shouldRejectUnknownProduct() {
            assertBusinessError(() -> branch.renameProduct("unknown", "Helado"), BusinessMessage.PRODUCT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("productWithMostStock")
    class ProductWithMostStock {

        @Test
        void shouldReturnEmptyWhenBranchHasNoProducts() {
            assertThat(Branch.create("Centro").productWithMostStock()).isEmpty();
        }

        @Test
        void shouldReturnTheOnlyProduct() {
            Product papas = Product.create("Papas", 10);

            assertThat(Branch.create("Centro").addProduct(papas).productWithMostStock()).contains(papas);
        }

        @Test
        void shouldReturnProductWithHighestStock() {
            Product papas = Product.create("Papas", 10);
            Product gaseosa = Product.create("Gaseosa", 50);
            Product helado = Product.create("Helado", 3);
            Branch branch = Branch.create("Centro").addProduct(papas).addProduct(gaseosa).addProduct(helado);

            assertThat(branch.productWithMostStock()).contains(gaseosa);
        }

        @Test
        void shouldReturnFirstAddedProductOnTie() {
            Product papas = Product.create("Papas", 20);
            Product gaseosa = Product.create("Gaseosa", 20);
            Branch branch = Branch.create("Centro").addProduct(papas).addProduct(gaseosa);

            assertThat(branch.productWithMostStock()).contains(papas);
        }
    }

    @Nested
    @DisplayName("rename")
    class Rename {

        @Test
        void shouldReturnNewBranchWithUpdatedNameKeepingIdAndProducts() {
            Product product = Product.create("Papas", 10);
            Branch original = Branch.create("Centro").addProduct(product);

            Branch renamed = original.rename("  Norte ");

            assertThat(renamed.name()).isEqualTo("Norte");
            assertThat(renamed.id()).isEqualTo(original.id());
            assertThat(renamed.products()).containsExactly(product);
            assertThat(original.name()).isEqualTo("Centro");
        }

        @Test
        void shouldRejectBlankName() {
            Branch branch = Branch.create("Centro");

            assertBusinessError(() -> branch.rename(" "), BusinessMessage.BRANCH_NAME_REQUIRED);
        }
    }

    @Nested
    @DisplayName("addProduct")
    class AddProduct {

        @Test
        void shouldReturnNewBranchContainingTheProduct() {
            Branch branch = Branch.create("Centro");
            Product product = Product.create("Papas", 10);

            Branch updated = branch.addProduct(product);

            assertThat(updated.products()).containsExactly(product);
            assertThat(updated.id()).isEqualTo(branch.id());
        }

        @Test
        void shouldNotModifyOriginalBranch() {
            Branch branch = Branch.create("Centro");

            branch.addProduct(Product.create("Papas", 10));

            assertThat(branch.products()).isEmpty();
        }

        @Test
        void shouldKeepExistingProducts() {
            Product papas = Product.create("Papas", 10);
            Product gaseosa = Product.create("Gaseosa", 5);

            Branch updated = Branch.create("Centro").addProduct(papas).addProduct(gaseosa);

            assertThat(updated.products()).containsExactly(papas, gaseosa);
        }

        @Test
        void shouldRejectNullProduct() {
            Branch branch = Branch.create("Centro");

            assertBusinessError(() -> branch.addProduct(null), BusinessMessage.BRANCH_PRODUCT_REQUIRED);
        }

        @Test
        void shouldRejectDuplicateNameIgnoringCase() {
            Branch branch = Branch.create("Centro").addProduct(Product.create("Papas", 10));

            assertBusinessError(() -> branch.addProduct(Product.create("PAPAS", 3)),
                    BusinessMessage.BRANCH_PRODUCT_ALREADY_EXISTS);
        }
    }
}
