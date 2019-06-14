package org.ga4gh.dataset.cli.cmd;

import org.ga4gh.dataset.cli.Config;
import org.ga4gh.dataset.cli.ConfigUtil;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "set-config", description = "Set configuration to a user-local config file")
public class SetConfig implements Runnable {

    @Option(names = "--api-url", description = "Dataset API Location")
    private String apiUrl;

    @Option(names = "--username", description = "Username for HTTP BASIC Authentication")
    private String username;

    @Option(names = "--password", description = "Password for HTTP BASIC Authentication")
    private String password;

    private Config userConfig;

    @Override
    public void run() {
        userConfig = ConfigUtil.getUserConfig();

        if (apiUrl != null) {
            userConfig.setApiUrl(apiUrl);
        }
        if (username != null) {
            userConfig.setUsername(username);
        }
        if (password != null) {
            userConfig.setPassword(password);
        }

        ConfigUtil.save(userConfig);
        System.out.println(ConfigUtil.dump(userConfig));
    }
}
