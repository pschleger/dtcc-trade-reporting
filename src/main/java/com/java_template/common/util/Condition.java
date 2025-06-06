package com.java_template.common.util;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Condition {
    private String type;
    private String jsonPath;
    private String operatorType;
    private Object value;

    public Condition(String type, String jsonPath, String operatorType, Object value) {
        this.type = type;
        this.jsonPath = jsonPath;
        this.operatorType = operatorType;
        this.value = value;
    }

    public static Condition of(String jsonPath, String operatorType, Object value) {
        return new Condition("simple", jsonPath, operatorType, value);
    }
}
