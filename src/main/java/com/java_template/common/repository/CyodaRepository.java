package com.java_template.common.repository;

import com.java_template.common.repository.dto.SaveEntityRespDTO;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class CyodaRepository implements CrudRepository<CyodaEntity, UUID> {

    private final CyodaHttpRepository repository;

    public CyodaRepository(CyodaHttpRepository repository) {
        this.repository = repository;
    }

    @Override
    public CyodaEntity save(CyodaEntity entity) {
        if (entity.getId() == null) {
            SaveEntityRespDTO entityRespDTO = repository.saveSingleEntity(entity);
            if (entityRespDTO != null) {
                entity.setId(UUID.fromString(entityRespDTO.getEntityIds().get(0)));
                return entity;
            }
        }
        else{
            if (repository.updateEntity(entity, null)) {
                return entity;
            }
        }
        return null;
    }

    @Override
    public Iterable<CyodaEntity> saveAll(Iterable<CyodaEntity> entities) {
        return null;
    }

    @Override
    public Optional<CyodaEntity> findById(UUID uuid) {
        CyodaEntity entity = repository.getByIdAsObject(uuid);
        return Optional.of(entity);
    }

    @Override
    public boolean existsById(UUID uuid) {
        return false;
    }

    @Override
    public Iterable<CyodaEntity> findAll() {
        return null;
    }

    @Override
    public Iterable<CyodaEntity> findAllById(Iterable<UUID> uuids) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(UUID uuid) {

    }

    @Override
    public void delete(CyodaEntity entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends UUID> uuids) {

    }

    @Override
    public void deleteAll(Iterable<? extends CyodaEntity> entities) {

    }

    @Override
    public void deleteAll() {

    }
}
