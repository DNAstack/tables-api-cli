package com.dnastack.ga4gh.dataset.cli.util;

import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.HttpUrl;
import com.dnastack.ga4gh.dataset.cli.ga4gh.Dataset;
import com.dnastack.ga4gh.dataset.cli.ga4gh.Schema;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class DatasetFetcher {
    private static final String DATASET_GET_ENDPOINT_TEMPLATE = "dataset/%s";  //THIS IS THE OLD ENDPOINT!

    private String datasetEndpointTemplate = DATASET_GET_ENDPOINT_TEMPLATE;
    private final String datasetId;
    private final boolean recurseRefs;
    protected final String accessToken;


    public DatasetFetcher(String datasetId, boolean recursePropertyRefs, String accessToken){
        this.datasetId = datasetId;
        this.recurseRefs = recursePropertyRefs;
        if (accessToken == null || accessToken.isEmpty()) {
            this.accessToken = null;
        } else {
            this.accessToken = accessToken;
        }
    }

    public void setDatasetEndpoint(String endpoint){
        this.datasetEndpointTemplate = endpoint+"/%s";
    }

    protected String getAbsoluteUrl(String urlOrPath){
        try {
            URI u = new URI(urlOrPath);
            if(u.isAbsolute()){
                return urlOrPath;
            }else{
                String baseURL = ConfigUtil.getUserConfig().getApiUrl();
                String path = String.format(datasetEndpointTemplate, urlOrPath);
                return HttpUrl.parse(baseURL).newBuilder()
                                     .addPathSegments(path).build().url().toString();

            }
        }catch(URISyntaxException use){
            throw new IllegalArgumentException(use);
        }
    }


    protected LinkedHashMap<String, Object> resolveRefs(LinkedHashMap<String, Object> properties){
        String refUrl = (String)properties.get("$ref");
        if(refUrl != null){
            LinkedHashMap<String, Object> resolvedProperties = HttpUtils.getAs(getAbsoluteUrl(refUrl), new TypeReference<LinkedHashMap<String,Object>>(){}); //don't recurse, references at a deeper level than this won't be expanded.
            if(recurseRefs){
                return resolveRefs(resolvedProperties);
            }else{
                return resolvedProperties;
            }
        }else {
            return properties;
        }
    }

    public Iterable<Dataset> getPages(){
        //String datasetUrl = String.format(datasetEndpointTemplate, datasetId);
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
                            currentPage = HttpUtils.getAs(getAbsoluteUrl(datasetId), Dataset.class, accessToken);
                        }else if(currentPage.getPagination() == null || currentPage.getPagination().getNextPageUrl() == null){
                            return null;
                        } else if(currentPage.getPagination().getNextPageUrl() != null){
                            currentPage = HttpUtils.getAs(getAbsoluteUrl(currentPage.getPagination().getNextPageUrl()), Dataset.class, accessToken);
                        }

                        if(currentPage.getSchema().getRef() != null){
                            currentPage.setSchema(HttpUtils.getAs(getAbsoluteUrl(currentPage.getSchema().getRef()), Schema.class, accessToken));
                            currentPage.getSchema().setPropertyMap(resolveRefs(currentPage.getSchema().getPropertyMap()));
                        }
                        return currentPage;
                    }
                };
            }
        };
    }
}
