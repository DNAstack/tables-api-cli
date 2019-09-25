package org.ga4gh.dataset.cli;

import lombok.Getter;
import org.ga4gh.dataset.cli.util.ConfigUtil;
import picocli.CommandLine;

@Getter
public class AuthOptions {
    @CommandLine.Option(names = "--api-url", description = "Dataset API Location")
    private String apiUrl;

    @CommandLine.Option(names = "--username", description = "Username for HTTP BASIC Authentication")
    private String username;

    @CommandLine.Option(names = "--password", description = "Password for HTTP BASIC Authentication")
    private String password;

    private Config userConfig;

    public void initAuth(){
        //load current config
         userConfig = ConfigUtil.getUserConfig();

         //update config with the value of arguments passed.
        if (getApiUrl() != null) {
            userConfig.setApiUrl(getApiUrl());
        }
        if (getUsername() != null) {
            userConfig.setUsername(getUsername());
        }
        if (getPassword() != null) {
            userConfig.setPassword(getPassword());
        }

        //Transiently save the changes.  Changes will NOT be written out to
        //file unless explicitly saved.
        ConfigUtil.setConfig(userConfig);

    }

}
