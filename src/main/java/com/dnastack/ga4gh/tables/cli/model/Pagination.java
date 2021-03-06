package com.dnastack.ga4gh.tables.cli.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Pagination {


    @JsonProperty("next_page_url")
    private String nextPageUrl;

    @JsonProperty("previous_page_url")
    private String previousPageUrl;

}
