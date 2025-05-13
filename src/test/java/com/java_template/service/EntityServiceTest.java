package com.java_template.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.java_template.auxiliary.EntityGenerator;
import com.java_template.common.service.EntityService;
import com.java_template.common.util.Condition;
import com.java_template.common.util.SearchConditionRequest;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EntityServiceTest {
    private final Logger logger = LoggerFactory.getLogger(EntityServiceTest.class);

    @Autowired
    private EntityGenerator entityGenerator;
    @Autowired
    private EntityService entityService;

    private static String ENTITY_VERSION = "1";
    private static String EMPLOYEE = "employee";
    private static String EXPENSE_REPORT = "expense_report";

    @AfterEach
    void deleteEntities() {
        entityService.deleteItems(EXPENSE_REPORT, ENTITY_VERSION);
        entityService.deleteItems(EMPLOYEE, ENTITY_VERSION);
    }

    @Test
    public void searchTest() {
        CompletableFuture<Void> f1 = CompletableFuture.runAsync(() -> entityService.deleteItems(EXPENSE_REPORT, ENTITY_VERSION));
        CompletableFuture<Void> f2 = CompletableFuture.runAsync(() -> entityService.deleteItems(EMPLOYEE, ENTITY_VERSION));
        CompletableFuture.allOf(f1, f2).join();
        var employees = entityGenerator.generateEmployees(1);
        entityService.addItems(EMPLOYEE, ENTITY_VERSION, employees).join();

        var nReports = 5;
        var reports = entityGenerator.generateExpenseReports(nReports);
        var report1 = (ObjectNode) reports.get(0);
        report1.put("destination", "FirstCity");
        var report2 = (ObjectNode) reports.get(1);
        report2.put("destination", "SecondCity");

        var saveResult = entityService.addItems(EXPENSE_REPORT, ENTITY_VERSION, reports).join();
        assertThat(saveResult).isNotNull();
        assertThat(saveResult.size()).isEqualTo(nReports);

        var conditionRequest = new SearchConditionRequest();
        conditionRequest.setType("group");
        conditionRequest.setOperator("OR");
        conditionRequest.setConditions(List.of(
                new Condition("simple", "$.destination", "EQUALS", "FirstCity"),
                new Condition("simple", "$.destination", "EQUALS", "SecondCity")
        ));

        var searchResult = entityService.getItemsByCondition(EXPENSE_REPORT, ENTITY_VERSION, conditionRequest).join();
        logger.info(searchResult.toString());

        assertThat(searchResult).isNotNull();
        assertThat(searchResult.isArray()).isTrue();
        assertThat(searchResult.size()).isEqualTo(2);

        Set<String> cities = new HashSet<>();
        for (JsonNode node : searchResult) {
            JsonNode destinationNode = node.get("data").get("destination");
            assertThat(destinationNode).isNotNull();
            cities.add(destinationNode.asText());
        }

        assertThat(cities).containsExactlyInAnyOrder("FirstCity", "SecondCity");
    }
}