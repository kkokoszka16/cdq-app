package com.banking.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@DisplayName("Hexagonal Architecture Rules")
class HexagonalArchitectureTest {

    private static final String DOMAIN_PACKAGE = "com.banking.domain..";
    private static final String APPLICATION_PACKAGE = "com.banking.application..";
    private static final String INFRASTRUCTURE_PACKAGE = "com.banking.infrastructure..";
    private static final String BOOTSTRAP_PACKAGE = "com.banking..";

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setUp() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.banking");
    }

    @Nested
    @DisplayName("Domain Layer Rules")
    class DomainLayerRules {

        @Test
        @DisplayName("given domain classes when checking dependencies then no framework dependencies")
        void given_domain_classes_when_checking_then_no_framework_dependencies() {
            noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework..",
                            "jakarta..",
                            "javax.persistence..",
                            "org.hibernate.."
                    )
                    .because("Domain layer must be framework-free")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("given domain classes when checking dependencies then no application layer dependencies")
        void given_domain_classes_when_checking_then_no_application_dependencies() {
            noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAPackage(APPLICATION_PACKAGE)
                    .because("Domain layer must not depend on application layer")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("given domain classes when checking dependencies then no infrastructure dependencies")
        void given_domain_classes_when_checking_then_no_infrastructure_dependencies() {
            noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAPackage(INFRASTRUCTURE_PACKAGE)
                    .because("Domain layer must not depend on infrastructure layer")
                    .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Application Layer Rules")
    class ApplicationLayerRules {

        @Test
        @DisplayName("given application classes when checking dependencies then no framework dependencies")
        void given_application_classes_when_checking_then_no_framework_dependencies() {
            noClasses()
                    .that().resideInAPackage(APPLICATION_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework..",
                            "jakarta..",
                            "javax.persistence..",
                            "org.hibernate.."
                    )
                    .because("Application layer must be framework-free")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("given application classes when checking dependencies then no infrastructure dependencies")
        void given_application_classes_when_checking_then_no_infrastructure_dependencies() {
            noClasses()
                    .that().resideInAPackage(APPLICATION_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAPackage(INFRASTRUCTURE_PACKAGE)
                    .because("Application layer must not depend on infrastructure layer")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("given application classes when checking dependencies then may depend on domain")
        void given_application_classes_when_checking_then_may_depend_on_domain() {
            classes()
                    .that().resideInAPackage(APPLICATION_PACKAGE)
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            "com.banking.domain..",
                            "com.banking.application..",
                            "java..",
                            "lombok..",
                            "org.slf4j.."
                    )
                    .because("Application layer should only depend on domain, itself, and logging")
                    .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Infrastructure Layer Rules")
    class InfrastructureLayerRules {

        @Test
        @DisplayName("given infrastructure classes when checking dependencies then may use frameworks")
        void given_infrastructure_classes_when_checking_then_may_use_frameworks() {
            classes()
                    .that().resideInAPackage(INFRASTRUCTURE_PACKAGE)
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            "com.banking..",
                            "org.springframework..",
                            "jakarta..",
                            "io.swagger..",
                            "io.github.bucket4j..",
                            "org.zalando..",
                            "org.mapstruct..",
                            "lombok..",
                            "java..",
                            "org.bson..",
                            "org.slf4j.."
                    )
                    .because("Infrastructure may use frameworks and external libraries")
                    .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Layer Dependencies")
    class LayerDependencies {

        @Test
        @DisplayName("given all layers when checking then dependency direction is correct")
        void given_all_layers_when_checking_then_dependency_direction_correct() {
            layeredArchitecture()
                    .consideringOnlyDependenciesInLayers()
                    .layer("Domain").definedBy("com.banking.domain..")
                    .layer("Application").definedBy("com.banking.application..")
                    .layer("Infrastructure").definedBy("com.banking.infrastructure..")
                    .whereLayer("Domain").mayNotAccessAnyLayer()
                    .whereLayer("Application").mayOnlyAccessLayers("Domain")
                    .whereLayer("Infrastructure").mayOnlyAccessLayers("Domain", "Application")
                    .because("Hexagonal architecture requires strict layer dependencies")
                    .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Port and Adapter Rules")
    class PortAndAdapterRules {

        @Test
        @DisplayName("given ports when checking then are interfaces")
        void given_ports_when_checking_then_are_interfaces() {
            classes()
                    .that().resideInAPackage("com.banking.application.port..")
                    .and().haveSimpleNameEndingWith("UseCase")
                    .should().beInterfaces()
                    .because("Use case ports must be interfaces")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("given output ports when checking then are interfaces")
        void given_output_ports_when_checking_then_are_interfaces() {
            classes()
                    .that().resideInAPackage("com.banking.application.port.out..")
                    .should().beInterfaces()
                    .because("Output ports must be interfaces")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("given adapters when checking then implement ports")
        void given_adapters_in_persistence_when_checking_then_reside_in_adapter_package() {
            classes()
                    .that().haveSimpleNameEndingWith("Repository")
                    .and().resideInAPackage("com.banking.infrastructure..")
                    .should().resideInAPackage("com.banking.infrastructure.adapter.out.persistence..")
                    .because("Repository adapters must be in persistence adapter package")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("given controllers when checking then reside in web adapter package")
        void given_controllers_when_checking_then_reside_in_web_adapter_package() {
            classes()
                    .that().haveSimpleNameEndingWith("Controller")
                    .should().resideInAPackage("com.banking.infrastructure.adapter.in.web..")
                    .because("Controllers must be in web adapter package")
                    .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Naming Conventions")
    class NamingConventions {

        @Test
        @DisplayName("given services when checking then have Service suffix")
        void given_services_when_checking_then_have_service_suffix() {
            classes()
                    .that().resideInAPackage("com.banking.application.service..")
                    .and().areNotInterfaces()
                    .should().haveSimpleNameEndingWith("Service")
                    .orShould().haveSimpleNameEndingWith("Processor")
                    .because("Application services must end with Service or Processor")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("given domain exceptions when checking then extend DomainException")
        void given_domain_exceptions_when_checking_then_extend_domain_exception() {
            classes()
                    .that().resideInAPackage("com.banking.domain.exception..")
                    .and().haveSimpleNameEndingWith("Exception")
                    .and().doNotHaveSimpleName("DomainException")
                    .should().beAssignableTo("com.banking.domain.exception.DomainException")
                    .because("Domain exceptions must extend DomainException")
                    .check(importedClasses);
        }
    }
}
