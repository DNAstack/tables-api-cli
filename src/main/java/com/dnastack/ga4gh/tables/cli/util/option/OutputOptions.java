package com.dnastack.ga4gh.tables.cli.util.option;

import com.dnastack.ga4gh.tables.cli.util.outputter.Outputter;
import lombok.Getter;
import picocli.CommandLine;

@Getter
public class OutputOptions {

    public enum OutputMode {
        JSON, TSV, CSV, TABLE, SILENT;
    }

    @CommandLine.Option(names = {"-o", "--output"}, description = "Valid values: ${COMPLETION-CANDIDATES}")
    private OutputMode outputMode = OutputMode.TABLE;

    public Outputter getOutputter() {
        return new Outputter(outputMode, System.out);
    }
}
