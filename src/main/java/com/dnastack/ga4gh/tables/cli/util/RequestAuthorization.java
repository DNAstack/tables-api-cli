package com.dnastack.ga4gh.tables.cli.util;

import java.util.Base64;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class RequestAuthorization {

    private String accessToken;

    private String username;

    private String password;


    public String getAuthorizationHeader() {

        if (accessToken != null) {
            return "Bearer " + accessToken;
        } else if (username != null) {
            String headerBase = username + ":";
            if (password != null) {
                headerBase += password;
            }

            return "Basic " + Base64.getEncoder().encodeToString(headerBase.getBytes());
        } else {
            return null;
        }
    }

    public boolean hasAuth() {
        return accessToken != null || username != null;
    }


}
