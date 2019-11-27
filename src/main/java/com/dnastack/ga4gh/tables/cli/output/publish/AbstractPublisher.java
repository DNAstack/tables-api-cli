package com.dnastack.ga4gh.tables.cli.output.publish;

import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Pagination;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.dnastack.ga4gh.tables.cli.output.OutputTableFormatter;
import com.dnastack.ga4gh.tables.cli.util.option.OutputOptions.OutputMode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class AbstractPublisher implements Publisher {

    protected final String destination;
    protected final String blobRoot;
    protected final String tableName;

    protected OutputMode outputMode;

    public AbstractPublisher(OutputMode outputMode, String tableName, String destination) {
        this.destination = destination;
        this.tableName = tableName;
        this.blobRoot = getBlobRoot(getObjectRoot(destination));
        this.outputMode = outputMode;

        if (this.outputMode == null) {
            this.outputMode = OutputMode.JSON;
        }
    }


    protected String getContentType() {
        if (outputMode.equals(OutputMode.JSON)) {
            return "application/json";
        } else {
            return "text/plain";
        }
    }

    protected String format(Table table) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (OutputTableFormatter formatter = new OutputTableFormatter(outputMode, outputStream)) {
                formatter.write(table);
            }
            return outputStream.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    protected String format(TableData tableData) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (OutputTableFormatter formatter = new OutputTableFormatter(outputMode, outputStream)) {
                formatter.write(tableData);
            }
            return outputStream.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    protected String format(ListTableResponse listTableResponse) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (OutputTableFormatter formatter = new OutputTableFormatter(outputMode, outputStream)) {
                formatter.write(listTableResponse);
            }
            return outputStream.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getBlobRoot(String root) {
        if (root == null) {
            return null;
        } else if (!root.endsWith("/")) {
            root += "/";
        }
        return root + tableName;
    }


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
