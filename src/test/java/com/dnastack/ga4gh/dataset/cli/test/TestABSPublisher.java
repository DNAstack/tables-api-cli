package com.dnastack.ga4gh.dataset.cli.test;

import com.dnastack.ga4gh.dataset.cli.ga4gh.Pagination;
import com.dnastack.ga4gh.dataset.cli.publisher.ABSPublisher;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ABS Publisher Tests")
public class TestABSPublisher {

    @Disabled
    @Test
    public void parseABSUri() {
        String destination = "https://example.blob.core.windows.net/example-container/sample-dataset";
        ABSPublisher publisher = new ABSPublisher(destination, null, false);
    }

    @Test
    public void generateNewRelativePagination() {
        final int pageNum = 2;
        final String destination = "https://example.blob.core.windows.net/example-container/sample-dataset";
        final String previousUrl = "https://example.com/datasets/test_1";
        final String nextUrl = "https://example.com/datasets/test_3";
        final String newPreviousUrl = "sample-dataset.1";
        final String newNextUrl = "sample-dataset.3";
        final Pagination originalPagination = new Pagination();
        final ABSPublisher publisher = new ABSPublisher(destination, null, false);
        originalPagination.setPrevPageUrl(previousUrl);
        originalPagination.setNextPageUrl(nextUrl);

        Pagination newPagination = publisher.getAbsolutePagination(originalPagination, pageNum);
        assert newPagination.getPrevPageUrl().equals(newPreviousUrl);
        assert newPagination.getNextPageUrl().equals(newNextUrl);
    }
}
