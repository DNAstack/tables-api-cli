package com.dnastack.ga4gh.dataset.cli.cmd;

import com.dnastack.ga4gh.dataset.cli.AuthOptions;
import com.dnastack.ga4gh.dataset.cli.Config;
import com.dnastack.ga4gh.dataset.cli.util.ConfigUtil;

import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;


@Command(name = "set-config", description = "Set configuration to a user-local config file",  requiredOptionMarker='*', sortOptions = false)
public class SetConfig implements Runnable {

    @Mixin
    private AuthOptions authOptions;

    private Config userConfig;

    @Override
    public void run() {
        authOptions.initAuth();
        userConfig = ConfigUtil.getUserConfig();

        if (authOptions.getApiUrl() != null) {
            userConfig.setApiUrl(authOptions.getApiUrl());
        }
        if (authOptions.getUsername() != null) {
            userConfig.setUsername(authOptions.getUsername());
        }
        if (authOptions.getPassword() != null) {
            userConfig.setPassword(authOptions.getPassword());
        }

        //save any changed config options.
        ConfigUtil.save(authOptions.getUserConfig());
        System.out.println(ConfigUtil.dump(authOptions.getUserConfig()));
    }
}
