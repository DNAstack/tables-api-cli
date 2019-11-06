package com.dnastack.ga4gh.tables.cli.publisher;

import com.dnastack.ga4gh.tables.cli.model.Pagination;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class Publisher {

    protected final String destination;
    protected final String blobRoot;
    protected final String tableName;

    public Publisher(String tableName, String destination) {
        this.destination = destination;
        this.tableName = tableName;
        this.blobRoot = getBlobRoot(getObjectRoot(destination));
    }

    private String getBlobRoot(String root) {
        if (root == null) {
            return null;
        } else if (!root.endsWith("/")) {
            root += "/";
        }
        return root + tableName;
    }

    public abstract void publish(Table dataset);

    public abstract void publish(TableData dataset, int pageNum);

    abstract String getObjectRoot(String destination);

    public Pagination getAbsolutePagination(Pagination oldPagination, int pageNum) {
        Pagination newPagination = new Pagination();
        if (pageNum != 0) {
            String prevPage = "data";
            if (pageNum > 1) {
                prevPage += "." + (pageNum - 1);
            }
            newPagination.setPreviousPageUrl(prevPage);
        }
        if (oldPagination.getNextPageUrl() != null) {
            String nextPage = String.format("%s.%s", "data", pageNum + 1);
            newPagination.setNextPageUrl(nextPage);
        }
        return newPagination;
    }

    public String toString(Object dataset) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(dataset);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to process dataset JSON", e);
        }
    }
}
