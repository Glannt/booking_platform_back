package com.dotnt.booking_travel_backend;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class BookingTravelBackendApplicationTests {

    private static final JavaClasses classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.dotnt.booking_travel_backend");

    @Test
    @DisplayName("No module outside media should depend on media.internal")
    void verifyMediaInternalEncapsulation() {
        ArchRule rule = noClasses()
                .that().resideOutsideOfPackage("..media.internal..")
                .should().dependOnClassesThat().resideInAPackage("..media.internal..");
        rule.check(classes);
    }

    @Test
    @DisplayName("Tour module should not depend on other modules' internal packages")
    void verifyTourEncapsulation() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..tour..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..media.internal..",
                        "..fleet.internal..",
                        "..intercity.internal..",
                        "..user.internal.."
                );
        rule.check(classes);
    }

    @Test
    @DisplayName("Fleet module should not depend on media.internal")
    void verifyFleetEncapsulation() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..fleet..")
                .should().dependOnClassesThat().resideInAPackage("..media.internal..");
        rule.check(classes);
    }

    @Test
    @DisplayName("Sections module should not depend on media.internal")
    void verifySectionsEncapsulation() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..sections..")
                .should().dependOnClassesThat().resideInAPackage("..media.internal..");
        rule.check(classes);
    }

    @Test
    @DisplayName("Intercity module should not depend on media.internal or sharedroute.internal")
    void verifyIntercityEncapsulation() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..intercity..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..media.internal..",
                        "..sharedroute.internal.."
                );
        rule.check(classes);
    }
}
