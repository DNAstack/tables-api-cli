package com.dnastack.ga4gh.tables.cli.cmd;

import com.dnastack.ga4gh.tables.cli.config.ConfigUtil;
import com.dnastack.ga4gh.tables.cli.util.LoggedCommand;
import com.dnastack.ga4gh.tables.cli.util.option.LoggingOptions;
import lombok.Getter;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Parameters;


@Command(name = "config", mixinStandardHelpOptions = true, description = "Local configuration", requiredOptionMarker = '*', sortOptions = false)
public class ConfigCmd implements LoggedCommand, Runnable {

    @Mixin
    @Getter
    LoggingOptions loggingOptions;

    @Command(name = "init", mixinStandardHelpOptions = true, description = "Initialize configuration to a user-local config file", requiredOptionMarker = '*', sortOptions = false)
    public void initConfig() {
        loggingOptions.init();
        ConfigUtil.initializeConfig();
    }

    @Command(name = "set", mixinStandardHelpOptions = true, description = "Set configuration value", requiredOptionMarker = '*', sortOptions = false)
    public void setConfig(@Parameters(index = "0", paramLabel = "key", description = "key of value to set") String key,
        @Parameters(index = "1", paramLabel = "key", description = "key of value to set") String value) {
        loggingOptions.init();
        ConfigUtil.setConfigValue(key, value);
        ConfigUtil.save();
    }

    @Command(name = "get", mixinStandardHelpOptions = true, description = "get configuration value", requiredOptionMarker = '*', sortOptions = false)
    public void getConfig(@Parameters(index = "0", paramLabel = "key", description = "key of value to set") String key) {
        loggingOptions.init();
        ConfigUtil.printConfigValue(key);
    }

    @Command(name = "unset", mixinStandardHelpOptions = true, description = "Unset configuration value", requiredOptionMarker = '*', sortOptions = false)
    public void unset(@Parameters(index = "0", paramLabel = "key", description = "key of value to unset") String key) {
        loggingOptions.init();
        ConfigUtil.setConfigValue(key, null);
        ConfigUtil.save();
    }

    @Command(name = "list", mixinStandardHelpOptions = true, description = "List configuration values", requiredOptionMarker = '*', sortOptions = false)
    public void list() {
        loggingOptions.init();
        ConfigUtil.printConfig();
    }

    @Override
    public void run() {
        loggingOptions.init();
        CommandLine.usage(this, System.out);
    }
}
