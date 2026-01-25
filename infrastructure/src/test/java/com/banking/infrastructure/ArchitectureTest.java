package com.banking.infrastructure;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@DisplayName("Hexagonal Architecture Rules")
class ArchitectureTest {

    private static final String DOMAIN_PACKAGE = "com.banking.domain..";
    private static final String APPLICATION_PACKAGE = "com.banking.application..";
    private static final String INFRASTRUCTURE_PACKAGE = "com.banking.infrastructure..";
    private static final String ADAPTER_IN_PACKAGE = "com.banking.infrastructure.adapter.in..";
    private static final String ADAPTER_OUT_PACKAGE = "com.banking.infrastructure.adapter.out..";
    private static final String PORTS_IN_PACKAGE = "com.banking.application.port.in..";
    private static final String PORTS_OUT_PACKAGE = "com.banking.application.port.out..";

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setUp() {
        Path projectRoot = findProjectRoot();

        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPaths(
                        projectRoot.resolve("domain/target/classes"),
                        projectRoot.resolve("application/target/classes"),
                        projectRoot.resolve("infrastructure/target/classes")
                );
    }

    private static Path findProjectRoot() {
        Path currentPath = Paths.get("").toAbsolutePath();
        while (currentPath != null) {
            if (currentPath.resolve("pom.xml").toFile().exists()
                    && currentPath.resolve("domain").toFile().exists()) {
                return currentPath;
            }
            currentPath = currentPath.getParent();
        }
        return Paths.get("").toAbsolutePath().getParent();
    }

    @Nested
    @DisplayName("Domain Layer Rules")
    class DomainLayerRules {

