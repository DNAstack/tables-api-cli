package com.dnastack.ga4gh.tables.cli.cmd;

import com.dnastack.ga4gh.tables.cli.util.ExceptionHandler;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.RunLast;

import java.io.IOException;
import java.util.Properties;


@Command(name = "tables", mixinStandardHelpOptions = true, version = {"tables-api-cli Version : ${tables-api-cli-version}"})
public class Main extends BaseCmd {

    private static CommandLine commandLine = setup();

    public static int runCommand(String... args) {
        return commandLine.execute(args);
    }

    public static void main(String... args) throws IOException {
        final Properties properties = new Properties();
        properties.load(Main.class.getClassLoader().getResourceAsStream(".properties"));
        System.setProperty("tables-api-cli-version", properties.getProperty("tables-api-cli-version"));
        System.exit(runCommand(args));
    }

    private static CommandLine setup() {
        return new CommandLine(new Main())
            .addSubcommand(new ConfigCmd())
            .addSubcommand(new Info())
            .addSubcommand(new Data())
            .addSubcommand(new ListTables())
            .addSubcommand(new Import())
            .addSubcommand(new Query())
            .addSubcommand(new Login())
            .setExecutionStrategy(new RunLast())
            .setExecutionExceptionHandler(new ExceptionHandler())
            .setCaseInsensitiveEnumValuesAllowed(true);
    }

    @Override
    public void runCmd() {
        commandLine.usage(System.out);
    }
}
