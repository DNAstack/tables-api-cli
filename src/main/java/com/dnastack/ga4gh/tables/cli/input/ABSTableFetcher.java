package com.dnastack.ga4gh.tables.cli.input;

import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.dnastack.ga4gh.tables.cli.util.AbsUtil;
import com.dnastack.ga4gh.tables.cli.util.HttpUtils;
import com.dnastack.ga4gh.tables.cli.util.RequestAuthorization;
import com.fasterxml.jackson.core.type.TypeReference;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class ABSTableFetcher extends AbstractTableFetcher {

    public ABSTableFetcher(String absRoot, boolean recursePropertyRefs, RequestAuthorization authorization) {
        super(absRoot, recursePropertyRefs, authorization);
    }

    @Override
    protected LinkedHashMap<String, Object> resolveRefs(String absoluteRefs) {
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
    protected TableData getDataPage(String url) {
        return getBlobAs(url, TableData.class);
    }

    @Override
    public ListTableResponse list() {
        return getBlobAs(getListAbsoluteUrl(), ListTableResponse.class);
    }

    @Override
    public Iterator<TableData> search(String query) {
        throw new UnsupportedOperationException("Searching GCS buckets is not currently supported");
    }

    @Override
    public Table getInfo(String tableName) {
        Table info = getBlobAs(getInfoAbsoluteUrl(tableName), Table.class);
        info.setDataModel(resolveRefs(info.getDataModel(), getInfoAbsoluteUrl(tableName)));
        return info;
    }

    protected String getBlobData(String absUrl) {
        try {
            String account = AbsUtil.getAccount(absUrl);
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(AbsUtil.getConnectionString(account));
            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
            CloudBlobContainer container = blobClient.getContainerReference(AbsUtil.getContainerName(absUrl));
            CloudBlockBlob blob = container.getBlockBlobReference(absUrl);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            blob.download(byteArrayOutputStream);
            return byteArrayOutputStream.toString();
        } catch (InvalidKeyException | URISyntaxException e) {
            throw new RuntimeException("Failed to connect to ABS account:" + e.getMessage());
        } catch (
                StorageException e) {
            throw new RuntimeException(String
                    .format("Unable to connect to ABS container %s : %s", AbsUtil.getContainerName(absUrl), e
                            .getMessage()));
        }
    }

}
