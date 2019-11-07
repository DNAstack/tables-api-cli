package com.dnastack.ga4gh.tables.cli.output.publish;

import com.dnastack.ga4gh.tables.cli.config.ConfigUtil;
import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Pagination;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.time.Instant;
import java.util.Date;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

public class ABSPublisher extends Publisher {

    private final String account;
    private final boolean generateSASPages;

    public ABSPublisher(String tableName, String destination, boolean generateSASPages) {
        super(tableName, destination);
        this.account = getAccount(destination);
        this.generateSASPages = generateSASPages;
    }

    private Pagination generateSASPagination(CloudBlobContainer container, Pagination pagination) {
        Pagination SASPagination = new Pagination();
        SharedAccessBlobPolicy blobPolicy = new SharedAccessBlobPolicy();
        blobPolicy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ));
        blobPolicy.setSharedAccessExpiryTime(new Date(Instant.now().toEpochMilli() + TimeUnit.HOURS.toMillis(1)));
        if (pagination.getPreviousPageUrl() != null && !pagination.getPreviousPageUrl().isBlank()) {
            try {
                CloudBlockBlob blob = container.getBlockBlobReference(pagination.getPreviousPageUrl());
                //blob.generateUserDelegationSharedAccessSignature()
                String sas = blob.generateSharedAccessSignature(blobPolicy, null);
                SASPagination.setPreviousPageUrl(pagination.getPreviousPageUrl() + "?" + sas);
            } catch (Exception e) {
                //Fix me
            }
        }

        if (pagination.getNextPageUrl() != null && !pagination.getNextPageUrl().isBlank()) {
            try {
                CloudBlockBlob blob = container.getBlockBlobReference(pagination.getNextPageUrl());
                if (!blob.exists()) {
                    blob.uploadFromByteArray(new byte[1], 0, 1);
                }
                String sas = blob.generateSharedAccessSignature(blobPolicy, null);
                SASPagination.setNextPageUrl(pagination.getNextPageUrl() + "?" + sas);
            } catch (Exception e) {
                //Fix me
            }
        }
        return SASPagination;
    }

    @Override
    public void publish(Table table) {
        String tableInfoJson = toString(table);
        String tableInfoPage = this.blobRoot + "/info";
        try {
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(getConnectionString(account));
            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
            CloudBlobContainer container = blobClient.getContainerReference(getContainerName(destination));
            container
                .createIfNotExists(BlobContainerPublicAccessType.OFF, new BlobRequestOptions(), new OperationContext());
            CloudBlockBlob blob = container.getBlockBlobReference(tableInfoPage);
            blob.uploadFromByteArray(tableInfoJson.getBytes(), 0, tableInfoJson.getBytes().length);
        } catch (InvalidKeyException | URISyntaxException e) {
            throw new RuntimeException("Failed to connect to ABS account:" + e.getMessage());
        } catch (StorageException e) {
            throw new RuntimeException(String
                .format("Unable to connect to ABS container %s : %s", getContainerName(destination), e.getMessage()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload blob: " + e.getMessage());
        }
    }

    @Override
    public void publish(ListTableResponse table) {
        String tableInfoJson = toString(table);
        String tableInfoPage = this.destination + "/tables";
        try {
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(getConnectionString(account));
            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
            CloudBlobContainer container = blobClient.getContainerReference(getContainerName(destination));
            container
                .createIfNotExists(BlobContainerPublicAccessType.OFF, new BlobRequestOptions(), new OperationContext());
            CloudBlockBlob blob = container.getBlockBlobReference(tableInfoPage);
            blob.uploadFromByteArray(tableInfoJson.getBytes(), 0, tableInfoJson.getBytes().length);
        } catch (InvalidKeyException | URISyntaxException e) {
            throw new RuntimeException("Failed to connect to ABS account:" + e.getMessage());
        } catch (StorageException e) {
            throw new RuntimeException(String
                .format("Unable to connect to ABS container %s : %s", getContainerName(destination), e.getMessage()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload blob: " + e.getMessage());
        }
    }

    @Override
    public void publish(TableData tableData, int pageNum) {
        TableData modifiedData = new TableData();
        modifiedData.setDataModel(tableData.getDataModel());
        modifiedData.setData(tableData.getData());
        modifiedData.setPagination(getAbsolutePagination(tableData.getPagination(), pageNum));
        String blobPage = this.blobRoot + "/data" + (pageNum == 0 ? "" : "." + pageNum);

        try {
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(getConnectionString(account));
            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
            CloudBlobContainer container = blobClient.getContainerReference(getContainerName(destination));
            container
                .createIfNotExists(BlobContainerPublicAccessType.OFF, new BlobRequestOptions(), new OperationContext());
            CloudBlockBlob blob = container.getBlockBlobReference(blobPage);
            if (generateSASPages) {
                modifiedData.setPagination(generateSASPagination(container, modifiedData.getPagination()));
            }
            String tableJson = toString(modifiedData);

            blob.uploadFromByteArray(tableJson.getBytes(), 0, tableJson.getBytes().length);
        } catch (InvalidKeyException | URISyntaxException e) {
            throw new RuntimeException("Failed to connect to ABS account:" + e.getMessage());
        } catch (StorageException e) {
            throw new RuntimeException(String
                .format("Unable to connect to ABS container %s : %s", getContainerName(destination), e.getMessage()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload blob: " + e.getMessage());
        }
    }


    @Override
    public String getObjectRoot(String destination) {
        String path = URI.create(destination).getPath();
        return path.substring(path.indexOf('/', 1) + 1);
    }

    private String getAccount(String destination) {
        String host = URI.create(destination).getHost();
        return host.substring(0, host.indexOf('.'));
    }

    private String getContainerName(String destination) {
        String path = URI.create(destination).getPath();
        if (path.startsWith("/")) {
            return path.substring(1, path.indexOf('/', 1));
        }
        return path.substring(0, path.indexOf('/'));
    }

    private String getConnectionString(String account) {
        final String STORAGE_CONNECTION_STRING_TEMPLATE = "DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s";
        return String
            .format(STORAGE_CONNECTION_STRING_TEMPLATE, account, ConfigUtil.getUserConfig().getAbsAccountKey());
    }

    private Pagination getSASPagination(Pagination oldPagination) {

        return null;
    }
}
