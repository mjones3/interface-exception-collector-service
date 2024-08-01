package com.arcone.biopro.distribution.inventory.unit;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "com.arcone.biopro.distribution.inventory")
public class LayerDependencyTest {

    private static final List<String> COMMON_DEPENDENCIES = new ArrayList<>(List.of(
        "java..",
        "javax..",
        "lombok..",
        "org.slf4j..",
        "org.apache.logging..",
        "org.apache.commons..",
        "reactor.core..",
        "org.springframework.stereotype..",
        "org.springframework.util.."
    ));

    private static final List<String> APPLICATION_COMMON_DEPENDENCIES = new ArrayList<>(List.of(
        "org.mapstruct..",
        "io.opentelemetry.."
    ));

    private static final List<String> INFRA_COMMON_DEPENDENCIES = new ArrayList<>(List.of(
        "org.springframework..",
        "jakarta..",
        "ch.qos.logback..",
        "freemarker..",
        "com.fasterxml.jackson..",
        "graphql..",
        "org.apache.kafka..",
        "reactor..",
        "io..",
        "net.logstash.."
    ));

    @ArchTest
    public static final ArchRule domainShouldNotDependOnOtherLayers =
        classes()
            .that().resideInAPackage("..domain..")
            .and().haveSimpleNameNotEndingWith("Test")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(concatenateLists(
                COMMON_DEPENDENCIES, List.of("..domain..")).toArray(new String[0]));

    @ArchTest
    public static final ArchRule applicationShouldOnlyDependOnDomain =
        classes()
            .that().resideInAPackage("..application..")
            .and().haveSimpleNameNotEndingWith("Test")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(concatenateLists(
                COMMON_DEPENDENCIES,
                APPLICATION_COMMON_DEPENDENCIES,
                List.of("..domain..", "..application..")).toArray(new String[0]));

    @ArchTest
    public static final ArchRule infraShouldOnlyDependOnDomainAndApplication =
        classes()
            .that().resideInAPackage("..infrastructure..")
            .and().haveSimpleNameNotEndingWith("Test")
            .and().areNotAnnotatedWith(Configuration.class)
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(concatenateLists(
                COMMON_DEPENDENCIES,
                APPLICATION_COMMON_DEPENDENCIES,
                INFRA_COMMON_DEPENDENCIES,
                List.of("..domain..", "..application..", "..infrastructure..")).toArray(new String[0]));

    @ArchTest
    public static final ArchRule adapterCanDependOnAnyLayer =
        classes()
            .that().resideInAPackage("..adapter..")
            .and().haveSimpleNameNotEndingWith("Test")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(concatenateLists
                (COMMON_DEPENDENCIES,
                    APPLICATION_COMMON_DEPENDENCIES,
                    INFRA_COMMON_DEPENDENCIES,
                    List.of("..domain..", "..application..", "..infrastructure..", "..adapter..")).toArray(new String[0]));


    @SafeVarargs
    public static List<String> concatenateLists(List<String>... lists) {
        List<String> result = new ArrayList<>();
        for (List<String> list : lists) {
            result.addAll(list);
        }
        return result;
    }
}

