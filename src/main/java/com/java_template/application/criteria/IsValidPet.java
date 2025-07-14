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
     * Validates all aspects of Pet entity and provides specific reasons for failures.
     * Returns EvaluationOutcome.Success for success, EvaluationOutcome.Fail for failures.
     */
    private EvaluationOutcome validatePet(Pet pet) {
        // Check if Pet entity exists and is valid
        if (pet == null) {
            logger.debug("Pet entity is null");
            return EvaluationOutcome.Fail.structuralFailure("Pet entity is null");
        }

        // Use Pet's built-in validation
        if (!pet.isValid()) {
            logger.debug("Pet entity failed basic validation");
            return EvaluationOutcome.Fail.structuralFailure("Pet entity failed basic validation (missing required fields)");
        }

        // Validate basic structure and required fields
        EvaluationOutcome structureResult = validateBasicStructure(pet);
        if (structureResult instanceof EvaluationOutcome.Fail) {
            return structureResult;
        }

        // Validate business rules
        EvaluationOutcome businessResult = validateBusinessRules(pet);
        if (businessResult instanceof EvaluationOutcome.Fail) {
            return businessResult;
        }

        // Validate data quality
        EvaluationOutcome qualityResult = validateDataQuality(pet);
        if (qualityResult instanceof EvaluationOutcome.Fail) {
            return qualityResult;
        }

        logger.debug("Pet passed all validation checks: id={}, name={}", pet.getId(), pet.getName());
        return EvaluationOutcome.success();
    }

    /**
     * Validates basic Pet entity structure and required fields with detailed outcomes.
     * Returns EvaluationOutcome.Success for success, EvaluationOutcome.Fail for failures.
     */
    private EvaluationOutcome validateBasicStructure(Pet pet) {
        // Validate required fields
        if (pet.getId() == null || pet.getId() <= 0) {
            String reason = pet.getId() == null ?
                "Pet ID is null" :
                String.format("Pet ID must be positive, got: %d", pet.getId());
            logger.debug("Pet has invalid ID: {}", pet.getId());
            return EvaluationOutcome.Fail.structuralFailure(reason);
        }

        if (pet.getName() == null || pet.getName().trim().isEmpty()) {
            String reason = pet.getName() == null ?
                "Pet name is null" :
                "Pet name is empty or contains only whitespace";
            logger.debug("Pet has invalid name: {}", pet.getName());
            return EvaluationOutcome.Fail.structuralFailure(reason);
        }

        logger.debug("Pet passed basic structure validation: id={}, name={}", pet.getId(), pet.getName());
        return EvaluationOutcome.success();
    }

    /**
     * Validates Pet entity against business rules with detailed outcomes.
     * Returns EvaluationOutcome.Success for success, EvaluationOutcome.Fail for failures.
     */
    private EvaluationOutcome validateBusinessRules(Pet pet) {
        // Validate status if present
        if (pet.getStatus() != null && !VALID_STATUSES.contains(pet.getStatus().toLowerCase())) {
            String reason = String.format("Pet status '%s' is invalid. Valid statuses are: %s",
                pet.getStatus(), String.join(", ", VALID_STATUSES));
            logger.debug("Pet has invalid status: {}", pet.getStatus());
            return EvaluationOutcome.Fail.businessRuleFailure(reason);
        }

        // Validate name length
        if (pet.getName() != null) {
            int nameLength = pet.getName().trim().length();
            if (nameLength < MIN_NAME_LENGTH || nameLength > MAX_NAME_LENGTH) {
                String reason = String.format("Pet name length %d is invalid. Must be between %d and %d characters",
                    nameLength, MIN_NAME_LENGTH, MAX_NAME_LENGTH);
                logger.debug("Pet name length {} is invalid", nameLength);
                return EvaluationOutcome.Fail.businessRuleFailure(reason);
            }
        }

        // Validate tags count
        if (pet.getTags() != null && pet.getTags().size() > MAX_TAGS) {
            String reason = String.format("Pet has too many tags (%d). Maximum allowed is %d",
                pet.getTags().size(), MAX_TAGS);
            logger.debug("Pet has too many tags: {}", pet.getTags().size());
            return EvaluationOutcome.Fail.businessRuleFailure(reason);
        }

        logger.debug("Pet passed business rules validation");
        return EvaluationOutcome.success();
    }

    /**
     * Validates Pet entity data quality and consistency with detailed outcomes.
     * Returns EvaluationOutcome.Success for success, EvaluationOutcome.Fail for failures.
     */
    private EvaluationOutcome validateDataQuality(Pet pet) {
        // Validate photo URLs if present
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

        // Validate tags if present
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

        // Validate category if present
        if (pet.getCategory() != null && pet.getCategory().trim().isEmpty()) {
            String reason = "Pet category is empty or contains only whitespace. If provided, category must have content";
            logger.debug("Pet has empty category");
            return EvaluationOutcome.Fail.dataQualityFailure(reason);
        }

        logger.debug("Pet passed data quality validation");
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
