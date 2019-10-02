package org.ga4gh.dataset.cli.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.*;
import org.ga4gh.dataset.cli.ga4gh.Dataset;
import org.ga4gh.dataset.cli.ga4gh.Page;

public class GCSPublisher {

    private final String GCS_URL = "https://storage.cloud.google.com";

    private final String bucket;
    private final String blob;

    //TODO: If this is _only_ supposed to publish search spec compatible datasets,
    // We should either enforce or automatically append /datasets to the bucket URI.
    public GCSPublisher(String destination) {
        if (destination == null) {
           this.bucket = null;
           this.blob = null;
           return;
        }
        if (!destination.startsWith("gs://")) {
            throw new RuntimeException("Publish destinations must be GCS URIs.");
        }
        // flaky
        this.bucket = destination.substring(destination.indexOf('/') + 2, destination.indexOf("/", destination.indexOf('/') + 2));
        this.blob = destination.substring("gs://".length() + bucket.length() + 1);
    }

    public String getBucket() {
        return this.bucket;
    }

    public String getBlob() {
        return this.blob;
    }

    /***
     * Generates the pagination information for datasets being uploaded to a GCS.
     * @param oldPagination The original pagination information
     * @param pageNum The current page (for determining prev/next page URLs)
     */
    public Page getNewPagination(Page oldPagination, int pageNum) {
        Page newPagination = new Page();
        if (pageNum != 0) {
            String prevPage = String.format("%s/%s/%s", this.GCS_URL, this.bucket, this.blob);
            if (pageNum > 1) {
                prevPage += "." + (pageNum - 1);
            }

            newPagination.setPrevPageUrl(prevPage);
        }
        if (oldPagination.getNextPageUrl() != null) {
            String nextPage = String.format("%s/%s/%s.%s", this.GCS_URL, this.bucket, this.blob, pageNum + 1);
            newPagination.setNextPageUrl(nextPage);
        }
        return newPagination;
    }

    public void publish(Dataset dataset, int pageNum) {
        if (this.blob == null) {
            return;
        }
        Dataset modifiedDataset = new Dataset();
        modifiedDataset.setSchema(dataset.getSchema());
        modifiedDataset.setObjects(dataset.getObjects());
        modifiedDataset.setPagination(getNewPagination(dataset.getPagination(), pageNum));
        ObjectMapper mapper = new ObjectMapper();
        String datasetJson;
        try {
            datasetJson = mapper.writeValueAsString(modifiedDataset);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to process dataset JSON", e);
        }

        String blobPage = this.blob + (pageNum == 0 ? "" :  "." + pageNum);
        Storage storage = StorageOptions.getDefaultInstance().getService();
        BlobId blobId = BlobId.of(this.bucket, blobPage);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();
        Blob blob = storage.create(blobInfo, datasetJson.getBytes());
        //TODO: Create blob ACL just for this user
    }

}
