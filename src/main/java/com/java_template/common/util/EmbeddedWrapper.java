package com.java_template.common.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EmbeddedWrapper {
    @JsonProperty("_embedded")
    private ObjectNodesWrapper embedded;

    @Getter
    @Setter
    public static class ObjectNodesWrapper {
        @JsonProperty("objectNodes")
        private List<TreeWrapper> objectNodes;
    }

    @Getter
    @Setter
    public static class TreeWrapper {
        private String id;
        private Object tree;
    }

    @Getter
    @Setter
    public static class SingleTreeWrapper {
        private Metadata meta;
        private Object tree;
    }

    @Getter
    @Setter
    public static class Metadata {
        private String id;
        private String type;
        private ModelKey modelKey;
    }

    @Getter
    @Setter
    public static class ModelKey {
        private String legalEntityId;
        private String name;
        private int version;
    }
}
