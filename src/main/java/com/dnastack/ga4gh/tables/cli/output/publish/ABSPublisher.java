package com.dnastack.ga4gh.tables.cli.output.publish;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Pagination;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.dnastack.ga4gh.tables.cli.util.AbsUtil;
import com.dnastack.ga4gh.tables.cli.util.option.OutputOptions.OutputMode;

import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;

public class ABSPublisher extends AbstractPublisher {

    private final String account;
    private final boolean generateSASPages;

    public ABSPublisher(OutputMode mode, String tableName, String destination, boolean generateSASPages) {
        super(mode, tableName, destination);
        this.account = AbsUtil.getAccount(destination);
        this.generateSASPages = generateSASPages;
    }

    private Pagination generateSASPagination(BlobContainerClient containerClient, Pagination pagination) {
        Pagination SASPagination = new Pagination();
        if (pagination.getPreviousPageUrl() != null && !pagination.getPreviousPageUrl().isBlank()) {
            String blobPage = this.blobRoot + "/" + pagination.getPreviousPageUrl();
            BlobClient blobClient = containerClient.getBlobClient(blobPage);
            String sas = blobClient.generateSas(getDefaultSignatureValues());
            SASPagination.setPreviousPageUrl(pagination.getPreviousPageUrl() + "?" + sas);
        }

        if (pagination.getNextPageUrl() != null && !pagination.getNextPageUrl().isBlank()) {
            String blobPage = this.blobRoot + "/" + pagination.getNextPageUrl();
            BlobClient blobClient = containerClient.getBlobClient(blobPage);
            if (!blobClient.exists()) {
                blobClient.upload(new ByteArrayInputStream(new byte[1]), 1);
            }

            String sas = blobClient.generateSas(getDefaultSignatureValues());
            SASPagination.setNextPageUrl(pagination.getNextPageUrl() + "?" + sas);
            blobClient.delete();
        }
        return SASPagination;
    }

    @Override
    public void publish(Table table) {
        String tableInfoJson = format(table);
        String tableInfoPage = this.blobRoot + "/info";
        BlobClient blobClient = getContainerClient().getBlobClient(tableInfoPage);
        blobClient.upload(new ByteArrayInputStream(tableInfoJson.getBytes()), tableInfoJson.getBytes().length);
    }

    @Override
    public void publish(ListTableResponse table) {
        String tableInfoJson = format(table);
        String tableInfoPage = this.destination + "/tables";
        BlobClient blobClient = getContainerClient().getBlobClient(tableInfoPage);
        blobClient.upload(new ByteArrayInputStream(tableInfoJson.getBytes()), tableInfoJson.getBytes().length);
    }

    @Override
    public void publish(TableData tableData, int pageNum) {
        TableData modifiedData = new TableData();
        modifiedData.setDataModel(tableData.getDataModel());
        modifiedData.setData(tableData.getData());
        modifiedData.setPagination(getAbsolutePagination(tableData.getPagination(), pageNum));
        String blobPage = this.blobRoot + "/data" + (pageNum == 0 ? "" : "." + pageNum);

        BlobContainerClient containerClient = getContainerClient();
        if (!containerClient.exists()) {
            throw new RuntimeException("The specified container does not exist: " + containerClient.getBlobContainerName());
        }
        BlobClient blobClient = containerClient.getBlobClient(blobPage);
        if (generateSASPages) {
            modifiedData.setPagination(generateSASPagination(containerClient, modifiedData.getPagination()));
        }
        String tableJson = format(modifiedData);
        blobClient.upload(new ByteArrayInputStream(tableJson.getBytes()), tableJson.getBytes().length);
        if (generateSASPages && pageNum == 0) {
            System.out.println(getFirstPageSasUri(blobClient.generateSas(getDefaultSignatureValues())));
        }
    }


    @Override
    public String getObjectRoot(String destination) {
        return AbsUtil.getObjectRoot(destination);
    }

    private String getFirstPageSasUri(String SAS) {
        return String.format("%s/%s/data?%s", this.destination, this.getTableName(), SAS);
    }

    private BlobServiceSasSignatureValues getDefaultSignatureValues() {
        BlobContainerSasPermission permissions = new BlobContainerSasPermission();
        permissions.setReadPermission(true);
        return new BlobServiceSasSignatureValues(OffsetDateTime.now().plusHours(3), permissions);
    }

    private BlobContainerClient getContainerClient() {
        String connectionString = AbsUtil.getConnectionString(account);
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString).buildClient();
        String container = AbsUtil.getContainerName(destination);
        BlobContainerClient client = blobServiceClient.getBlobContainerClient(container);
        if (!client.exists()) {
            throw new RuntimeException("The specified container does not exist: " + container);
        }
        return client;
    }
}
