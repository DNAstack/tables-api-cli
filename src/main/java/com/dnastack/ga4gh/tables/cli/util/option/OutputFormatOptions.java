package com.dnastack.ga4gh.tables.cli.util.option;

import lombok.Getter;
import picocli.CommandLine;

@Getter
public class OutputFormatOptions {

    public enum OutputMode {
        JSON, TSV, CSV, TABLE;
    }

    @CommandLine.Option(names = {"-o", "--output"}, description = "Valid values: ${COMPLETION-CANDIDATES}")
    private OutputMode outputMode = null;

}
