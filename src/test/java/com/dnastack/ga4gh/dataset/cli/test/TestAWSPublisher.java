package com.dnastack.ga4gh.dataset.cli.test;

import com.dnastack.ga4gh.tables.cli.config.Config;
import com.dnastack.ga4gh.tables.cli.config.ConfigUtil;
import com.dnastack.ga4gh.tables.cli.input.AWSTableFetcher;
import com.dnastack.ga4gh.tables.cli.input.TableFetcher;
import com.dnastack.ga4gh.tables.cli.input.TableFetcherFactory;
import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Pagination;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.dnastack.ga4gh.tables.cli.output.OutputWriter;
import com.dnastack.ga4gh.tables.cli.output.publish.AWSPublisher;
import com.dnastack.ga4gh.tables.cli.util.RequestAuthorization;
import com.dnastack.ga4gh.tables.cli.util.option.OutputOptions;
import com.dnastack.ga4gh.tables.cli.util.option.OutputOptions.OutputMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

@DisplayName("AWS Publisher Tests")
public class TestAWSPublisher {

    // Read and Write to a specific path, ENV VARiables , Tables Data

    @Test
    public void awsFetchAndPublish() {

        // Publishing should update tables file?

        Config config = ConfigUtil.getUserConfig();
        TableFetcher tableDataFetcher = TableFetcherFactory
                .getTableFetcher(config.getApiUrl(), false, config.getRequestAuthorization());

        // Publish table

        Table table = new Table();

        String tableName = "cities";
        table.setName(tableName);

        AWSPublisher publisher = new AWSPublisher(OutputMode.JSON, tableName, config.getApiUrl());
        assert publisher.getBucket().equals("fizz-dev-test");

        publisher.publish(table);

        // Fetch Table back

        OutputOptions outputOptions = null;
        int maxPages = Integer.MAX_VALUE;

        if (outputOptions.getDestinationTableName() == null){
            outputOptions.setDestinationTableName(tableName);
        }

        try (OutputWriter outputWriter = new OutputWriter(outputOptions)) {
            int pageNum = 0;
            Iterator<TableData> data = tableDataFetcher.getData(tableName);
            while (data.hasNext() && pageNum < maxPages) {
                TableData tableData = data.next();
                pageNum++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }



    }

    /*
    @Test
    public void generateNewRelativePagination() {
        final int pageNum = 2;
        String destination = "gs://test-bucket/table";
        String tableName = "example-table";
        final String previousUrl = "https://example.com/datasets/test_1";
        final String nextUrl = "https://example.com/datasets/test_3";
        final String newPreviousUrl = "data.1";
        final String newNextUrl = "data.3";
        final Pagination originalPagination = new Pagination();
        final AWSPublisher publisher = new AWSPublisher(OutputMode.JSON, tableName, destination);
        originalPagination.setPreviousPageUrl(previousUrl);
        originalPagination.setNextPageUrl(nextUrl);

        Pagination newPagination = publisher.getAbsolutePagination(originalPagination, pageNum);
        assert newPagination.getPreviousPageUrl().equals(newPreviousUrl);
        assert newPagination.getNextPageUrl().equals(newNextUrl);
    }
     */

}
