package com.java_template.auxiliary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.java_template.common.service.EntityService;
import lombok.Getter;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Component
@Getter
public class EntityGenerator {

    @Autowired
    private Faker faker;
    @Autowired
    private Randomizer randomizer;
    @Autowired
    private EntityService entityService;

    private static String ENTITY_VERSION = "1";
    private static String EMPLOYEE = "employee";

    private final ObjectMapper objectMapper;
    UUID fakeUuid = UUID.fromString("a50a7fbe-1e3b-11b2-9575-f2bfe09fbe21");

    List<String> descriptions = List.of("hotel", "taxi", "transportation", "meals", "other");

    public EntityGenerator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ArrayNode generateExpenseReports(int count, boolean fakeEmployeeId) {
        ArrayNode items = entityService.getItems(EMPLOYEE, ENTITY_VERSION).join();
        ArrayNode reports = objectMapper.createArrayNode();

        List<UUID> idList = new ArrayList<>();
        if (!fakeEmployeeId) {
            for (JsonNode node : items) {
                idList.add(UUID.fromString(node.get("technicalId").asText()));
            }
        } else {
            idList = List.of(fakeUuid);
        }

        for (int i = 0; i < count; i++) {
            UUID employeeId = randomizer.getRandomElement(idList);
            ObjectNode report = objectMapper.createObjectNode();
            report.put("employeeId", employeeId.toString());
            report.put("destination", faker.country().capital());
            report.put("departureDate", new Timestamp(faker.date().past(1, TimeUnit.DAYS).getTime()).toString());
            report.set("expenseList", generateExpenseList(2));
            report.put("advancePayment", new BigDecimal(faker.commerce().price(50, 100)).toString());
            report.put("amountPayable", new BigDecimal("0.00").toString());

            reports.add(report);
        }
        return reports;
    }

    public ArrayNode generateExpenseReports(int count) {
        return generateExpenseReports(count, false);
    }

    public ArrayNode generateExpenseList(int count) {
        ArrayNode expenses = objectMapper.createArrayNode();
        for (int i = 0; i < count; i++) {
            ObjectNode expense = objectMapper.createObjectNode();
            expense.put("description", randomizer.getRandomElement(descriptions));
            expense.put("amount", new BigDecimal(faker.commerce().price(10, 100)).toString());
            expenses.add(expense);
        }
        return expenses;
    }

    public ArrayNode generatePayments(int count) {
        ArrayNode payments = objectMapper.createArrayNode();
        for (int i = 0; i < count; i++) {
            ObjectNode payment = objectMapper.createObjectNode();
            payment.put("expenseReportId", fakeUuid.toString());
            payment.put("amount", new BigDecimal(faker.commerce().price(10, 100)).toString());
            payments.add(payment);
        }
        return payments;
    }

    public ArrayNode generateEmployees(int count) {
        ArrayNode employees = objectMapper.createArrayNode();
        for (int i = 0; i < count; i++) {
            ObjectNode employee = objectMapper.createObjectNode();
            employee.put("fullName", faker.name().fullName());
            employee.put("department", faker.job().field());
            employees.add(employee);
        }
        return employees;
    }
}
