package com.java_template.common.repository.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SaveEntityRespDTO {
    private String transactionId;

    @JsonProperty("entityIds")
    private List<String> entityIds;
}
