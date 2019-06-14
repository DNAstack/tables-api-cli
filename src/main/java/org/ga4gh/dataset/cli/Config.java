package org.ga4gh.dataset.cli;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Config {

    @JsonProperty("api_url")
    private String apiUrl;

    private Auth auth;

    public void setUsername(String username) {
        if (auth == null) {
            auth = new Auth();
        }
        auth.setUsername(username);
    }

    public void setPassword(String password) {
        if (auth == null) {
            auth = new Auth();
        }
        auth.setPassword(password);
    }

    public String getUsername() {
        return auth == null ? null : auth.getUsername();
    }

    public String getPassword() {
        return auth == null ? null : auth.getPassword();
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Auth {
        private String username;
        private String password;
    }
}
