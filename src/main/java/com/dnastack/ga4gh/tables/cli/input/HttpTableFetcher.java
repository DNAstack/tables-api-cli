package com.dnastack.ga4gh.tables.cli.input;

import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.dnastack.ga4gh.tables.cli.util.HttpUtils;
import com.dnastack.ga4gh.tables.cli.util.RequestAuthorization;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Iterator;
import java.util.LinkedHashMap;

public class HttpTableFetcher extends AbstractTableFetcher {


    public HttpTableFetcher(String httpApiUrl, boolean recursePropertyRefs, RequestAuthorization authorization) {
        super(httpApiUrl, recursePropertyRefs, authorization);
    }

    @Override
    protected LinkedHashMap<String, Object> resolveRefs(String absoluteRefs) {
        TypeReference<LinkedHashMap<String, Object>> typeReference = new TypeReference<LinkedHashMap<String, Object>>() {
        };
        return HttpUtils.getAs(absoluteRefs, typeReference, authorization);
    }

    @Override
    public ListTableResponse list() {
        return HttpUtils.getAs(getListAbsoluteUrl(), ListTableResponse.class, authorization);
    }

    private String getQueryJson(String query) {
        return "{ \"query\": \"" + query + "\" }";
    }

    @Override
    public Iterator<TableData> search(String query) {
        TableData data = HttpUtils.postAs(getSearchAbsoluteUrl(), getQueryJson(query), TableData.class, authorization);
        return new TableDataIterator(getSearchAbsoluteUrl(), data);
    }


    @Override
    public Table getInfo(String tableName) {
        Table info = HttpUtils.getAs(getInfoAbsoluteUrl(tableName), Table.class, authorization);
        info.setDataModel(resolveRefs(info.getDataModel(), getInfoAbsoluteUrl(tableName)));
        return info;
    }

    @Override
    protected TableData getDataPage(String url) {
        return HttpUtils.getAs(url, TableData.class, authorization);
    }

    @Override
    String getBlobData(String s3Url) {
        return null;
    }
}
