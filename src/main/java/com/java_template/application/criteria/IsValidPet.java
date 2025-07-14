package com.java_template.application.criteria;

import com.java_template.application.entity.pet.Pet;
import com.java_template.common.serializer.CriterionSerializer;
import com.java_template.common.serializer.ErrorInfo;
import com.java_template.common.serializer.EvaluationOutcome;
import com.java_template.common.serializer.ReasonAttachmentStrategy;
import com.java_template.common.serializer.SerializerFactory;
import com.java_template.common.config.Config;
import com.java_template.common.workflow.CyodaCriterion;
import com.java_template.common.workflow.CyodaEventContext;
import com.java_template.common.workflow.OperationSpecification;
import org.cyoda.cloud.api.event.common.ModelSpec;
import org.cyoda.cloud.api.event.processing.EntityCriteriaCalculationRequest;
import org.cyoda.cloud.api.event.processing.EntityCriteriaCalculationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * ABOUTME: Criteria implementation that validates Pet entities using the enhanced EvaluationChain approach.
 * Demonstrates advanced usage patterns including:
 * - Type-safe entity extraction with EvaluationChain
 * - EvaluationOutcome sealed classes for clear success/failure contracts
 * - Reason attachment to warnings for temporary workaround
 * - Comprehensive validation logic with detailed failure reasons
 * - Custom error handling with ErrorInfo
 * - Fluent API for criteria evaluation
 */
@Component
public class IsValidPet implements CyodaCriterion {

    private static final Logger logger = LoggerFactory.getLogger(IsValidPet.class);

    // Business validation constants
    private static final Set<String> VALID_STATUSES = Set.of("available", "pending", "sold");
    private static final Set<String> REQUIRED_FIELDS = Set.of("id", "name");
    private static final int MIN_NAME_LENGTH = 1;
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_TAGS = 10;

    private final CriterionSerializer serializer;

    public IsValidPet(SerializerFactory serializerFactory) {
        this.serializer = serializerFactory.getDefaultCriteriaSerializer();
        logger.info("IsValidPet criteria initialized with SerializerFactory");
    }

    @Override
    public EntityCriteriaCalculationResponse check(CyodaEventContext<EntityCriteriaCalculationRequest> context) {
        EntityCriteriaCalculationRequest request = context.getEvent();
        logger.debug("Checking Pet validity for request: {}", request.getId());

        return serializer.withRequest(request)
            .evaluateEntity(Pet.class, this::validatePet)
            .withReasonAttachment(ReasonAttachmentStrategy.toWarnings())
            .withErrorHandler((error, pet) -> {
                logger.debug("Pet validation failed for request: {}", request.getId(), error);
                return ErrorInfo.validationError("Pet validation failed: " + error.getMessage());
            })
            .complete();
    }

    @Override
    public boolean supports(OperationSpecification opsSpec) {
        if (opsSpec instanceof OperationSpecification.Entity entitySpec) {
            ModelSpec modelSpec = entitySpec.modelKey();
            return "pet".equals(modelSpec.getName()) &&
                   Integer.parseInt(Config.ENTITY_VERSION) == modelSpec.getVersion();
        }
        return false;
    }

    // ========================================
    // VALIDATION METHODS
    // ========================================

    /**
     * Comprehensive Pet validation with detailed outcomes for use with EvaluationChain.
     * Uses logical chaining to validate all aspects of Pet entity.
     * Returns EvaluationOutcome.Success for success, EvaluationOutcome.Fail for failures.
     */
    private EvaluationOutcome validatePet(Pet pet) {
        logger.debug("Starting Pet validation for: {}", pet != null ? pet.getId() : "null");

        // Chain all validation checks with AND logic - first failure stops the chain
        EvaluationOutcome result = validatePetExists(pet)
            .and(validatePetBasicValidity(pet))
            .and(validateBasicStructure(pet))
            .and(validateBusinessRules(pet))
            .and(validateDataQuality(pet));

        if (result.isSuccess()) {
            logger.debug("Pet passed all validation checks: id={}, name={}", pet.getId(), pet.getName());
        } else {
            logger.debug("Pet validation failed");
        }

        return result;
    }

