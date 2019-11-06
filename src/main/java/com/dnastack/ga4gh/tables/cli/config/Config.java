package com.dnastack.ga4gh.tables.cli.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class Config {

    @ConfigProperty(key = "api_url", description = "The default api url to use for operations")
    @JsonProperty("api_url")
    String apiUrl;


    @ConfigProperty(key = "username", description = "The username to use for authenticating requests")
    @JsonProperty("username")
    String username;


    @ConfigProperty(key = "password", description = "The password to use for authentication", obscure = true)
    @JsonProperty("password")
    String password;


    @ConfigProperty(key = "abs_account_key", description = "Azure Blob Storage account key")
    @JsonProperty("abs_account_key")
    String absAccountKey;

    @ConfigProperty(key = "abs_sas_delegation_key", description = "Azure Blob Storage SAS delegation key")
    @JsonProperty("abs_sas_delegation_key")
    String absSASDelegationKey;

    @JsonIgnore
    String customAccessToken;
    /**
     * A token map, where the Key == The API URL. This is to facilitate rapid context switching
     */
    @JsonProperty("api_tokens")
    Map<String, String> apiTokens;

    @JsonIgnore
    public void addApiToken(String token) {
        String api = ConfigUtil.getUserConfig().getApiUrl();
        if (api == null) {
            customAccessToken = api;
        } else {
            if (apiTokens == null) {
                apiTokens = new HashMap<>();
            }
            apiTokens.put(api, token);
        }
    }

    @JsonIgnore
    public String getTokenOrNull() {
        if (customAccessToken != null) {
            return customAccessToken;
        } else {
            String api = ConfigUtil.getUserConfig().getApiUrl();
            return api != null && apiTokens != null ? apiTokens.get(api) : null;

        }
    }
}
