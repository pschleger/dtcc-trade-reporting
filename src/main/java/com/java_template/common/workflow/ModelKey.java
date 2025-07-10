package com.java_template.common.workflow;

/**
 * Identifies a model by operationName and version.
 */

public record ModelKey(String modelName, String modelVersion) {
}
