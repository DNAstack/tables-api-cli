package com.dnastack.ga4gh.tables.cli.input;

import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.dnastack.ga4gh.tables.cli.util.HttpUtils;
import com.dnastack.ga4gh.tables.cli.util.RequestAuthorization;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class FileSystemTableFetcher extends AbstractTableFetcher {

    public FileSystemTableFetcher(String rootDirectory, boolean recursePropertyRefs, RequestAuthorization authorization) {
        super(rootDirectory, recursePropertyRefs, authorization);
    }

    @Override
    protected LinkedHashMap<String, Object> resolveRefs(String absoluteRefs) throws IOException {
        TypeReference<LinkedHashMap<String, Object>> typeReference = new TypeReference<LinkedHashMap<String, Object>>() {
        };
        if (absoluteRefs.startsWith("http")) {
            return HttpUtils.getAs(absoluteRefs, typeReference, authorization);
        } else {
            return getFileAs(absoluteRefs, typeReference);
        }
    }

    @Override
    protected TableData getDataPage(String url) throws IOException {
        return getFileAs(url, TableData.class);
    }

    @Override
    String getBlobData(String s3Url) {
        return null;
    }

    @Override
    public ListTableResponse list() throws IOException {
        return getFileAs(getListAbsoluteUrl(), ListTableResponse.class);
    }

    @Override
    public Iterator<TableData> search(String query) {
        throw new UnsupportedOperationException("Cannot search against local files");
    }

    @Override
    public Table getInfo(String tableName) throws IOException {
        Table info = getFileAs(getInfoAbsoluteUrl(tableName), Table.class);
        info.setDataModel(resolveRefs(info.getDataModel(), getInfoAbsoluteUrl(tableName)));
        return info;
    }

    private <T> T getFileAs(String path, Class<T> clazz) throws IOException {
        String data = getFileData(path);
        return HttpUtils.getMapper().readValue(data, clazz);
    }

    private <T> T getFileAs(String path, TypeReference<T> typeReference) throws IOException {
        String data = getFileData(path);
        return HttpUtils.getMapper().readValue(data, typeReference);
    }

    private String getFileData(String path) throws IOException {
        File file = new File(path);
        return Files.readString(file.toPath());
    }
}
