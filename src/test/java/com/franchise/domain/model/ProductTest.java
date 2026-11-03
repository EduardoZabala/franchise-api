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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

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
        void shouldCreateProductWithGeneratedIdAndTrimmedName() {
            Product product = Product.create("  Hamburguesa  ", 10);

            assertThat(product.id()).isNotBlank();
            assertThat(product.name()).isEqualTo("Hamburguesa");
            assertThat(product.stock()).isEqualTo(10);
        }

        @Test
        void shouldGenerateDifferentIds() {
            assertThat(Product.create("A", 1).id()).isNotEqualTo(Product.create("A", 1).id());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        void shouldRejectBlankName(String name) {
            assertBusinessError(() -> Product.create(name, 1), BusinessMessage.PRODUCT_NAME_REQUIRED);
        }

        @Test
        void shouldRejectNegativeStock() {
            assertBusinessError(() -> Product.create("Papas", -1), BusinessMessage.PRODUCT_STOCK_NEGATIVE);
        }

        @Test
        void shouldAcceptZeroStock() {
            assertThat(Product.create("Papas", 0).stock()).isZero();
        }

        @Test
        void shouldRejectBlankId() {
            assertBusinessError(() -> new Product(" ", "Papas", 1), BusinessMessage.PRODUCT_ID_REQUIRED);
        }
    }

    @Nested
    @DisplayName("withStock")
    class WithStock {

        @Test
        void shouldReturnNewInstanceWithUpdatedStock() {
            Product original = Product.create("Papas", 5);

            Product updated = original.withStock(20);

            assertThat(updated.stock()).isEqualTo(20);
            assertThat(updated.id()).isEqualTo(original.id());
            assertThat(original.stock()).isEqualTo(5);
        }

        @Test
        void shouldRejectNegativeStock() {
            Product product = Product.create("Papas", 5);

            assertBusinessError(() -> product.withStock(-3), BusinessMessage.PRODUCT_STOCK_NEGATIVE);
        }
    }

    @Nested
    @DisplayName("rename")
    class Rename {

        @Test
        void shouldReturnNewInstanceWithUpdatedName() {
            Product original = Product.create("Papas", 5);

            Product renamed = original.rename("Papas fritas");

            assertThat(renamed.name()).isEqualTo("Papas fritas");
            assertThat(renamed.id()).isEqualTo(original.id());
            assertThat(original.name()).isEqualTo("Papas");
        }

        @Test
        void shouldRejectBlankName() {
            Product product = Product.create("Papas", 5);

            assertBusinessError(() -> product.rename(""), BusinessMessage.PRODUCT_NAME_REQUIRED);
        }
    }
}
