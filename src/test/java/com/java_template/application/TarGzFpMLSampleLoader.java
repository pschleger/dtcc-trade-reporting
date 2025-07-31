package com.java_template.application;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for loading FpML samples directly from tar.gz archives.
 * This provides efficient streaming access to test samples without filesystem extraction.
 */
@Slf4j
public class TarGzFpMLSampleLoader {

    /**
     * Loads all XML files from a tar.gz archive in the classpath.
     *
     * @param tarGzPath Path to the tar.gz file in the classpath (e.g., "fpml-samples.tar.gz")
     * @return List of FpML samples with their content and metadata
     */
    public static List<FpMLSample> loadSamplesFromTarGz(String tarGzPath) throws IOException {
        List<FpMLSample> samples = new ArrayList<>();

        ClassPathResource tarGzResource = new ClassPathResource(tarGzPath);
        if (!tarGzResource.exists()) {
            log.warn("Tar.gz file not found: {}", tarGzPath);
            return samples;
        }

        try (InputStream inputStream = tarGzResource.getInputStream();
             GzipCompressorInputStream gzipStream = new GzipCompressorInputStream(inputStream);
             TarArchiveInputStream tarStream = new TarArchiveInputStream(gzipStream)) {

            TarArchiveEntry entry;
            while ((entry = tarStream.getNextEntry()) != null) {
                if (!entry.isDirectory() &&
                    entry.getName().toLowerCase().endsWith(".xml") &&
                    !entry.getName().contains("__MACOSX") &&
                    !entry.getName().contains("/._")) {
                    try {
                        FpMLSample sample = loadSampleFromTarEntry(tarStream, entry);
                        samples.add(sample);
                        log.debug("Loaded FpML sample: {} ({} bytes)", sample.getFileName(), sample.getContent().length());
                    } catch (Exception e) {
                        log.warn("Failed to load sample from {}: {}", entry.getName(), e.getMessage());
                    }
                }
            }
        }

        log.info("Loaded {} FpML samples from {}", samples.size(), tarGzPath);
        return samples;
    }

    /**
     * Loads samples with filtering by path patterns.
     */
    public static List<FpMLSample> loadSamplesFromTarGz(String tarGzPath, String... pathFilters) throws IOException {
        List<FpMLSample> allSamples = loadSamplesFromTarGz(tarGzPath);

        if (pathFilters == null || pathFilters.length == 0) {
            return allSamples;
        }

        List<FpMLSample> filteredSamples = new ArrayList<>();
        for (FpMLSample sample : allSamples) {
            for (String filter : pathFilters) {
                if (sample.getPath().contains(filter)) {
                    filteredSamples.add(sample);
                    break;
                }
            }
        }

        log.info("Filtered {} samples to {} using filters: {}",
                allSamples.size(), filteredSamples.size(), String.join(", ", pathFilters));
        return filteredSamples;
    }

    /**
     * Loads samples from specific product type directories.
     */
    public static List<FpMLSample> loadSamplesByProductTypes(String tarGzPath, String... productTypes) throws IOException {
        List<String> pathFilters = new ArrayList<>();
        for (String productType : productTypes) {
            pathFilters.add("samples/" + productType + "/");
        }
        return loadSamplesFromTarGz(tarGzPath, pathFilters.toArray(new String[0]));
    }

    /**
     * Loads a limited number of samples from each product type for testing.
     */
    public static List<FpMLSample> loadRepresentativeSamples(String tarGzPath, int samplesPerType) throws IOException {
        List<FpMLSample> allSamples = loadSamplesFromTarGz(tarGzPath);

        // Group samples by product type
        Map<String, List<FpMLSample>> samplesByType = allSamples.stream()
                .collect(java.util.stream.Collectors.groupingBy(FpMLSample::getProductType));

        List<FpMLSample> representativeSamples = new ArrayList<>();
        for (Map.Entry<String, List<FpMLSample>> entry : samplesByType.entrySet()) {
            List<FpMLSample> typeSamples = entry.getValue();
            int limit = Math.min(samplesPerType, typeSamples.size());
            representativeSamples.addAll(typeSamples.subList(0, limit));
        }

        log.info("Selected {} representative samples from {} product types (max {} per type)",
                representativeSamples.size(), samplesByType.size(), samplesPerType);
        return representativeSamples;
    }

