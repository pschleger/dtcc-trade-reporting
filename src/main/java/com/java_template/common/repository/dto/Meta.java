package com.java_template.common.repository.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Meta {
    private String token;
    private String entityModel;
    private String entityVersion;
    private String updateTransition = "update";

    public Meta(String token, String entityModel, String entityVersion) {
        this.token = token;
        this.entityModel = entityModel;
        this.entityVersion = entityVersion;
    }

    @Override
    public String toString() {
        return "Meta{" +
                "token='" + token + '\'' +
                ", entityModel='" + entityModel + '\'' +
                ", entityVersion='" + entityVersion + '\'' +
                ", updateTransition='" + updateTransition + '\'' +
                '}';
    }
}

