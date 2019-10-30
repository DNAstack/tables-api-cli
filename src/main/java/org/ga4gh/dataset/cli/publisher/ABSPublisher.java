package org.ga4gh.dataset.cli.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import org.ga4gh.dataset.cli.Config;
import org.ga4gh.dataset.cli.ga4gh.Dataset;
import org.ga4gh.dataset.cli.ga4gh.Pagination;

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

    public ABSPublisher(String destination, Config.Auth auth, boolean generateSASPages) {
        super(destination, auth);
        this.account = getAccount(destination);
        this.generateSASPages = generateSASPages;
    }

    private Pagination generateSASPagination(CloudBlobContainer container, Pagination pagination) {
        Pagination SASPagination = new Pagination();
        SharedAccessBlobPolicy blobPolicy = new SharedAccessBlobPolicy();
        blobPolicy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ));
        blobPolicy.setSharedAccessExpiryTime(new Date(Instant.now().toEpochMilli() + TimeUnit.HOURS.toMillis(1)));
        if (pagination.getPrevPageUrl() != null && !pagination.getPrevPageUrl().isBlank()) {
            try {
                CloudBlockBlob blob = container.getBlockBlobReference(pagination.getPrevPageUrl());
                //blob.generateUserDelegationSharedAccessSignature()
                String sas = blob.generateSharedAccessSignature(blobPolicy, null);
                SASPagination.setPrevPageUrl(pagination.getPrevPageUrl() + "?" + sas);
            } catch (Exception e) {
                //Fix me
            }
        }

        if (pagination.getNextPageUrl() != null && !pagination.getNextPageUrl().isBlank()) {
            try {
                CloudBlockBlob blob = container.getBlockBlobReference(pagination.getNextPageUrl());
                //Ensure blob exists
                if (!blob.exists()) {
                    blob.uploadFromByteArray(new byte[1], 0, 1);
                }
                //blob.generateUserDelegationSharedAccessSignature()
                String sas = blob.generateSharedAccessSignature(blobPolicy, null);
                SASPagination.setNextPageUrl(pagination.getNextPageUrl() + "?" + sas);
            } catch (Exception e) {
                //Fix me
            }
        }
        return SASPagination;
    }

    @Override
    public void publish(Dataset dataset, int pageNum) {
        Dataset modifiedDataset = new Dataset();
        modifiedDataset.setSchema(dataset.getSchema());
        modifiedDataset.setObjects(dataset.getObjects());
        modifiedDataset.setPagination(getAbsolutePagination(dataset.getPagination(), pageNum));
        String blobPage = this.blob + (pageNum == 0 ? "" :  "." + pageNum);

        try {
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(getConnectionString(account));
            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
            CloudBlobContainer container = blobClient.getContainerReference(getContainerName(destination));
            container.createIfNotExists(BlobContainerPublicAccessType.OFF, new BlobRequestOptions(), new OperationContext());
            CloudBlockBlob blob = container.getBlockBlobReference(blobPage);
            if (generateSASPages) {
                modifiedDataset.setPagination(generateSASPagination(container, modifiedDataset.getPagination()));
            }
            String datasetJson = toString(modifiedDataset);

            blob.uploadFromByteArray(datasetJson.getBytes(), 0, datasetJson.getBytes().length);
            //System.out.println("Published: " + blob.getUri());
        } catch (InvalidKeyException | URISyntaxException e) {
            System.out.println("Failed to connect to ABS account:" + e.getMessage());
            System.exit(1);
        } catch (StorageException e) {
            //System.out.println("Unable to connect to ABS container: " + getContainerName(destination) + " :" + e.getMessage());
            System.out.printf("Unable to connect to ABS container %s : %s", getContainerName(destination), e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Failed to upload blob: " + e.getMessage());
            System.exit(1);
        }
    }

    @Override
    String getBlobName(String destination) {
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
        return String.format(STORAGE_CONNECTION_STRING_TEMPLATE, account, auth.getAbsAccountKey());
    }

    private Pagination getSASPagination(Pagination oldPagination) {

        return null;
    }
}
