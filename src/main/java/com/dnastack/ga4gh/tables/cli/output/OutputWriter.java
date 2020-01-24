package com.dnastack.ga4gh.tables.cli.output;

import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.dnastack.ga4gh.tables.cli.output.publish.*;
import com.dnastack.ga4gh.tables.cli.util.option.OutputOptions;
import com.dnastack.ga4gh.tables.cli.util.option.OutputOptions.OutputMode;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;

public class OutputWriter implements Closeable {

    private final Publisher publisher;
    private int pageNum = 0;

    public OutputWriter(OutputOptions publishOptions) {
        String tableName = publishOptions.getDestinationTableName();
        if (publishOptions.getDestination() == null) {
            if (publishOptions.getOutputMode() == null) {
                publishOptions.setOutputMode(OutputMode.TABLE);
            }
            publisher = new ConsolePublisher(publishOptions.getOutputMode(), publishOptions
                    .getDestinationTableName(), null);
        } else {
            URI publishUri = URI.create(publishOptions.getDestination());
            String destination = publishOptions.getDestination();
            OutputMode mode = publishOptions.getOutputMode();

            if (publishUri.getScheme() == null || publishUri.getScheme().equals("file")) {
                publisher = new FileSystemPublisher(mode, tableName, destination);
            } else if (publishUri.getScheme().equals("gs")) {
                publisher = new GCSPublisher(mode, tableName, destination);
            } else if (publishUri.getScheme().equals("s3")) {
                publisher = new AWSPublisher(mode, tableName, destination);
            } else if (publishUri.getScheme().equals("https") && publishUri.getHost()
                    .endsWith("blob.core.windows.net")) {
                publisher = new ABSPublisher(mode, tableName, destination, publishOptions.isGenerateSASPages());
            } else {
                throw new IllegalArgumentException("No Output writers defined");
            }
        }
    }

    public void write(Table table) {
        publisher.publish(table);
    }

    public void write(TableData tableData) {
        publisher.publish(tableData, pageNum);
        pageNum++;

    }

    public void write(ListTableResponse tableResponse) {
        publisher.publish(tableResponse);
        pageNum++;
    }

    public void writeSearchResult(TableData tableData) {
        if (pageNum == 0) {
            if (!(publisher instanceof ConsolePublisher)) {
                write(new Table(null, null, tableData.getDataModel()));
            }
        }
        write(tableData);
    }

    public Boolean bucketIsEmpty() {
        return publisher.isBucketEmpty();
    }

    @Override
    public void close() throws IOException {
        if (publisher instanceof Closeable) {
            ((Closeable) publisher).close();
        }
    }
}
