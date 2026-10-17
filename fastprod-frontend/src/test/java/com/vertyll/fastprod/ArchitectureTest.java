package com.vertyll.fastprod;

import com.vertyll.fastprod.shared.security.AccountActionPages;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "com.vertyll.fastprod", importOptions = ImportOption.DoNotIncludeTests.class)
final class ArchitectureTest {

    @ArchTest
    static final ArchRule SHARED_KNOWS_NO_FEATURE = noClasses().that()
        .resideInAPackage("com.vertyll.fastprod.shared..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("com.vertyll.fastprod.base..", "com.vertyll.fastprod.modules..");

    @ArchTest
    static final ArchRule SHARED_PACKAGES_ARE_FREE_OF_CYCLES =
            slices().matching("com.vertyll.fastprod.shared.(*)..").should().beFreeOfCycles();

    @ArchTest
    static final ArchRule SECURITY_KNOWS_NEITHER_KEYCLOAK_NOR_THE_UI = noClasses().that()
        .resideInAPackage("com.vertyll.fastprod.shared.security..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("com.vertyll.fastprod.shared.keycloak..", "com.vertyll.fastprod.base.ui..");

    @ArchTest
    static final ArchRule ACCOUNT_ACTIONS_START_ONLY_IN_THE_KEYCLOAK_MODULE = noClasses().that()
        .resideOutsideOfPackages("com.vertyll.fastprod.shared.keycloak..", "com.vertyll.fastprod.shared.security..")
        .should()
        .dependOnClassesThat()
        .belongToAnyOf(AccountActionPages.class);

    private ArchitectureTest() {
    }
}
