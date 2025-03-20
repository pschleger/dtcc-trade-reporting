package com.java_template.common.repository;


import com.java_template.common.entity.BaseEntity;

import java.util.Optional;

public interface CrudRepository<T extends BaseEntity, ID> {

    T save(T entity);

    Iterable<T> saveAll(Iterable<T> entities);

    Optional<T> findById(ID id);

    boolean existsById(ID id);

    Iterable<T> findAll();

    Iterable<T> findAllById(Iterable<ID> ids);

    long count();

    void deleteById(ID id);

    void delete(T entity);

    void deleteAllById(Iterable<? extends ID> ids);

    void deleteAll(Iterable<? extends T> entities);

    void deleteAll();

}