    /**
     * Loads a single sample from a tar entry.
     */
    private static FpMLSample loadSampleFromTarEntry(TarArchiveInputStream tarStream, TarArchiveEntry entry) throws IOException {
        byte[] buffer = new byte[8192];
        StringBuilder contentBuilder = new StringBuilder();

        int bytesRead;
        while ((bytesRead = tarStream.read(buffer)) != -1) {
            contentBuilder.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
        }

        String content = contentBuilder.toString();
        String fileName = extractFileName(entry.getName());
        String productType = detectProductType(entry.getName());

        FpMLSample sample = new FpMLSample();
        sample.setPath(entry.getName());
        sample.setFileName(fileName);
        sample.setContent(content);
        sample.setProductType(productType);
        sample.setFileSize(content.length());
        sample.setCompressed(true);

        return sample;
    }

    /**
     * Extracts the filename from a full path.
     */
    private static String extractFileName(String fullPath) {
        int lastSlash = fullPath.lastIndexOf('/');
        return lastSlash >= 0 ? fullPath.substring(lastSlash + 1) : fullPath;
    }

    /**
     * Detects the product type from the path and content.
     * Uses the FpML official sample directory structure for accurate classification.
     */
    private static String detectProductType(String path) {
        String lowerPath = path.toLowerCase();

        // Detect by FpML official directory structure (samples/product-type/)
        if (lowerPath.contains("samples/fx-derivatives/")) {
            return "FX Derivatives";
        } else if (lowerPath.contains("samples/interest-rate-derivatives/")) {
            return "Interest Rate Derivatives";
        } else if (lowerPath.contains("samples/credit-derivatives/")) {
            return "Credit Derivatives";
        } else if (lowerPath.contains("samples/equity-options/")) {
            return "Equity Options";
        } else if (lowerPath.contains("samples/equity-swaps/")) {
            return "Equity Swaps";
        } else if (lowerPath.contains("samples/equity-forwards/")) {
            return "Equity Forwards";
        } else if (lowerPath.contains("samples/commodity-derivatives/")) {
            return "Commodity Derivatives";
        } else if (lowerPath.contains("samples/bond-options/")) {
            return "Bond Options";
        } else if (lowerPath.contains("samples/variance-swaps/")) {
            return "Variance Swaps";
        } else if (lowerPath.contains("samples/volatility-swaps/")) {
            return "Volatility Swaps";
        } else if (lowerPath.contains("samples/correlation-swaps/")) {
            return "Correlation Swaps";
        } else if (lowerPath.contains("samples/dividend-swaps/")) {
            return "Dividend Swaps";
        } else if (lowerPath.contains("samples/inflation-swaps/")) {
            return "Inflation Swaps";
        } else if (lowerPath.contains("samples/loan/")) {
            return "Loan";
        } else if (lowerPath.contains("samples/repo/")) {
            return "Repo";
        } else if (lowerPath.contains("samples/securities/")) {
            return "Securities";
        }

        // Fallback to legacy detection for non-standard paths
        if (lowerPath.contains("fx-") || lowerPath.contains("fx_")) {
            return "FX Derivatives";
        } else if (lowerPath.contains("ird-") || lowerPath.contains("interest")) {
            return "Interest Rate Derivatives";
        } else if (lowerPath.contains("cd-") || lowerPath.contains("credit")) {
            return "Credit Derivatives";
        } else if (lowerPath.contains("eqd-") || lowerPath.contains("equity")) {
            return "Equity Derivatives";
        } else if (lowerPath.contains("com-") || lowerPath.contains("commodity")) {
            return "Commodity Derivatives";
        }

        return "Unknown";
    }

    /**
     * Represents a single FpML sample loaded from a tar.gz archive.
     */
    @Data
    public static class FpMLSample {
        private String path;
        private String fileName;
        private String content;
        private String productType;
        private int fileSize;
        private boolean compressed;

        /**
         * Checks if this sample is expected to fail processing.
         */
        public boolean isExpectedToFail() {
            String lowerName = fileName.toLowerCase();
            return lowerName.contains("invalid") ||
                   lowerName.contains("malformed") ||
                   lowerName.contains("error") ||
                   lowerName.contains("bad");
        }

        /**
         * Returns the FpML version from the content if detectable.
         */
        public String getFpmlVersion() {
            if (content.contains("fpmlVersion=\"5-13\"")) {
                return "5.13";
            } else if (content.contains("fpmlVersion=\"5-12\"")) {
                return "5.12";
            } else if (content.contains("fpmlVersion=\"5-11\"")) {
                return "5.11";
            } else if (content.contains("version=\"5-13\"")) {
                return "5.13";
            } else if (content.contains("version=\"5-12\"")) {
                return "5.12";
            }
            return "Unknown";
        }

        /**
         * Returns the root element type.
         */
        public String getRootElementType() {
            if (content.contains("<requestConfirmation")) {
                return "requestConfirmation";
            } else if (content.contains("<FpML")) {
                return "FpML";
            } else if (content.contains("<dataDocument")) {
                return "dataDocument";
            }
            return "Unknown";
        }
    }
}
