package org.ga4gh.dataset.cli.ga4gh;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Schema {
    @JsonProperty("$id")
    private String id;

    @JsonProperty("$schema")
    private String schema;

    @JsonProperty("$ref")
    private String ref;

    @JsonProperty("properties")
    private LinkedHashMap<String, Object> propertyMap;
}
