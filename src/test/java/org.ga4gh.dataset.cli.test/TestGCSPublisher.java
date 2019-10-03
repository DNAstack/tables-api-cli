package org.ga4gh.dataset.cli.test;

import org.ga4gh.dataset.cli.ga4gh.Dataset;
import org.ga4gh.dataset.cli.ga4gh.Page;
import org.ga4gh.dataset.cli.util.GCSPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GCS Publisher Tests")
public class TestGCSPublisher {

    @Test
    public void parseGCSUri() {
        String destination = "gs://test-bucket/datasets/test-blob";
        GCSPublisher publisher = new GCSPublisher(destination);
        assert publisher.getBucket().equals("test-bucket");
        assert publisher.getBlob().equals("datasets/test-blob");
    }

    //TODO: Add tests for Page 0 and LastPage, or make this test better
    @Test
    public void generateNewPagination() {
        final int pageNum = 2;
        final String destination = "gs://example/datasets/test";
        final String previousUrl = "https://example.com/datasets/test_1";
        final String nextUrl = "https://example.com/datasets/test_3";
        final String newPreviousUrl = "https://storage.cloud.google.com/example/datasets/test.1";
        final String newNextUrl = "https://storage.cloud.google.com/example/datasets/test.3";
        final Page originalPagination = new Page();
        final GCSPublisher publisher = new GCSPublisher(destination);
        originalPagination.setPrevPageUrl(previousUrl);
        originalPagination.setNextPageUrl(nextUrl);

        Page newPagination = publisher.getNewPagination(originalPagination, pageNum);
        assert newPagination.getPrevPageUrl().equals(newPreviousUrl);
        assert newPagination.getNextPageUrl().equals(newNextUrl);

    }
}
