package com.dnastack.ga4gh.dataset.cli.test;

import com.dnastack.ga4gh.tables.cli.model.Pagination;
import com.dnastack.ga4gh.tables.cli.output.publish.GCSPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GCS Publisher Tests")
public class TestGCSPublisher {

    @Test
    public void parseGCSUri() {
        String destination = "gs://test-bucket/table";
        String tableName = "example-table";
        GCSPublisher publisher = new GCSPublisher(tableName, destination);
        assert publisher.getBucket().equals("test-bucket");
        assert publisher.getBlobRoot().equals("table/" + tableName);
    }

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
        final GCSPublisher publisher = new GCSPublisher(tableName, destination);
        originalPagination.setPreviousPageUrl(previousUrl);
        originalPagination.setNextPageUrl(nextUrl);

        Pagination newPagination = publisher.getAbsolutePagination(originalPagination, pageNum);
        assert newPagination.getPreviousPageUrl().equals(newPreviousUrl);
        assert newPagination.getNextPageUrl().equals(newNextUrl);
    }

    //TODO: Add tests for Page 0 and LastPage, or make this test better
    @Test
    public void generateNewPagination() {
        final int pageNum = 2;
        final String destination = "gs://example/table";
        final String tableName = "test";
        final String previousUrl = "https://example.com/table/test/data";
        final String nextUrl = "https://example.com/table/test/data";
        final String newPreviousUrl = "https://storage.cloud.google.com/example/table/test/data.1";
        final String newNextUrl = "https://storage.cloud.google.com/example/table/test/data.3";
        final Pagination originalPagination = new Pagination();
        final GCSPublisher publisher = new GCSPublisher(tableName, destination);
        originalPagination.setPreviousPageUrl(previousUrl);
        originalPagination.setNextPageUrl(nextUrl);

        Pagination newPagination = publisher.getNewPagination(originalPagination, pageNum);
        assert newPagination.getPreviousPageUrl().equals(newPreviousUrl);
        assert newPagination.getNextPageUrl().equals(newNextUrl);
    }
}
