package com.dnastack.ga4gh.tables.cli.input;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.dnastack.ga4gh.tables.cli.util.AbsUtil;
import com.dnastack.ga4gh.tables.cli.util.HttpUtils;
import com.dnastack.ga4gh.tables.cli.util.RequestAuthorization;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class ABSTableFetcher extends AbstractTableFetcher {

    public ABSTableFetcher(String absRoot, boolean recursePropertyRefs, RequestAuthorization authorization) {
        super(absRoot, recursePropertyRefs, authorization);
    }

    @Override
    protected LinkedHashMap<String, Object> resolveRefs(String absoluteRefs) throws IOException {
        URI uri = URI.create(absoluteRefs);
        TypeReference<LinkedHashMap<String, Object>> typeReference = new TypeReference<LinkedHashMap<String, Object>>() {
        };
        if (uri.getScheme().startsWith("https") && uri.getHost().endsWith("blob.core.windows.net")) {
            return getBlobAs(absoluteRefs, typeReference);
        } else {
            return HttpUtils.getAs(absoluteRefs, typeReference, authorization);
        }
    }

    @Override
    protected TableData getDataPage(String url) throws IOException {
        return getBlobAs(url, TableData.class);
    }

    @Override
    public ListTableResponse list() throws IOException {
        return getBlobAs(getListAbsoluteUrl(), ListTableResponse.class);
    }

    @Override
    public Iterator<TableData> search(String query) {
        throw new UnsupportedOperationException("Searching GCS buckets is not currently supported");
    }

    @Override
    public Table getInfo(String tableName) throws IOException {
        Table info = getBlobAs(getInfoAbsoluteUrl(tableName), Table.class);
        info.setDataModel(resolveRefs(info.getDataModel(), getInfoAbsoluteUrl(tableName)));
        return info;
    }

    protected String getBlobData(String absUrl) throws IOException {
        try {
            String account = AbsUtil.getAccount(absUrl);
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(AbsUtil.getConnectionString(account)).buildClient();
            String container = AbsUtil.getContainerName(absUrl);
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(container);
            if (!containerClient.exists()) {
                throw new IOException("Blob Container could not found for " + absUrl);
            }

            BlobClient blobClient = containerClient.getBlobClient(absUrl);

            if (!blobClient.exists()) {
                throw new FileNotFoundException(absUrl);
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            blobClient.download(byteArrayOutputStream);
            return byteArrayOutputStream.toString();
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }
}
