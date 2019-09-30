package org.ga4gh.dataset.cli.test;

import org.ga4gh.dataset.cli.util.GCSPublisher;
import org.junit.jupiter.api.Test;

public class TestGCSPublisher {

    @Test
    public void parseGCSUriCorrectly() {
        String folderlessDest = "gs://test-bucket/test-folder/test-blob";
        GCSPublisher publisher = new GCSPublisher(folderlessDest);
        assert publisher.getBucket().equals("test-bucket/test-folder");
        assert publisher.getBlob().equals("test-blob");
    }
}