    /**
     * Validates that the Pet entity exists.
     */
    private EvaluationOutcome validatePetExists(Pet pet) {
        if (pet == null) {
            logger.debug("Pet entity is null");
            return EvaluationOutcome.Fail.structuralFailure("Pet entity is null");
        }
        return EvaluationOutcome.success();
    }

    /**
     * Validates Pet's built-in validation.
     */
    private EvaluationOutcome validatePetBasicValidity(Pet pet) {
        if (pet != null && !pet.isValid()) {
            logger.debug("Pet entity failed basic validation");
            return EvaluationOutcome.Fail.structuralFailure("Pet entity failed basic validation (missing required fields)");
        }
        return EvaluationOutcome.success();
    }

    /**
     * Validates basic Pet entity structure and required fields with detailed outcomes.
     * Uses logical chaining for multiple field validations.
     */
    private EvaluationOutcome validateBasicStructure(Pet pet) {
        if (pet == null) {
            return EvaluationOutcome.success(); // Already validated in validatePetExists
        }

        // Chain ID and name validation with AND logic
        EvaluationOutcome result = validatePetId(pet).and(validatePetName(pet));

        if (result.isSuccess()) {
            logger.debug("Pet passed basic structure validation: id={}, name={}", pet.getId(), pet.getName());
        }

        return result;
    }

    /**
     * Validates Pet ID field.
     */
    private EvaluationOutcome validatePetId(Pet pet) {
        if (pet.getId() == null || pet.getId() <= 0) {
            String reason = pet.getId() == null ?
                "Pet ID is null" :
                String.format("Pet ID must be positive, got: %d", pet.getId());
            logger.debug("Pet has invalid ID: {}", pet.getId());
            return EvaluationOutcome.Fail.structuralFailure(reason);
        }
        return EvaluationOutcome.success();
    }

    /**
     * Validates Pet name field.
     */
    private EvaluationOutcome validatePetName(Pet pet) {
        if (pet.getName() == null || pet.getName().trim().isEmpty()) {
            String reason = pet.getName() == null ?
                "Pet name is null" :
                "Pet name is empty or contains only whitespace";
            logger.debug("Pet has invalid name: {}", pet.getName());
            return EvaluationOutcome.Fail.structuralFailure(reason);
        }
        return EvaluationOutcome.success();
    }

    /**
     * Validates Pet entity against business rules with detailed outcomes.
     * Uses logical chaining for multiple business rule validations.
     */
    private EvaluationOutcome validateBusinessRules(Pet pet) {
        if (pet == null) {
            return EvaluationOutcome.success(); // Already validated in validatePetExists
        }

        // Chain all business rule validations with AND logic
        EvaluationOutcome result = validatePetStatus(pet)
            .and(validatePetNameLength(pet))
            .and(validatePetTagsCount(pet));

        if (result.isSuccess()) {
            logger.debug("Pet passed business rules validation");
        }

        return result;
    }

    /**
     * Validates Pet status against allowed values.
     */
    private EvaluationOutcome validatePetStatus(Pet pet) {
        if (pet.getStatus() != null && !VALID_STATUSES.contains(pet.getStatus().toLowerCase())) {
            String reason = String.format("Pet status '%s' is invalid. Valid statuses are: %s",
                pet.getStatus(), String.join(", ", VALID_STATUSES));
            logger.debug("Pet has invalid status: {}", pet.getStatus());
            return EvaluationOutcome.Fail.businessRuleFailure(reason);
        }
        return EvaluationOutcome.success();
    }

    /**
     * Validates Pet name length constraints.
     */
    private EvaluationOutcome validatePetNameLength(Pet pet) {
        if (pet.getName() != null) {
            int nameLength = pet.getName().trim().length();
            if (nameLength < MIN_NAME_LENGTH || nameLength > MAX_NAME_LENGTH) {
                String reason = String.format("Pet name length %d is invalid. Must be between %d and %d characters",
                    nameLength, MIN_NAME_LENGTH, MAX_NAME_LENGTH);
                logger.debug("Pet name length {} is invalid", nameLength);
                return EvaluationOutcome.Fail.businessRuleFailure(reason);
            }
        }
        return EvaluationOutcome.success();
    }

