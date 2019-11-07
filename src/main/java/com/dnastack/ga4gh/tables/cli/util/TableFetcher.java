package com.dnastack.ga4gh.tables.cli.util;

import com.dnastack.ga4gh.tables.cli.config.ConfigUtil;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.fasterxml.jackson.core.type.TypeReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import okhttp3.HttpUrl;

public class TableFetcher {

    private static final String TABLE_GET_ENDPOINT_TEMPLATE = "%s/%s/%s";  //THIS IS THE OLD ENDPOINT!
    public static final String TABLE_GET_ENDPOINT = "table";

    private String tableEndpointTemplate = TABLE_GET_ENDPOINT_TEMPLATE;
    private String tableEndpoint = TABLE_GET_ENDPOINT;
    private final String tableName;
    private final boolean recurseRefs;
    protected final RequestAuthorization authorization;


    public TableFetcher(String tableName, boolean recursePropertyRefs, RequestAuthorization authorization) {
        this.tableName = tableName;
        this.recurseRefs = recursePropertyRefs;
        this.authorization = authorization;
    }

    public void setTableEndpoint(String endpoint) {
        this.tableEndpoint = endpoint;
    }


    private String getInfoAbsoluteUrl() {
        return getAbsoluteUrl(String.format(TABLE_GET_ENDPOINT_TEMPLATE, tableEndpoint, tableName, "info"));
    }

    private String getDataAbsoluteUrl() {
        return getAbsoluteUrl(String.format(TABLE_GET_ENDPOINT_TEMPLATE, tableEndpoint, tableName, "data"));
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


    protected LinkedHashMap<String, Object> resolveRefs(LinkedHashMap<String, Object> properties, String urlContext) {
        String refUrl = (String) properties.get("$ref");
        if (refUrl != null) {
            String absoluteRefUrl = getAbsoluteUrl(refUrl, urlContext);
            LinkedHashMap<String, Object> resolvedProperties = HttpUtils
                .getAs(absoluteRefUrl, new TypeReference<LinkedHashMap<String, Object>>() {
                }, authorization); //don't recurse, references at a deeper level than this won't be expanded.
            if (recurseRefs) {
                return resolveRefs(resolvedProperties, absoluteRefUrl);
            } else {
                return resolvedProperties;
            }
        } else {
            return properties;
        }
    }

    public Table getInfo() {
        String absoluteUrl = getInfoAbsoluteUrl();
        Table table = HttpUtils.getAs(absoluteUrl, Table.class, authorization);
        table.setDataModel(resolveRefs(table.getDataModel(), absoluteUrl));
        return table;
    }

    public Iterable<TableData> getDataPages() {
        return new Iterable<>() {
            @Override
            public Iterator<TableData> iterator() {
                return new Iterator<>() {

                    TableData currentPage;
                    String currentUrlContext;

                    @Override
                    public boolean hasNext() {
                        if (currentPage == null) {
                            return true;
                        }
                        return currentPage.getPagination() != null
                            && currentPage.getPagination().getNextPageUrl() != null;
                    }

                    @Override
                    public TableData next() {
                        if (currentPage == null) {
                            currentUrlContext = getDataAbsoluteUrl();
                            currentPage = HttpUtils.getAs(currentUrlContext, TableData.class, authorization);
                        } else if (currentPage.getPagination() == null
                            || currentPage.getPagination().getNextPageUrl() == null) {
                            return null;
                        } else if (currentPage.getPagination().getNextPageUrl() != null) {
                            currentUrlContext = getAbsoluteUrl(currentPage.getPagination()
                                .getNextPageUrl(), currentUrlContext);
                            currentPage = HttpUtils.getAs(currentUrlContext, TableData.class, authorization);
                        }

                        currentPage.setDataModel(resolveRefs(currentPage.getDataModel(), currentUrlContext));
                        return currentPage;
                    }
                };
            }
        };
    }
}
