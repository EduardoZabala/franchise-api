package com.franchise.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Enforces the Clean Architecture dependency rule: every dependency points inwards, to the domain.
 */
@AnalyzeClasses(packages = "com.franchise", importOptions = ImportOption.DoNotIncludeTests.class)
class CleanArchitectureTest {

    private static final String DOMAIN = "..domain..";
    private static final String MODEL = "..domain.model..";
    private static final String GATEWAYS = "..domain.model.gateways..";
    private static final String USE_CASES = "..domain.usecase..";
    private static final String INFRASTRUCTURE = "..infrastructure..";
    private static final String ENTRY_POINTS = "..infrastructure.entrypoints..";
    private static final String DRIVEN_ADAPTERS = "..infrastructure.drivenadapters..";
    private static final String CONFIG = "..infrastructure.config..";

    @ArchTest
    static final ArchRule layers_respect_the_dependency_rule = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Domain").definedBy(DOMAIN)
            .layer("EntryPoints").definedBy(ENTRY_POINTS)
            .layer("DrivenAdapters").definedBy(DRIVEN_ADAPTERS)
            .layer("Config").definedBy(CONFIG)
            .whereLayer("EntryPoints").mayOnlyBeAccessedByLayers("Config")
            .whereLayer("DrivenAdapters").mayOnlyBeAccessedByLayers("Config")
            .whereLayer("Config").mayNotBeAccessedByAnyLayer()
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("EntryPoints", "DrivenAdapters", "Config");

    @ArchTest
    static final ArchRule domain_does_not_depend_on_infrastructure = noClasses()
            .that().resideInAPackage(DOMAIN)
            .should().dependOnClassesThat().resideInAPackage(INFRASTRUCTURE)
            .because("the domain is the core and must not know about adapters");

    @ArchTest
    static final ArchRule domain_is_framework_agnostic = noClasses()
            .that().resideInAPackage(DOMAIN)
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "software.amazon..", "jakarta..")
            .because("business rules must not depend on Spring, AWS or web/validation frameworks");

    @ArchTest
    static final ArchRule entities_are_plain_java = noClasses()
            .that().resideInAPackage(MODEL)
            .and().resideOutsideOfPackage(GATEWAYS)
            .should().dependOnClassesThat().resideInAPackage("reactor..")
            .because("entities hold pure business rules; only gateways and use cases are reactive");

    @ArchTest
    static final ArchRule entities_do_not_depend_on_use_cases = noClasses()
            .that().resideInAPackage(MODEL)
            .should().dependOnClassesThat().resideInAPackage(USE_CASES);

    @ArchTest
    static final ArchRule gateways_are_interfaces = classes()
            .that().resideInAPackage(GATEWAYS)
            .and().haveSimpleNameEndingWith("Gateway")
            .should().beInterfaces();

    @ArchTest
    static final ArchRule use_cases_follow_naming_convention = classes()
            .that().resideInAPackage(USE_CASES)
            .and().areTopLevelClasses()
            .and().haveSimpleNameNotEndingWith("package-info")
            .should().haveSimpleNameEndingWith("UseCase");

    @ArchTest
    static final ArchRule entry_points_and_driven_adapters_are_independent = noClasses()
            .that().resideInAPackage(ENTRY_POINTS)
            .should().dependOnClassesThat().resideInAPackage(DRIVEN_ADAPTERS)
            .because("adapters communicate only through the domain");
}
