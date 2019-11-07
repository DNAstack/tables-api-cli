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
public class PublishOptions implements Cloneable {

    @CommandLine.Option(names = {"-p", "--publish"},
        required = true,
        description = "Publish the results to a target destination. Currently Supported are [GCP,ABS,Local]. The Destination should be a valid URI depening on the location")
    private String publishDestination;

    @CommandLine.Option(names = "--generate-signed-page-urls",
        description = "When publishing to Azure Blob Storage, generates signed pagination urls (1 hr expiry).")
    private boolean generateSASPages;

    @CommandLine.Option(names = {"-N", "--publish-table-name"},
        description = "A different name to save this table to", required = true)
    private String destinationTableName;


    @Override
    public PublishOptions clone() {
        return new PublishOptions(publishDestination, generateSASPages, destinationTableName);
    }
}
