package com.java_template.common.ai;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AIRequestDto {
    private String id = UUID.randomUUID().toString();
    private String question;
    private String return_object = "chat";
}
