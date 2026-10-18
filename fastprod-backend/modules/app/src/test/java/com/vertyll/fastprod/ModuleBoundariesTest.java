package com.vertyll.fastprod;

import java.util.List;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * The modules' boundaries: each module is reached by the others only through its {@code api} package, and no two
 * modules depend on each other. {@code sharedinfrastructure} is the shared kernel every module may use.
 */
@AnalyzeClasses(packages = "com.vertyll.fastprod", importOptions = ImportOption.DoNotIncludeTests.class)
final class ModuleBoundariesTest {

    private static final String BASE = "com.vertyll.fastprod.";
    private static final List<String> MODULES = List.of("employee", "file", "security", "translation", "user");

    @ArchTest
    static final ArchRule MODULES_ARE_FREE_OF_CYCLES = slices().matching(BASE + "(*)..").should().beFreeOfCycles();

    @ArchTest
    static void modulesReachEachOtherOnlyThroughTheirApi(JavaClasses classes) {
        for (String module : MODULES) {
            String inside = BASE + module + "..";
            noClasses().that()
                .resideOutsideOfPackage(inside)
                .should()
                .dependOnClassesThat(resideInAPackage(inside).and(not(resideInAPackage(BASE + module + ".api.."))))
                .as("other modules reach " + module + " only through " + module + ".api")
                .check(classes);
        }
    }

    private ModuleBoundariesTest() {
    }
}
