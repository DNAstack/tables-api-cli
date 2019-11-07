package com.dnastack.ga4gh.tables.cli.output.publish;

import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Pagination;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GCSPublisher extends Publisher {

    private final String GCS_URL = "https://storage.cloud.google.com";

    private final String bucket;

    //TODO: If this is _only_ supposed to publish search spec compatible datasets,
    // We should either enforce or automatically append /datasets to the bucket URI.
    public GCSPublisher(String tableName, String destination) {
        super(tableName, destination);
        if (destination == null) {
            this.bucket = null;
            return;
        }
        if (!destination.startsWith("gs://")) {
            throw new RuntimeException("Publish destinations must be GCS URIs.");
        }
        this.bucket = getBucket(destination);
    }

    public String getBucket() {
        return this.bucket;
    }

    public String getBlobRoot() {
        return blobRoot;
    }

    /***
     * Generates the pagination information for datasets being uploaded to a GCS.
     * @param oldPagination The original pagination information
     * @param pageNum The current page (for determining prev/next page URLs)
     */
    public Pagination getNewPagination(Pagination oldPagination, int pageNum) {
        Pagination newPagination = new Pagination();
        if (pageNum != 0) {
            String prevPage = String.format("%s/%s/%s/data", this.GCS_URL, this.bucket, this.blobRoot);
            if (pageNum > 1) {
                prevPage += "." + (pageNum - 1);
            }

            newPagination.setPreviousPageUrl(prevPage);
        }
        if (oldPagination.getNextPageUrl() != null) {
            String nextPage = String
                .format("%s/%s/%s/data.%s", this.GCS_URL, this.bucket, this.blobRoot, pageNum + 1);
            newPagination.setNextPageUrl(nextPage);
        }
        return newPagination;
    }


    @Override
    public void publish(Table table) {
        if (!tableName.equals(table.getName())) {
            table.setName(tableName);
        }

        String tableInfoJson = toString(table);
        String tableInfoPage = this.blobRoot + "/info";
        Storage storage = StorageOptions.getDefaultInstance().getService();
        BlobId blobId = BlobId.of(this.bucket, tableInfoPage);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("application/json").build();
        Blob blob = storage.create(blobInfo, tableInfoJson.getBytes());
    }

    @Override
    public void publish(ListTableResponse table) {
        String tableInfoJson = toString(table);
        String tableInfoPage = this.destination + "/tables";
        Storage storage = StorageOptions.getDefaultInstance().getService();
        BlobId blobId = BlobId.of(this.bucket, tableInfoPage);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("application/json").build();
        Blob blob = storage.create(blobInfo, tableInfoJson.getBytes());
    }

    @Override
    public void publish(TableData tableData, int pageNum) {
        if (this.blobRoot == null) {
            return;
        }

        TableData modifiedData = new TableData();
        modifiedData.setDataModel(tableData.getDataModel());
        modifiedData.setData(tableData.getData());
        modifiedData.setPagination(getAbsolutePagination(tableData.getPagination(), pageNum));
        String datasetJson = toString(modifiedData);

        String blobPage = this.blobRoot + "/data" + (pageNum > 0 ? "." + pageNum : "");
        Storage storage = StorageOptions.getDefaultInstance().getService();
        BlobId blobId = BlobId.of(this.bucket, blobPage);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("application/json").build();
        Blob blob = storage.create(blobInfo, datasetJson.getBytes());
        //TODO: Create blob ACL just for this user
    }


    private final static Pattern GSPattern = Pattern.compile("^gs://(?<bucket>[0-9a-zA-Z_\\-.]+)/(?<object>.+)$");

    public String getBucket(String gsUrl) {
        Matcher matcher = GSPattern.matcher(gsUrl);
        if (matcher.find()) {
            return matcher.group("bucket");
        } else {
            throw new IllegalArgumentException("Could not handle transfer, this is not a google file");
        }
    }

    @Override
    public String getObjectRoot(String gsUrl) {
        Matcher matcher = GSPattern.matcher(gsUrl);
        if (matcher.find()) {
            return matcher.group("object");
        } else {
            return "table";
        }
    }

}
