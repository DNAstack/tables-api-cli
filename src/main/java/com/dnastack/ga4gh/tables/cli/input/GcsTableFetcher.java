package com.dnastack.ga4gh.tables.cli.input;

import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.dnastack.ga4gh.tables.cli.util.GcsUtil;
import com.dnastack.ga4gh.tables.cli.util.HttpUtils;
import com.dnastack.ga4gh.tables.cli.util.RequestAuthorization;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.google.cloud.storage.StorageOptions;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class GcsTableFetcher extends AbstractTableFetcher {

    private final Storage storage;

    public GcsTableFetcher(String rootApiTarget, boolean recursePropertyRefs, RequestAuthorization authorization) {
        super(rootApiTarget, recursePropertyRefs, authorization);
        storage = StorageOptions.getDefaultInstance().getService();
    }

    @Override
    protected LinkedHashMap<String, Object> resolveRefs(String absoluteRefs) throws IOException {
        TypeReference<LinkedHashMap<String, Object>> typeReference = new TypeReference<LinkedHashMap<String, Object>>() {
        };
        if (absoluteRefs.startsWith("gs://")) {
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

    protected String getBlobData(String gsUrl) throws IOException {
        try {
            Blob blob = storage.get(GcsUtil.getBucket(gsUrl), GcsUtil.getObjectRoot(gsUrl));
            if (blob == null || !blob.exists()) {
                throw new FileNotFoundException(gsUrl);
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            blob.downloadTo(byteArrayOutputStream);
            return byteArrayOutputStream.toString();
        } catch (StorageException e) {
            throw new IOException(e);
        }
    }
}
