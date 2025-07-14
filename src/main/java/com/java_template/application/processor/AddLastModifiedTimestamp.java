package com.java_template.application.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.java_template.application.entity.pet.Pet;
import com.java_template.common.serializer.ErrorInfo;
import com.java_template.common.serializer.ProcessorSerializer;
import com.java_template.common.serializer.SerializerFactory;
import com.java_template.common.serializer.StandardErrorCodes;
import com.java_template.common.workflow.CyodaEventContext;
import com.java_template.common.workflow.CyodaProcessor;
import com.java_template.common.workflow.OperationSpecification;
import com.java_template.common.config.Config;
import org.cyoda.cloud.api.event.common.ModelSpec;
import org.cyoda.cloud.api.event.processing.EntityProcessorCalculationRequest;
import org.cyoda.cloud.api.event.processing.EntityProcessorCalculationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ABOUTME: Processor that adds last modified timestamp to Pet entities using ProcessingChaining.
 * Demonstrates modern fluent entity processing with validation, transformation, and error handling.
 */
@Component
public class AddLastModifiedTimestamp implements CyodaProcessor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // Business constants
    private static final Set<String> VALID_STATUSES = Set.of("available", "pending", "sold");
    private static final Set<String> VALID_CATEGORIES = Set.of("dog", "cat", "bird", "fish", "reptile", "other");
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_TAGS = 10;
    private static final int MAX_PHOTO_URLS = 20;

    private final ProcessorSerializer serializer;
    private final ObjectMapper objectMapper;

    public AddLastModifiedTimestamp(SerializerFactory serializerFactory, ObjectMapper objectMapper) {
        this.serializer = serializerFactory.getDefaultProcessorSerializer();
        this.objectMapper = objectMapper;
        logger.info("AddLastModifiedTimestamp processor initialized with SerializerFactory");
    }

    @Override
    public EntityProcessorCalculationResponse process(CyodaEventContext<EntityProcessorCalculationRequest> context) {
        EntityProcessorCalculationRequest request = context.getEvent();
        logger.info("Processing Pet entity timestamp update for request: {}", request.getId());

        // Modern fluent entity processing with ProcessingChaining - individual business logic steps
        return serializer.withRequest(request)
                .toEntity(Pet.class)
                .withErrorHandler(this::createPetProcessingError)
                .validate(this::isValidPet, "Pet entity is invalid (missing ID or name)")
                .map(this::createPetCopy)
                .map(this::normalizeStatus)
                .map(this::normalizeCategory)
                .map(this::cleanAndValidateTags)
                .map(this::cleanAndValidatePhotoUrls)
                .map(this::trimAndValidateName)
                .validate(this::validatePetBusinessRules, "Pet entity failed business validation")
                .map(this::addTimestampAndFinalize)
                .complete();
    }

    @Override
    public boolean supports(OperationSpecification modelKey) {
        if (modelKey instanceof OperationSpecification.Entity entitySpec) {
            ModelSpec modelSpec = entitySpec.modelKey();
            return "pet".equals(modelSpec.getName()) &&
                    Integer.parseInt(Config.ENTITY_VERSION) == modelSpec.getVersion();
        }
        return false;
    }

    // ========================================
    // PROCESSING CHAIN METHODS
    // ========================================

    /**
     * Creates custom error information for Pet processing failures.
     * Provides detailed context about the Pet entity and error.
     */
    private ErrorInfo createPetProcessingError(Throwable error, Pet pet) {
        String petInfo = pet != null ? "Pet ID: " + pet.getId() : "unknown pet";
        String errorMessage = "Failed to process " + petInfo + ": " + error.getMessage();

        if (error instanceof IllegalArgumentException) {
            return ErrorInfo.validationError(errorMessage);
        } else {
            return ErrorInfo.processingError(errorMessage);
        }
    }

    /**
     * Validates that a Pet entity has required fields.
     */
    private boolean isValidPet(Pet pet) {
        return pet != null && pet.getId() != null && pet.getName() != null && !pet.getName().trim().isEmpty();
    }

    /**
     * Validates Pet entity against business rules.
     * Returns true if valid, throws IllegalArgumentException with details if invalid.
     */
    private boolean validatePetBusinessRules(Pet pet) {
        List<String> errors = new ArrayList<>();

        // Validate status
        if (pet.getStatus() != null && !VALID_STATUSES.contains(pet.getStatus().toLowerCase())) {
            errors.add("Invalid pet status: " + pet.getStatus());
        }

        // Validate name length
        if (pet.getName() != null && pet.getName().length() > MAX_NAME_LENGTH) {
            errors.add("Pet name too long (max " + MAX_NAME_LENGTH + " characters)");
        }

        // Validate tags count
        if (pet.getTags() != null && pet.getTags().size() > MAX_TAGS) {
            errors.add("Too many tags (max " + MAX_TAGS + " allowed)");
        }

        // Validate photo URLs count
        if (pet.getPhotoUrls() != null && pet.getPhotoUrls().size() > MAX_PHOTO_URLS) {
            errors.add("Too many photo URLs (max " + MAX_PHOTO_URLS + " allowed)");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", errors));
        }

        return true;
    }

    /**
     * Adds timestamp and performs final processing on the Pet entity.
     */
    private Pet addTimestampAndFinalize(Pet pet) {
        pet.addLastModifiedTimestamp();
        logger.debug("Added timestamp to Pet: {}", pet.getId());
        return pet;
    }

    // ========================================
    // INDIVIDUAL BUSINESS LOGIC CHAIN METHODS
    // ========================================

    /**
     * Creates a deep copy of Pet entity for safe processing.
     * This is the first step in the business logic chain.
     */
    private Pet createPetCopy(Pet original) {
        logger.debug("Creating copy for Pet: {}", original.getId());
        Pet copy = new Pet();
        copy.setId(original.getId());
        copy.setName(original.getName());
        copy.setStatus(original.getStatus());
        copy.setCategory(original.getCategory());
        copy.setLastModified(original.getLastModified());

        // Deep copy lists
        if (original.getTags() != null) {
            copy.setTags(new ArrayList<>(original.getTags()));
        }
        if (original.getPhotoUrls() != null) {
            copy.setPhotoUrls(new ArrayList<>(original.getPhotoUrls()));
        }

        return copy;
    }

    /**
     * Normalizes the Pet status using the entity's built-in method.
     */
    private Pet normalizeStatus(Pet pet) {
        logger.debug("Normalizing status for Pet: {}", pet.getId());
        pet.normalizeStatus();
        return pet;
    }

    /**
     * Validates and normalizes the Pet category.
     */
    private Pet normalizeCategory(Pet pet) {
        logger.debug("Normalizing category for Pet: {}", pet.getId());
        if (pet.getCategory() != null) {
            pet.setCategory(pet.getCategory().toLowerCase());
            if (!VALID_CATEGORIES.contains(pet.getCategory())) {
                pet.setCategory("other");
                logger.debug("Normalized invalid category to 'other' for Pet: {}", pet.getId());
            }
        }
        return pet;
    }

    /**
     * Cleans and validates Pet tags.
     */
    private Pet cleanAndValidateTags(Pet pet) {
        logger.debug("Cleaning and validating tags for Pet: {}", pet.getId());
        if (pet.getTags() != null) {
            List<String> cleanedTags = pet.getTags().stream()
                    .filter(tag -> tag != null && !tag.trim().isEmpty())
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .distinct()
                    .limit(MAX_TAGS)
                    .collect(Collectors.toList());
            pet.setTags(cleanedTags);
        }
        return pet;
    }

    /**
     * Validates and cleans Pet photo URLs.
     */
    private Pet cleanAndValidatePhotoUrls(Pet pet) {
        logger.debug("Cleaning and validating photo URLs for Pet: {}", pet.getId());
        if (pet.getPhotoUrls() != null) {
            List<String> validUrls = pet.getPhotoUrls().stream()
                    .filter(this::isValidUrl)
                    .limit(MAX_PHOTO_URLS)
                    .collect(Collectors.toList());
            pet.setPhotoUrls(validUrls);
        }
        return pet;
    }

    /**
     * Trims and validates Pet name length.
     */
    private Pet trimAndValidateName(Pet pet) {
        logger.debug("Trimming and validating name for Pet: {}", pet.getId());
        if (pet.getName() != null) {
            String trimmedName = pet.getName().trim();
            if (trimmedName.length() > MAX_NAME_LENGTH) {
                trimmedName = trimmedName.substring(0, MAX_NAME_LENGTH);
                logger.debug("Truncated Pet name to {} characters for Pet: {}", MAX_NAME_LENGTH, pet.getId());
            }
            pet.setName(trimmedName);
        }
        return pet;
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
     * Gets processor name for logging and identification.
     */
    public String getName() {
        return "AddLastModifiedTimestamp";
    }

    /**
     * Gets processor version for tracking.
     */
    public String getVersion() {
        return "3.1.0";
    }

    /**
     * Gets supported entity types.
     */
    public Set<String> getSupportedEntityTypes() {
        return Set.of("pet");
    }
}
