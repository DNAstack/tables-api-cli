package org.ga4gh.dataset.cli.util;

import com.google.cloud.storage.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public class GSPublisher {

    private final String bucket;
    private final String blob;

    public GSPublisher(String destination) {
        if (destination == null) {
           this.bucket = null;
           this.blob = null;
           return;
        }
        if (!destination.startsWith("gs://")) {
            throw new RuntimeException("Publish destinations must be GCS URIs.");
        }
        // flaky
        this.bucket = destination.substring(destination.indexOf('/') + 2, destination.lastIndexOf('/'));
        this.blob = destination.substring("gs://".length() + bucket.length() + 1);
    }

    public void publish(String dataset) {
        if (this.blob == null) {
            return;
        }
        Storage storage = StorageOptions.getDefaultInstance().getService();
        BlobId blobId = BlobId.of(this.bucket, this.blob);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();
        Blob blob = storage.create(blobInfo, dataset.getBytes(UTF_8));
        //TODO: Create blob ACL just for this user
    }

}
