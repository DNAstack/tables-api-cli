package com.dnastack.ga4gh.dataset.cli.test;

import com.dnastack.ga4gh.tables.cli.model.Pagination;
import com.dnastack.ga4gh.tables.cli.publisher.ABSPublisher;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ABS Publisher Tests")
public class TestABSPublisher {

    @Disabled
    @Test
    public void parseABSUri() {
        String destination = "https://example.blob.core.windows.net/example-container/table";
        String tableName = "sample-table";
        ABSPublisher publisher = new ABSPublisher(tableName,destination, false);
    }

    @Test
    public void generateNewRelativePagination() {
        final int pageNum = 2;
        final String destination = "https://example.blob.core.windows.net/example-container/table";
        final String tableName = "sample-table";
        final String previousUrl = "https://example.com/datasets/test_1";
        final String nextUrl = "https://example.com/datasets/test_3";
        final String newPreviousUrl = "data.1";
        final String newNextUrl = "data.3";
        final Pagination originalPagination = new Pagination();
        final ABSPublisher publisher = new ABSPublisher(tableName,destination, false);
        originalPagination.setPreviousPageUrl(previousUrl);
        originalPagination.setNextPageUrl(nextUrl);

        Pagination newPagination = publisher.getAbsolutePagination(originalPagination, pageNum);
        assert newPagination.getPreviousPageUrl().equals(newPreviousUrl);
        assert newPagination.getNextPageUrl().equals(newNextUrl);
    }
}
