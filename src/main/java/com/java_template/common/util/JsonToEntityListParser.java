package com.java_template.common.util;

import com.java_template.common.entity.BaseEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class JsonToEntityListParser {
    public <T extends BaseEntity> List<T> parseResponse(String jsonResponse, Class<T> clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            EmbeddedWrapper response = mapper.readValue(jsonResponse, EmbeddedWrapper.class);
            List<EmbeddedWrapper.TreeWrapper> entityWrappers = response.getEmbedded().getObjectNodes();

            List<T> entities = new ArrayList<>();
            for (EmbeddedWrapper.TreeWrapper wrapper : entityWrappers) {
                T entity = mapper.convertValue(wrapper.getTree(), clazz);
                entity.setId(UUID.fromString(wrapper.getId()));
                entities.add(entity);
            }
            return entities;
        } catch (Exception e) {
            List<EmbeddedWrapper.TreeWrapper> entityWrappers = mapper.readValue(jsonResponse, new TypeReference<List<EmbeddedWrapper.TreeWrapper>>() {});

            List<T> entities = new ArrayList<>();
            for (EmbeddedWrapper.TreeWrapper wrapper : entityWrappers) {
                T entity = mapper.convertValue(wrapper.getTree(), clazz);
                entity.setId(UUID.fromString(wrapper.getId()));
                entities.add(entity);
            }
            return entities;
        }
    }
}