    /**
     * Validates Pet tags count constraints.
     */
    private EvaluationOutcome validatePetTagsCount(Pet pet) {
        if (pet.getTags() != null && pet.getTags().size() > MAX_TAGS) {
            String reason = String.format("Pet has too many tags (%d). Maximum allowed is %d",
                pet.getTags().size(), MAX_TAGS);
            logger.debug("Pet has too many tags: {}", pet.getTags().size());
            return EvaluationOutcome.Fail.businessRuleFailure(reason);
        }
        return EvaluationOutcome.success();
    }

    /**
     * Validates Pet entity data quality and consistency with detailed outcomes.
     * Uses logical chaining for multiple data quality validations.
     */
    private EvaluationOutcome validateDataQuality(Pet pet) {
        if (pet == null) {
            return EvaluationOutcome.success(); // Already validated in validatePetExists
        }

        // Chain all data quality validations with AND logic
        EvaluationOutcome result = validatePetPhotoUrls(pet)
            .and(validatePetTags(pet))
            .and(validatePetCategory(pet));

        if (result.isSuccess()) {
            logger.debug("Pet passed data quality validation");
        }

        return result;
    }

    /**
     * Validates Pet photo URLs format and validity.
     */
    private EvaluationOutcome validatePetPhotoUrls(Pet pet) {
        if (pet.getPhotoUrls() != null) {
            for (int i = 0; i < pet.getPhotoUrls().size(); i++) {
                String url = pet.getPhotoUrls().get(i);
                if (url != null && !isValidUrl(url)) {
                    String reason = String.format("Pet photo URL at index %d is invalid: '%s'. URLs must start with http://, https://, or ftp://",
                        i, url);
                    logger.debug("Pet has invalid photo URL: {}", url);
                    return EvaluationOutcome.Fail.dataQualityFailure(reason);
                }
            }
        }
        return EvaluationOutcome.success();
    }

    /**
     * Validates Pet tags content and format.
     */
    private EvaluationOutcome validatePetTags(Pet pet) {
        if (pet.getTags() != null) {
            for (int i = 0; i < pet.getTags().size(); i++) {
                String tag = pet.getTags().get(i);
                if (tag == null || tag.trim().isEmpty()) {
                    String reason = String.format("Pet tag at index %d is invalid: %s. Tags cannot be null or empty",
                        i, tag == null ? "null" : "empty/whitespace");
                    logger.debug("Pet has invalid tag at index {}: {}", i, tag);
                    return EvaluationOutcome.Fail.dataQualityFailure(reason);
                }
            }
        }
        return EvaluationOutcome.success();
    }

    /**
     * Validates Pet category content.
     */
    private EvaluationOutcome validatePetCategory(Pet pet) {
        if (pet.getCategory() != null && pet.getCategory().trim().isEmpty()) {
            String reason = "Pet category is empty or contains only whitespace. If provided, category must have content";
            logger.debug("Pet has empty category");
            return EvaluationOutcome.Fail.dataQualityFailure(reason);
        }
        return EvaluationOutcome.success();
    }

    // ========================================
    // UTILITY METHODS
    // ========================================

    /**
     * Validates if a string is a valid URL format.
     */
    private boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        try {
            // Basic URL validation
            return url.startsWith("http://") || url.startsWith("https://") || url.startsWith("ftp://");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets criteria name for logging and identification.
     */
    public String getName() {
        return "IsValidPet";
    }

    /**
     * Gets criteria version for tracking.
     */
    public String getVersion() {
        return "1.0.0";
    }

    /**
     * Gets supported entity types.
     */
    public Set<String> getSupportedEntityTypes() {
        return Set.of("pet");
    }
}