        @Test
        @DisplayName("given_domain_classes_when_checking_dependencies_then_no_spring_dependencies")
        void given_domain_classes_when_checking_dependencies_then_no_spring_dependencies() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework..",
                            "org.springframework.boot..",
                            "org.springframework.data..",
                            "org.springframework.web.."
                    )
                    .because("Domain layer must be framework-free (Pure Java)")
                    .allowEmptyShould(true);

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("given_domain_classes_when_checking_dependencies_then_no_infrastructure_dependencies")
        void given_domain_classes_when_checking_dependencies_then_no_infrastructure_dependencies() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAPackage(INFRASTRUCTURE_PACKAGE)
                    .because("Domain layer must not depend on infrastructure layer")
                    .allowEmptyShould(true);

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("given_domain_classes_when_checking_dependencies_then_no_application_dependencies")
        void given_domain_classes_when_checking_dependencies_then_no_application_dependencies() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAPackage(APPLICATION_PACKAGE)
                    .because("Domain layer must not depend on application layer")
                    .allowEmptyShould(true);

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("given_domain_classes_when_checking_dependencies_then_no_jakarta_dependencies")
        void given_domain_classes_when_checking_dependencies_then_no_jakarta_dependencies() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "jakarta.persistence..",
                            "jakarta.servlet..",
                            "jakarta.validation.."
                    )
                    .because("Domain layer must be framework-free")
                    .allowEmptyShould(true);

            rule.check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Application Layer Rules")
    class ApplicationLayerRules {

        @Test
        @DisplayName("given_application_classes_when_checking_dependencies_then_no_infrastructure_dependencies")
        void given_application_classes_when_checking_dependencies_then_no_infrastructure_dependencies() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(APPLICATION_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAPackage(INFRASTRUCTURE_PACKAGE)
                    .because("Application layer must not depend on infrastructure layer")
                    .allowEmptyShould(true);

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("given_application_classes_when_checking_dependencies_then_no_spring_dependencies")
        void given_application_classes_when_checking_dependencies_then_no_spring_dependencies() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(APPLICATION_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework..",
                            "org.springframework.boot..",
                            "org.springframework.data..",
                            "org.springframework.web.."
                    )
                    .because("Application layer must be framework-free")
                    .allowEmptyShould(true);

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("given_application_classes_when_checking_dependencies_then_only_domain_allowed")
        void given_application_classes_when_checking_dependencies_then_only_domain_allowed() {
            ArchRule rule = classes()
                    .that().resideInAPackage(APPLICATION_PACKAGE)
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            "com.banking.domain..",
                            "com.banking.application..",
                            "java..",
                            "lombok..",
                            "org.slf4j.."
                    )
                    .because("Application layer should only depend on domain and itself")
                    .allowEmptyShould(true);

            rule.check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Infrastructure Layer Rules")
    class InfrastructureLayerRules {

        @Test
        @DisplayName("given_infrastructure_classes_when_checking_dependencies_then_only_inner_layers_allowed")
        void given_infrastructure_classes_when_checking_dependencies_then_only_inner_layers_allowed() {
            ArchRule rule = classes()
                    .that().resideInAPackage(INFRASTRUCTURE_PACKAGE)
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            "com.banking.domain..",
                            "com.banking.application..",
                            "com.banking.infrastructure..",
                            "java..",
                            "lombok..",
                            "org.slf4j..",
                            "org.springframework..",
                            "org.mapstruct..",
                            "org.springdoc..",
                            "org.zalando..",
                            "io.swagger..",
                            "io.bucket4j..",
                            "io.github.bucket4j..",
                            "io.micrometer..",
                            "jakarta..",
                            "com.fasterxml.."
                    )
                    .because("Infrastructure layer should only depend on allowed packages")
                    .allowEmptyShould(true);

            rule.check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Adapter Independence Rules")
    class AdapterIndependenceRules {

        @Test
        @DisplayName("given_input_adapter_classes_when_checking_dependencies_then_no_output_adapter_dependencies")
        void given_input_adapter_classes_when_checking_dependencies_then_no_output_adapter_dependencies() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(ADAPTER_IN_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAPackage(ADAPTER_OUT_PACKAGE)
                    .because("Input adapters should not directly depend on output adapters")
                    .allowEmptyShould(true);

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("given_output_adapter_classes_when_checking_dependencies_then_no_input_adapter_dependencies")
        void given_output_adapter_classes_when_checking_dependencies_then_no_input_adapter_dependencies() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(ADAPTER_OUT_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAPackage(ADAPTER_IN_PACKAGE)
                    .because("Output adapters should not directly depend on input adapters")
                    .allowEmptyShould(true);

            rule.check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Port Interface Rules")
    class PortInterfaceRules {

        @Test
        @DisplayName("given_input_port_classes_when_checking_type_then_must_be_interfaces")
        void given_input_port_classes_when_checking_type_then_must_be_interfaces() {
            ArchRule rule = classes()
                    .that().resideInAPackage(PORTS_IN_PACKAGE)
                    .should().beInterfaces()
                    .because("Input ports (use cases) should be interfaces")
                    .allowEmptyShould(true);

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("given_output_port_classes_when_checking_type_then_must_be_interfaces")
        void given_output_port_classes_when_checking_type_then_must_be_interfaces() {
            ArchRule rule = classes()
                    .that().resideInAPackage(PORTS_OUT_PACKAGE)
                    .should().beInterfaces()
                    .because("Output ports (repositories, external services) should be interfaces")
                    .allowEmptyShould(true);

            rule.check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Layered Architecture Rules")
    class LayeredArchitectureRules {

        @Test
        @DisplayName("given_hexagonal_architecture_when_checking_layer_dependencies_then_must_respect_direction")
        void given_hexagonal_architecture_when_checking_layer_dependencies_then_must_respect_direction() {
            ArchRule rule = layeredArchitecture()
                    .consideringOnlyDependenciesInLayers()
                    .layer("Domain").definedBy("com.banking.domain..")
                    .layer("Application").definedBy("com.banking.application..")
                    .layer("Infrastructure").definedBy("com.banking.infrastructure..")
                    .whereLayer("Domain").mayNotAccessAnyLayer()
                    .whereLayer("Application").mayOnlyAccessLayers("Domain")
                    .whereLayer("Infrastructure").mayOnlyAccessLayers("Application", "Domain")
                    .because("Hexagonal architecture requires outer layers to depend on inner layers only")
                    .allowEmptyShould(true);

            rule.check(importedClasses);
        }
    }
}
