package com.dnastack.ga4gh.tables.cli.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Table implements Comparable<Table> {

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("data_model")
    private LinkedHashMap<String, Object> dataModel;

    @Override
    public int compareTo(Table o) {
        return this.name.compareTo(o.name);
    }

}
