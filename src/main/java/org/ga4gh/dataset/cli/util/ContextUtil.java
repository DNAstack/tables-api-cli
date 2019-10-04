package org.ga4gh.dataset.cli.util;

import com.dnastack.ddap.cli.login.Context;
import com.dnastack.ddap.cli.login.ContextDAO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ContextUtil {
    public static Context loadContextOrExit(ContextDAO contextDAO) {
        try {
            return contextDAO.load();
        } catch (ContextDAO.PersistenceException e) {
            System.err.println(e.getMessage());
            System.err.println("Try running the 'login' command.");
            //throw new CommandLineClient.SystemExit(1, e);
            System.exit(1);
        }
        throw new RuntimeException("Didn't happen.");
    }

    @Setter
    @Getter
    @AllArgsConstructor
    public static class TokenData {
        final String token;

    }

    public static String getAccessToken() {
        String path = System.getenv("HOME") + "/.dataset-api-cli";
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        TokenData tokenData;
        try {
            tokenData = yamlMapper.readValue(new File(path), TokenData.class);

        } catch (IOException e ) {
            throw new RuntimeException("Unable to read access token.");
        }
        return tokenData.getToken();
    }
}
