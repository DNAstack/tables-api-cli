package com.dnastack.ga4gh.tables.cli.util.option;

import com.dnastack.ga4gh.tables.cli.config.Config;
import com.dnastack.ga4gh.tables.cli.config.ConfigUtil;
import com.dnastack.ga4gh.tables.cli.util.InitializableOptions;
import lombok.Getter;
import picocli.CommandLine;

@Getter
public class AuthOptions implements InitializableOptions {

    @CommandLine.Option(names = "--api-url", description = "Table API Location")
    private String apiUrl;

    @CommandLine.Option(names = "--username", description = "Username for HTTP BASIC Authentication")
    private String username;

    @CommandLine.Option(names = "--password", arity = "0..1", interactive = true, description = "Password for HTTP BASIC Authentication")
    private char[] password;

    @CommandLine.Option(names = "--abs-container-account-key", description = "Key required to access Azure Blob Storage Containers")
    private String absAccountKey;

    @CommandLine.Option(names = "--abs-sas-delegation-key", description = "Key required to delegate access to Azure Blob Storage Containers")
    private String absSASDelegationKey;

    @CommandLine.Option(names = {"--access-token"},
        description = "Custom access token",
        required = false)
    private String customAccessToken;

    private Config userConfig;

    public void init() {
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
            userConfig.setPassword(new String(getPassword()));
        }

        if (getAbsAccountKey() != null) {
            userConfig.setAbsAccountKey(getAbsAccountKey());
        }

        if (getAbsSASDelegationKey() != null) {
            userConfig.setAbsAccountKey(getAbsSASDelegationKey());
        }

        if (getCustomAccessToken() != null) {
            userConfig.setCustomAccessToken(getCustomAccessToken());
        }

        //Transiently save the changes.  Changes will NOT be written out to
        //file unless explicitly saved.
        ConfigUtil.setConfig(userConfig);

    }

}
