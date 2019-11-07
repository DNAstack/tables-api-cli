package com.dnastack.ga4gh.tables.cli.output;

import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.dnastack.ga4gh.tables.cli.output.publish.ABSPublisher;
import com.dnastack.ga4gh.tables.cli.output.publish.FileSystemPublisher;
import com.dnastack.ga4gh.tables.cli.output.publish.GCSPublisher;
import com.dnastack.ga4gh.tables.cli.output.publish.Publisher;
import com.dnastack.ga4gh.tables.cli.util.option.PublishOptions;
import java.io.IOException;
import java.net.URI;

public class PublishingOutputWriter implements OutputWriter {

    private final Publisher publisher;
    private int pageNum = 0;

    public PublishingOutputWriter(PublishOptions publishOptions) {
        String tableName = publishOptions.getDestinationTableName();
        URI publishUri = URI.create(publishOptions.getPublishDestination());
        //Local file
        if (publishUri.getScheme() == null || publishUri.getScheme().equals("file")) {
            publisher = new FileSystemPublisher(tableName, publishOptions.getPublishDestination());
        } else if (publishUri.getScheme().equals("gs")) {
            publisher = new GCSPublisher(tableName, publishOptions.getPublishDestination());
        } else if (publishUri.getScheme().equals("https") && publishUri.getHost()
            .endsWith("blob.core.windows.net")) {
            publisher = new ABSPublisher(tableName, publishOptions.getPublishDestination(), publishOptions
                .isGenerateSASPages());
        } else {
            throw new IllegalArgumentException("No publishers defined");
        }
    }

    @Override
    public void write(Table table) {
        publisher.publish(table);
    }

    @Override
    public void write(TableData tableData) {
        publisher.publish(tableData, pageNum);
        pageNum++;

    }

    @Override
    public void write(ListTableResponse tableResponse) {
        publisher.publish(tableResponse);
        pageNum++;
    }

    @Override
    public void writeSearchResult(TableData tableData) {
        if (pageNum == 0) {
            write(new Table(null, null, tableData.getDataModel()));
        }
        write(tableData);
    }

    @Override
    public void close() throws IOException {
        //
    }
}
