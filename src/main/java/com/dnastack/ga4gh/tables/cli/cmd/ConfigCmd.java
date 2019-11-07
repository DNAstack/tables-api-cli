package com.dnastack.ga4gh.tables.cli.cmd;

import com.dnastack.ga4gh.tables.cli.config.ConfigUtil;
import java.util.HashMap;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;


@Command(name = "config", mixinStandardHelpOptions = true, description = "Local configuration", requiredOptionMarker = '*', sortOptions = false)
public class ConfigCmd extends BaseCmd {


    @Command(name = "init", mixinStandardHelpOptions = true, description = "Initialize configuration to a user-local config file", requiredOptionMarker = '*', sortOptions = false)
    public void initConfig() {
        ConfigUtil.initializeConfig();
    }

    @Command(name = "set", mixinStandardHelpOptions = true, description = "Set configuration value", requiredOptionMarker = '*', sortOptions = false)
    public void setConfig(@Parameters(index = "0", paramLabel = "key", description = "key of value to set") String key,
        @Parameters(index = "1", paramLabel = "value", description = "value to set") String value) {
        ConfigUtil.setConfigValue(key, value);
        ConfigUtil.save();
    }

    @Command(name = "get", mixinStandardHelpOptions = true, description = "get configuration value", requiredOptionMarker = '*', sortOptions = false)
    public void getConfig(@Parameters(index = "0", paramLabel = "key", description = "key of value to set") String key) {
        ConfigUtil.printConfigValue(key);
    }

    @Command(name = "unset", mixinStandardHelpOptions = true, description = "Unset configuration value", requiredOptionMarker = '*', sortOptions = false)
    public void unset(@Parameters(index = "0", paramLabel = "key", description = "key of value to unset") String key) {
        ConfigUtil.setConfigValue(key, null);
        ConfigUtil.save();
    }

    @Command(name = "list", mixinStandardHelpOptions = true, description = "List configuration values", requiredOptionMarker = '*', sortOptions = false)
    public void list() {
        ConfigUtil.printConfig();
    }

    @Command(name = "add-token", mixinStandardHelpOptions = true, description = "Add token to authenticate requests", requiredOptionMarker = '*', sortOptions = false)
    public void addToken(@Parameters(index = "0", paramLabel = "api_url", description = "url of requests token should be used for") String apiUrl,
        @Parameters(index = "1", paramLabel = "token", description = "Access token to set") String token) {
        var tokens = ConfigUtil.getUserConfig().getApiTokens();
        if (tokens == null) {
            tokens = new HashMap<>();
            ConfigUtil.getUserConfig().setApiTokens(tokens);
        }
        tokens.put(apiUrl, token);
        ConfigUtil.save();
    }

    @Command(name = "remove-token", mixinStandardHelpOptions = true, description = "Remove token from token store", requiredOptionMarker = '*', sortOptions = false)
    public void removeToken(@Parameters(index = "0", paramLabel = "api_url", description = "url of token to remove") String apiUrl,
        @Parameters(index = "1", paramLabel = "token", description = "Access token to set") String token) {
        var tokens = ConfigUtil.getUserConfig().getApiTokens();
        if (tokens == null) {
            tokens = new HashMap<>();
            ConfigUtil.getUserConfig().setApiTokens(tokens);
        }
        tokens.remove(apiUrl);
        ConfigUtil.save();
    }

    @Command(name = "list-tokens", mixinStandardHelpOptions = true, description = "List tokens which have been added for apis", requiredOptionMarker = '*', sortOptions = false)
    public void listTokens() {
        ConfigUtil.printTokenList();
    }

    @Override
    public void runCmd() {
        CommandLine.usage(this, System.out);
    }
}
