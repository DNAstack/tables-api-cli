package com.dnastack.ga4gh.tables.cli.util.option;

import com.dnastack.ga4gh.tables.cli.publisher.ABSPublisher;
import com.dnastack.ga4gh.tables.cli.publisher.GCSPublisher;
import com.dnastack.ga4gh.tables.cli.publisher.NoPublisher;
import com.dnastack.ga4gh.tables.cli.publisher.Publisher;
import lombok.Getter;
import picocli.CommandLine;

@Getter
public class PublishOptions {

    @CommandLine.Option(names = {"-p", "--publish-destination"},
        description = "A valid Azure Blob Storage URI, or GCS URI of the format gs://{bucket}/{blob}")
    private String publishDestination;

    @CommandLine.Option(names = "--generate-signed-page-urls",
        description = "When publishing to Azure Blob Storage, generates signed pagination urls (1 hr expiry).")
    private boolean generateSASPages;

    @CommandLine.Option(names = {"-N", "--publish-table-name"},
        description = "A different name to save this table to")
    private String destinationTableName;

    public Publisher getPublisher(String tableName) {
        if (destinationTableName != null) {
            tableName = destinationTableName;
        }

        if (publishDestination == null || publishDestination.isBlank() || tableName == null) {
            return new NoPublisher();
        }
        if (!publishDestination.startsWith("gs://")) {
            return new ABSPublisher(tableName, publishDestination, generateSASPages);
        }
        return new GCSPublisher(tableName, publishDestination);
    }
}
