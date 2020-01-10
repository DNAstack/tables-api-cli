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

    public static void main(String... args){

        try {
            final Properties properties = new Properties();
            properties.load(Main.class.getClassLoader().getResourceAsStream(".properties"));
            System.setProperty("tables-api-cli-version", properties.getProperty("tables-api-cli-version"));
        } catch (IOException | NullPointerException | IllegalArgumentException ex) {
            System.setProperty("tables-api-cli-version", "Unknown value");
            System.out.println("There was an issue reading the version value. The cause was most likely a\n" +
                               "compilation error with the .jar file. Please try re-downloading the file\n" +
                               "from our official github repo : https://github.com/DNAstack/tables-api-cli\n\n" +
                               "You may continue using this API as all other functionality should be unaffected by\n" +
                               "this error. If you encounter any more issues, please re-download the file.\n");
        }

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
