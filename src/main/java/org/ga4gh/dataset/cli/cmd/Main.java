package org.ga4gh.dataset.cli.cmd;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "datasets")
public class Main implements Runnable {

    public static void main(String... args) {
        try {
            System.exit(
                    new CommandLine(new Main())
                            .addSubcommand(new SetConfig())
                            .addSubcommand(new List())
                            .addSubcommand(new Get())
                            .addSubcommand(new Download())
                            .execute(args));
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            System.exit(0);
        }
    }

    @Override
    public void run() {}
}
