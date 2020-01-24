package com.dnastack.ga4gh.tables.cli.output.publish;

import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.dnastack.ga4gh.tables.cli.util.GcsUtil;
import com.dnastack.ga4gh.tables.cli.util.option.OutputOptions.OutputMode;
import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;

public class GCSPublisher extends AbstractPublisher {

    private final String bucket;


    public GCSPublisher(OutputMode mode, String tableName, String destination) {
        super(mode, tableName, destination);
        if (destination == null) {
            this.bucket = null;
            return;
        }
        if (!destination.startsWith("gs://")) {
            throw new RuntimeException("Publish destinations must be GCS URIs.");
        }
        this.bucket = GcsUtil.getBucket(destination);
    }

    public String getBucket() {
        return this.bucket;
    }

    public String getBlobRoot() {
        return blobRoot;
    }


    @Override
    public void publish(Table table) {
        if (!tableName.equals(table.getName())) {
            table.setName(tableName);
        }

        String tableInfoJson = format(table);
        String tableInfoPage = this.blobRoot + "/info";
        Storage storage = StorageOptions.getDefaultInstance().getService();
        BlobId blobId = BlobId.of(this.bucket, tableInfoPage);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(getContentType()).build();
        Blob blob = storage.create(blobInfo, tableInfoJson.getBytes());
    }

    @Override
    public void publish(ListTableResponse table) {

        String tableInfoJson = format(table);
        String tableInfoPage = "tables";
        Storage storage = StorageOptions.getDefaultInstance().getService();
        BlobId blobId = BlobId.of(this.bucket, tableInfoPage);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(getContentType()).build();
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
        String datasetJson = format(modifiedData);

        String blobPage = this.blobRoot + "/data" + (pageNum > 0 ? "." + pageNum : "");
        Storage storage = StorageOptions.getDefaultInstance().getService();
        BlobId blobId = BlobId.of(this.bucket, blobPage);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(getContentType()).build();
        Blob blob = storage.create(blobInfo, datasetJson.getBytes());
        //TODO: Create blob ACL just for this user
    }

    public Boolean isBucketEmpty() {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        Page<Blob> blobs = storage.list(this.getBucket());
        for (Blob blob : blobs.iterateAll()) {
            return false;
        }
        return true;
    }

    @Override
    public String getObjectRoot(String gsUrl) {
        return GcsUtil.getObjectRoot(gsUrl);
    }

}
