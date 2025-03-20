package com.java_template.common.entity;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EntityRegistry {
    private static final Map<String, Class<? extends BaseEntity>> modelToClassMap = new HashMap<>();
    private static final Map<Class<? extends BaseEntity>, String> classToModelMap = new HashMap<>();

    static {
//        modelToClassMap.put("expense_report_job", ExpenseReportJob.class);
//        modelToClassMap.put("expense_report", ExpenseReport.class);
//        modelToClassMap.put("employee_expense", EmployeeExpense.class);
//        classToModelMap.put(ExpenseReportJob.class, "expense_report_job");
//        classToModelMap.put(ExpenseReport.class, "expense_report");
//        classToModelMap.put(EmployeeExpense.class, "employee_expense");
    }

    public static Class<? extends BaseEntity> getClassByModel(String model) {
        return modelToClassMap.get(model);
    }

    public static String getModelByClass(Class<? extends BaseEntity> clazz) {
        return classToModelMap.get(clazz);
    }
}
