package com.dnastack.ga4gh.tables.cli.util;

import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Pagination;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.dnastack.ga4gh.tables.cli.output.OutputWriter;
import com.dnastack.ga4gh.tables.cli.util.option.OutputOptions;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//import lombok.extern.slf4j.Slf4j;

//@Slf4j
public class Importer implements Closeable {

    private static final String TABLES_FILE = "tables";
    private static final String TABLE_DIR = "table";

    private String description;
    private ObjectMapper objectMapper = new ObjectMapper().setDefaultPropertyInclusion(Include.NON_NULL);
    private final int pageSize;
    private List<Map<String, Object>> currentPageObjects;
    private LinkedHashMap<String, Object> dataModel;
    private OutputOptions tablePublishOptions;

    private final OutputWriter infoWriter;
    private final OutputWriter dataWriter;
    private final OutputWriter listWriter;

    public Importer(OutputOptions outputOptions, String description, int pageSize, String pathToInputDataModel) {
        this.pageSize = pageSize;
        this.currentPageObjects = new ArrayList<>(pageSize);

        tablePublishOptions = outputOptions.clone();
        String tableDest = tablePublishOptions.getDestination();

        if (tableDest != null) {
            tablePublishOptions
                .setDestination(tableDest.endsWith("/") ? tableDest + TABLE_DIR : tableDest + "/" + TABLE_DIR);
        }

        listWriter = new OutputWriter(outputOptions);
        infoWriter = new OutputWriter(tablePublishOptions);
        dataWriter = new OutputWriter(tablePublishOptions.clone());

        this.description = description == null ? "Generated Table" : description;
        this.dataModel = readDataModel(pathToInputDataModel);


    }


    private LinkedHashMap<String, Object> readDataModel(String pathToDataModel) {
        try {
            return objectMapper
                .readValue(new File(pathToDataModel), new TypeReference<LinkedHashMap<String, Object>>() {
                });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addRecord(Map<String, Object> json) {
        if (currentPageObjects.size() == pageSize) {
            writeCurrentPage(false);
            currentPageObjects.clear();
        }
        currentPageObjects.add(json);
    }

    private void writeCurrentPage(boolean lastRecord) {
        TableData tableData = new TableData();
        tableData.setData(currentPageObjects);
        tableData.setDataModel(dataModel);
        Pagination paginationPlaceholder = new Pagination();
        paginationPlaceholder.setPreviousPageUrl("prev");
        if (!lastRecord) {
            paginationPlaceholder.setNextPageUrl("next");
        }
        tableData.setPagination(paginationPlaceholder);
        dataWriter.write(tableData);
    }


    @Override
    public void close() throws IOException {
        if (currentPageObjects.size() > 0) {
            writeCurrentPage(true);
        }
        writeTableInfo();
        writeTablesList();

        infoWriter.close();
        listWriter.close();
        dataWriter.close();
    }

    private void writeTablesList() {
        Table tableInfo = new Table();
        tableInfo.setDescription(description);
        tableInfo.setDataModel(dataModel);
        ListTableResponse listTableResponse = new ListTableResponse();
        listTableResponse.setTables(Arrays.asList(tableInfo));
        listWriter.write(listTableResponse);
    }

    private void writeTableInfo() {
        Table tableInfo = new Table();
        tableInfo.setDescription(description);
        tableInfo.setDataModel(dataModel);
        infoWriter.write(tableInfo);

    }
}
