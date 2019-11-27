package com.dnastack.ga4gh.tables.cli.util.option;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picocli.CommandLine;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OutputOptions implements Cloneable {

    public enum OutputMode {
        JSON, TSV, CSV, TABLE;
    }

    @CommandLine.Option(names = {"-f", "--format"}, description = "Valid values: ${COMPLETION-CANDIDATES}")
    private OutputMode outputMode = null;


    @CommandLine.Option(names = {"-o", "--output"}, description = "Output destination")
    private String destination = null;

    @CommandLine.Option(names = "--generate-signed-page-urls",
        description = "When publishing to Azure Blob Storage, generates signed pagination urls (1 hr expiry).")
    private boolean generateSASPages = false;

    @CommandLine.Option(names = {"-N", "--table-name"},
        description = "A different name to save this table to")
    private String destinationTableName = null;

    @Override
    public OutputOptions clone() {
        return new OutputOptions(outputMode, destination, generateSASPages, destinationTableName);
    }
}
