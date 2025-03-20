package com.java_template.service;

import com.java_template.common.entity.BaseEntity;
import com.java_template.common.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class EntityService {

    private final CrudRepository<BaseEntity, UUID> repository;

    public EntityService(CrudRepository<? extends BaseEntity, UUID> repository) {
        this.repository = (CrudRepository<BaseEntity, UUID>) repository;
    }

    public BaseEntity getItem(UUID id) {
        return repository.findById(id).orElseThrow();
    }

    public List<BaseEntity> getItems() {
        return List.of();
    }

    public List<BaseEntity> getSingleItemByCondition(String condition) {
        return List.of();
    }

    public List<BaseEntity> getItemsByCondition(String condition) {
        return List.of();
    }

    public BaseEntity addItem(BaseEntity entity) {
        return repository.save(entity);
    }

    public BaseEntity updateItem(String id, BaseEntity entity) {
        return null;
    }
}



