package com.java_template.common.util;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SearchConditionRequest {
    private String type;
    private String operator;
    private List<Condition> conditions;
}
