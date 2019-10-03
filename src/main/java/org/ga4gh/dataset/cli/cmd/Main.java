package org.ga4gh.dataset.cli.cmd;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "datasets")
public class Main implements Runnable {

    public static int runCommand(String... args){
        return  new CommandLine(new Main())
                .addSubcommand(new SetConfig())
                .addSubcommand(new ListDatasets())
                .addSubcommand(new Get())
                .addSubcommand(new Import())
                .addSubcommand(new Query())
                .addSubcommand(new Login())
                .addSubcommand(new GetAccess())
                .setCaseInsensitiveEnumValuesAllowed(true)
                .execute(args);
    }
    public static void main(String... args) {
        try {

           System.exit(runCommand(args));

        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            System.exit(0);
        }
    }

    @Override
    public void run() {}
}
