package org.ga4gh.dataset.cli.util;

import okhttp3.HttpUrl;
import org.ga4gh.dataset.cli.ga4gh.Dataset;
import org.ga4gh.dataset.cli.ga4gh.Schema;

import java.util.Iterator;

public class DatasetSearcher extends DatasetFetcher {

    private static final String SEARCH_ENDPOINT = "search";
    private final String queryJson;


    public DatasetSearcher(String query, boolean recursePropertyRefs){
        super(null, recursePropertyRefs);
        this.queryJson = getQueryJson(query);
    }

    private String getQueryJson(String query) {
        return "{ \"query\": \"" + query + "\" }";
    }


    private String getSearchUrl(){
        String baseURL = ConfigUtil.getUserConfig().getApiUrl();
        return HttpUrl.parse(baseURL).newBuilder()
                .addPathSegments(SEARCH_ENDPOINT).build().url().toString();
    }

    public Iterable<Dataset> getPage(){
        return new Iterable<>(){
            @Override
            public Iterator<Dataset> iterator() {
                return new Iterator<>(){

                    Dataset currentPage;

                    @Override
                    public boolean hasNext() {
                        if(currentPage == null){
                            return true;
                        }
                        return currentPage.getPagination() != null && currentPage.getPagination().getNextPageUrl() != null;
                    }

                    @Override
                    public Dataset next() {
                        if(currentPage == null) {
                            currentPage = HttpUtils.postAs(getSearchUrl(), queryJson, Dataset.class);
                        }else if(currentPage.getPagination() == null || currentPage.getPagination().getNextPageUrl() == null){
                            return null;
                        } else if(currentPage.getPagination().getNextPageUrl() != null){
                            currentPage = HttpUtils.getAs(getAbsoluteUrl(currentPage.getPagination().getNextPageUrl()), Dataset.class);
                        }

                        if(currentPage.getSchema().getRef() != null){
                            currentPage.setSchema(HttpUtils.getAs(getAbsoluteUrl(currentPage.getSchema().getRef()), Schema.class));
                            currentPage.getSchema().setPropertyMap(resolveRefs(currentPage.getSchema().getPropertyMap()));
                        }
                        return currentPage;
                    }
                };
            }
        };
    }
}
