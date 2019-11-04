package com.dnastack.ga4gh.dataset.cli.ga4gh;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Dataset {
    @JsonProperty("schema")
    private Schema schema;

    @JsonProperty("objects")
    private List<Map<String, Object>> objects;

    @JsonProperty("pagination")
    private Pagination pagination;
}
