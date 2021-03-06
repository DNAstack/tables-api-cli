package com.dnastack.ga4gh.tables.cli.input;

import com.dnastack.ga4gh.tables.cli.config.ConfigUtil;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.dnastack.ga4gh.tables.cli.util.HttpUtils;
import com.dnastack.ga4gh.tables.cli.util.RequestAuthorization;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import okhttp3.HttpUrl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.LinkedHashMap;

public abstract class AbstractTableFetcher implements TableFetcher {

    public static final String TABLE_GET_ENDPOINT = "table";
    public static final String TABLE_LIST_ENDPOINT = "tables";
    public static final String TABLE_SEARCH_ENDPOINT = "search";
    private final boolean recurseRefs;
    protected final RequestAuthorization authorization;
    private final String rootApiTarget;

    public AbstractTableFetcher(String rootApiTarget, boolean recursePropertyRefs, RequestAuthorization authorization) {
        this.recurseRefs = recursePropertyRefs;
        this.authorization = authorization;
        this.rootApiTarget = rootApiTarget;
        if (rootApiTarget.endsWith("/")) {
            rootApiTarget = rootApiTarget.substring(0, rootApiTarget.length() - 1);
        }
    }


    protected String getListAbsoluteUrl() {
        return rootApiTarget + "/" + TABLE_LIST_ENDPOINT;
    }

    protected String getInfoAbsoluteUrl(String tableName) {
        return rootApiTarget + "/" + TABLE_GET_ENDPOINT + "/" + tableName + "/info";
    }

    protected String getDataAbsoluteUrl(String tableName) {
        return rootApiTarget + "/" + TABLE_GET_ENDPOINT + "/" + tableName + "/data";

    }

    protected String getSearchAbsoluteUrl() {
        return rootApiTarget + "/" + TABLE_SEARCH_ENDPOINT;
    }


    protected String getAbsoluteUrl(String urlOrPath) {
        return getAbsoluteUrl(urlOrPath, null);
    }

    protected String getAbsoluteUrl(String urlOrPath, String currentUrlContext) {
        try {
            URI u = new URI(urlOrPath);
            if (u.isAbsolute()) {
                return urlOrPath;
            } else if (currentUrlContext != null) {
                URI currentUrl = URI.create(currentUrlContext);
                return currentUrl.resolve(u).toString();

            } else {
                String baseURL = ConfigUtil.getUserConfig().getApiUrl();
                return HttpUrl.parse(baseURL).newBuilder()
                        .addPathSegments(urlOrPath).build().url().toString();

            }
        } catch (URISyntaxException use) {
            throw new IllegalArgumentException(use);
        }
    }

    protected abstract LinkedHashMap<String, Object> resolveRefs(String absoluteRefs) throws IOException;


    protected LinkedHashMap<String, Object> resolveRefs(LinkedHashMap<String, Object> properties, String urlContext) throws IOException {
        String refUrl = (String) properties.get("$ref");
        if (refUrl != null) {
            String absoluteRefUrl = getAbsoluteUrl(refUrl, urlContext);
            LinkedHashMap<String, Object> resolvedProperties = resolveRefs(absoluteRefUrl);
            if (recurseRefs) {
                return resolveRefs(resolvedProperties, absoluteRefUrl);
            } else {
                return resolvedProperties;
            }
        } else {
            return properties;
        }
    }


    public Iterator<TableData> getData(String tableName) {
        return new TableDataIterator(getDataAbsoluteUrl(tableName));
    }

    protected abstract TableData getDataPage(String url) throws IOException ;

    protected <T> T getBlobAs(String url, Class<T> clazz) throws IOException{
        String data = getBlobData(url);
        try {
            return HttpUtils.getMapper().readValue(data, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> T getBlobAs(String url, TypeReference<T> typeReference) throws IOException{
        String data = getBlobData(url);
        try {
            return HttpUtils.getMapper().readValue(data, typeReference);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves data from the source represented by the TableFetcher implementation.
     *
     * @param url Absolute URL we want to retrieve the data from. Never null
     * @return the data in the form of a string. Null if the data is not retrievable.
     * @throws java.io.FileNotFoundException if the URL points to a nonexistent resource
     * @throws java.io.IOException if retrieving the data fails for other reasons (eg. timeout, DNS lookup error, ...)
     */
    abstract String getBlobData(String url) throws IOException;

    public class TableDataIterator implements Iterator<TableData> {

        TableData currentPage;
        String currentContext;
        TableData initialPage;

        public TableDataIterator(String initalContext) {
            currentContext = initalContext;
        }

        public TableDataIterator(String initalContext, TableData initialPage) {
            currentContext = initalContext;
            this.initialPage = initialPage;

        }

        @Override
        public boolean hasNext() {
            if (currentPage == null) {
                return true;
            }
            return currentPage.getPagination() != null
                    && currentPage.getPagination().getNextPageUrl() != null;
        }

        @SneakyThrows
        @Override
        public TableData next(){
            if (initialPage != null) {
                currentPage = initialPage;
                initialPage = null;
            } else if (currentPage == null) {
                currentPage = getDataPage(currentContext);
            } else if (currentPage.getPagination() == null
                    || currentPage.getPagination().getNextPageUrl() == null) {
                return null;
            } else if (currentPage.getPagination().getNextPageUrl() != null) {
                currentContext = getAbsoluteUrl(currentPage.getPagination()
                        .getNextPageUrl(), currentContext);
                currentPage = getDataPage(currentContext);
            }
            if(currentPage.getDataModel() != null) {
                currentPage.setDataModel(resolveRefs(currentPage.getDataModel(), currentContext));
            }
            return currentPage;
        }
    }

}
