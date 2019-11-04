package com.dnastack.ga4gh.dataset.cli;

import lombok.Getter;
import com.dnastack.ga4gh.dataset.cli.util.outputter.Outputter;
import picocli.CommandLine;

@Getter
public class OutputOptions {
    public enum OutputMode{
        JSON, TSV, CSV, TABLE, SILENT;
    }

    @CommandLine.Option(names = {"-o", "--output"}, description = "Valid values: ${COMPLETION-CANDIDATES}")
    private OutputMode outputMode = OutputMode.SILENT;

    public Outputter getOutputter(){
        return new Outputter(outputMode);
    }
}
