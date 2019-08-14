package org.ga4gh.dataset.cli;

import lombok.Getter;
import org.ga4gh.dataset.cli.util.Outputter;
import picocli.CommandLine;

@Getter
public class OutputOptions {
    public enum OutputMode{
        JSON, TSV, CSV, TABLE;
    }

    @CommandLine.Option(names = {"-o", "--output"}, description = "Valid values: ${COMPLETION-CANDIDATES}")
    private OutputMode outputMode = OutputMode.TABLE;

    public Outputter getOutputter(){
        return new Outputter(outputMode);
    }
}
