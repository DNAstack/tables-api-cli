package com.dnastack.ga4gh.tables.cli.util;

import com.dnastack.ga4gh.tables.cli.config.ConfigUtil;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import java.util.Iterator;
import okhttp3.HttpUrl;

public class TableSearcher extends TableFetcher {

    private static final String SEARCH_ENDPOINT = "search";
    private final String queryJson;


    public TableSearcher(String query, boolean recursePropertyRefs, RequestAuthorization authorization) {
        //TODO: accessToken here
        super(null, recursePropertyRefs, authorization);
        this.queryJson = getQueryJson(query);
    }

    private String getQueryJson(String query) {
        return "{ \"query\": \"" + query + "\" }";
    }


    private String getSearchUrl() {
        String baseURL = ConfigUtil.getUserConfig().getApiUrl();
        return HttpUrl.parse(baseURL).newBuilder()
            .addPathSegments(SEARCH_ENDPOINT).build().url().toString();
    }

    @Override
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
                            currentUrlContext = getSearchUrl();
                            currentPage = HttpUtils.postAs(getSearchUrl(), queryJson, TableData.class, authorization);
                        } else if (currentPage.getPagination() == null
                            || currentPage.getPagination().getNextPageUrl() == null) {
                            return null;
                        } else if (currentPage.getPagination().getNextPageUrl() != null) {
                            currentPage = HttpUtils.getAs(getAbsoluteUrl(currentPage.getPagination()
                                .getNextPageUrl(), currentUrlContext), TableData.class, authorization);
                        }
                        currentPage.setDataModel(resolveRefs(currentPage.getDataModel(), currentUrlContext));
                        return currentPage;
                    }
                };
            }
        };
    }
}
