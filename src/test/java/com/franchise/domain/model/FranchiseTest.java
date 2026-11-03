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

class FranchiseTest {

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
        void shouldCreateEmptyFranchiseWithGeneratedIdAndTrimmedName() {
            Franchise franchise = Franchise.create("  Burger House  ");

            assertThat(franchise.id()).isNotBlank();
            assertThat(franchise.name()).isEqualTo("Burger House");
            assertThat(franchise.branches()).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        void shouldRejectBlankName(String name) {
            assertBusinessError(() -> Franchise.create(name), BusinessMessage.FRANCHISE_NAME_REQUIRED);
        }

        @Test
        void shouldRejectBlankId() {
            assertBusinessError(() -> new Franchise(" ", "Burger House", List.of()),
                    BusinessMessage.FRANCHISE_ID_REQUIRED);
        }

        @Test
        void shouldTreatNullBranchesAsEmptyList() {
            assertThat(new Franchise("f-1", "Burger House", null).branches()).isEmpty();
        }

        @Test
        void shouldNotBeAffectedByChangesToTheOriginalList() {
            List<Branch> source = new ArrayList<>();
            Franchise franchise = new Franchise("f-1", "Burger House", source);

            source.add(Branch.create("Centro"));

            assertThat(franchise.branches()).isEmpty();
        }

        @Test
        void shouldExposeUnmodifiableBranches() {
            Franchise franchise = Franchise.create("Burger House");

            assertThatThrownBy(() -> franchise.branches().add(Branch.create("Centro")))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("rename")
    class Rename {

        @Test
        void shouldReturnNewFranchiseWithUpdatedNameKeepingIdAndBranches() {
            Branch branch = Branch.create("Centro");
            Franchise original = Franchise.create("Burger House").addBranch(branch);

            Franchise renamed = original.rename("  Burger Palace ");

            assertThat(renamed.name()).isEqualTo("Burger Palace");
            assertThat(renamed.id()).isEqualTo(original.id());
            assertThat(renamed.branches()).containsExactly(branch);
            assertThat(original.name()).isEqualTo("Burger House");
        }

        @Test
        void shouldRejectBlankName() {
            Franchise franchise = Franchise.create("Burger House");

            assertBusinessError(() -> franchise.rename(""), BusinessMessage.FRANCHISE_NAME_REQUIRED);
        }
    }

    @Nested
    @DisplayName("renameBranch")
    class RenameBranch {

        private final Branch centro = new Branch("b-1", "Centro", List.of(Product.create("Papas", 1)));
        private final Branch norte = new Branch("b-2", "Norte", List.of());
        private final Franchise franchise = new Franchise("f-1", "Burger House", List.of(centro, norte));

        @Test
        void shouldReturnRenamedBranchKeepingItsProducts() {
            assertThat(franchise.renameBranch("b-1", "Sur"))
                    .isEqualTo(new Branch("b-1", "Sur", centro.products()));
        }

        @Test
        void shouldAllowChangingCaseOfItsOwnName() {
            assertThat(franchise.renameBranch("b-1", "CENTRO").name()).isEqualTo("CENTRO");
        }

        @Test
        void shouldRejectNameUsedByAnotherBranch() {
            assertBusinessError(() -> franchise.renameBranch("b-1", "norte"),
                    BusinessMessage.FRANCHISE_BRANCH_ALREADY_EXISTS);
        }

        @Test
        void shouldRejectUnknownBranch() {
            assertBusinessError(() -> franchise.renameBranch("unknown", "Sur"), BusinessMessage.BRANCH_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("addBranch")
    class AddBranch {

        @Test
        void shouldReturnNewFranchiseContainingTheBranchWithSameIdAndName() {
            Franchise franchise = Franchise.create("Burger House");
            Branch branch = Branch.create("Centro");

            Franchise updated = franchise.addBranch(branch);

            assertThat(updated.branches()).containsExactly(branch);
            assertThat(updated.id()).isEqualTo(franchise.id());
            assertThat(updated.name()).isEqualTo("Burger House");
        }

        @Test
        void shouldNotModifyOriginalFranchise() {
            Franchise franchise = Franchise.create("Burger House");

            franchise.addBranch(Branch.create("Centro"));

            assertThat(franchise.branches()).isEmpty();
        }

        @Test
        void shouldKeepExistingBranches() {
            Branch centro = Branch.create("Centro");
            Branch norte = Branch.create("Norte");

            Franchise updated = Franchise.create("Burger House").addBranch(centro).addBranch(norte);

            assertThat(updated.branches()).containsExactly(centro, norte);
        }

        @Test
        void shouldRejectNullBranch() {
            Franchise franchise = Franchise.create("Burger House");

            assertBusinessError(() -> franchise.addBranch(null), BusinessMessage.FRANCHISE_BRANCH_REQUIRED);
        }

        @Test
        void shouldRejectDuplicateNameIgnoringCase() {
            Franchise franchise = Franchise.create("Burger House").addBranch(Branch.create("Centro"));

            assertBusinessError(() -> franchise.addBranch(Branch.create("CENTRO")),
                    BusinessMessage.FRANCHISE_BRANCH_ALREADY_EXISTS);
        }
    }
}
