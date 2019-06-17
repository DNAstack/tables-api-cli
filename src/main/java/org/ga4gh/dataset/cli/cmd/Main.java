package org.ga4gh.dataset.cli.cmd;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "datasets")
public class Main implements Runnable {

    public static void main(String... args) {
        System.exit(
                new CommandLine(new Main())
                        .addSubcommand(new SetConfig())
                        .addSubcommand(new List())
                        .addSubcommand(new Get())
                        .execute(args));
    }

    @Override
    public void run() {}
}
