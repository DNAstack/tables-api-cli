package com.dnastack.ga4gh.tables.cli.util.option;

import com.dnastack.ga4gh.tables.cli.publisher.ABSPublisher;
import com.dnastack.ga4gh.tables.cli.publisher.FileSystemPublisher;
import com.dnastack.ga4gh.tables.cli.publisher.GCSPublisher;
import com.dnastack.ga4gh.tables.cli.publisher.NoopPublisher;
import com.dnastack.ga4gh.tables.cli.publisher.Publisher;
import java.net.URI;
import lombok.Getter;
import picocli.CommandLine;

@Getter
public class PublishOptions {

    @CommandLine.Option(names = {"-p", "--publish"},
        description = "Publish the results to a target destination. Currently Supported are [GCP,ABS,Local]. The Destination should be a valid URI depening on the location")
    private String publishDestination;

    @CommandLine.Option(names = "--generate-signed-page-urls",
        description = "When publishing to Azure Blob Storage, generates signed pagination urls (1 hr expiry).")
    private boolean generateSASPages;

    @CommandLine.Option(names = {"-N", "--publish-table-name"},
        description = "A different name to save this table to")
    private String destinationTableName;


    public boolean shouldPublish() {
        return publishDestination != null && !publishDestination.isEmpty();
    }

    public Publisher getPublisher(String tableName) {
        if (!shouldPublish()){
            return new NoopPublisher();
        }
        if (destinationTableName != null) {
            tableName = destinationTableName;
        }

        if (tableName == null) {
            throw new IllegalArgumentException("Cannot publish results, no table name provided");
        }

        URI publishUri = URI.create(publishDestination);

        //Local file
        if (publishUri.getScheme() == null || publishUri.getScheme().equals("file://")) {
            return new FileSystemPublisher(tableName, publishDestination);
        } else if (publishUri.getScheme().equals("gs://")) {
            return new GCSPublisher(tableName, publishDestination);
        } else if (publishUri.getScheme().equals("https://") && publishUri.getHost()
            .endsWith("blob.core.windows.net")) {
            return new ABSPublisher(tableName, publishDestination, generateSASPages);
        } else {
            return new NoopPublisher();
        }
    }
}
