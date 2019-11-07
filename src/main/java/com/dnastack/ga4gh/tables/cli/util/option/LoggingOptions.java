package com.dnastack.ga4gh.tables.cli.util.option;

import lombok.Getter;
import picocli.CommandLine.Option;

public class LoggingOptions {

    @Getter
    @Option(names = "--debug", description = "Print DEBUG level information")
    private boolean debug;

    @Getter
    @Option(names = {"--stdout"}, description = "logging destination for std out")
    private String stdout;

    @Getter
    @Option(names = {"--stderr"}, description = "logging destination for std err")
    private String stderr;

}
