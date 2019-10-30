package org.ga4gh.dataset.cli.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import org.ga4gh.dataset.cli.Config;
import org.ga4gh.dataset.cli.ga4gh.Dataset;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

public class ABSPublisher extends Publisher {

    private final String account;

    public ABSPublisher(String destination, Config.Auth auth) {
        super(destination, auth);
        this.account = getAccount(destination);
    }

    @Override
    public void publish(Dataset dataset, int pageNum) {
        Dataset modifiedDataset = new Dataset();
        modifiedDataset.setSchema(dataset.getSchema());
        modifiedDataset.setObjects(dataset.getObjects());
        modifiedDataset.setPagination(getAbsolutePagination(dataset.getPagination(), pageNum));
        ObjectMapper mapper = new ObjectMapper();
        String datasetJson;
        try {
            datasetJson = mapper.writeValueAsString(modifiedDataset);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to process dataset JSON", e);
        }

        String blobPage = this.blob + (pageNum == 0 ? "" :  "." + pageNum);

        try {
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(getConnectionString(account));
            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
            CloudBlobContainer container = blobClient.getContainerReference(getContainerName(destination));
            container.createIfNotExists(BlobContainerPublicAccessType.OFF, new BlobRequestOptions(), new OperationContext());
            CloudBlockBlob blob = container.getBlockBlobReference(blobPage);
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
}
