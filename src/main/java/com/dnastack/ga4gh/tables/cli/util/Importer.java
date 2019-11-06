package com.dnastack.ga4gh.tables.cli.util;

import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Pagination;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;

//import lombok.extern.slf4j.Slf4j;

//@Slf4j
public class Importer implements Closeable {

    private static final String TABLES_FILE = "tables";
    private static final String TABLE_DIR = "table";

    private final File outputDir;
    private final File dataDir;
    private final File inputDataModelFile;
    private final boolean quiet;
    private String tableName;
    private String relPathToDataModel;
    private String description;
    private ObjectMapper objectMapper = new ObjectMapper().setDefaultPropertyInclusion(Include.NON_NULL);
    private final int numPerPage;
    private List<Map<String, Object>> currentPageObjects;
    private int pageNumber;


    public Importer(String outputDirectory, String tableName, String description, int numPerPage, String pathToInputDataModel, String dataModelId, boolean quiet) {
        this.numPerPage = numPerPage;
        this.tableName = tableName;
        this.currentPageObjects = new ArrayList<>(numPerPage);
        this.outputDir = new File(outputDirectory);
        this.dataDir = new File(outputDir, TABLE_DIR + "/" + tableName);
        this.quiet = quiet;

        if (!dataDir.exists()) {
            dataDir.mkdirs();
        } else if (!dataDir.isDirectory()) {
            throw new IllegalArgumentException("Can't output datasets to directory " + dataDir.getAbsolutePath()
                + " -- path already exists, but is not a directory.");
        }
        this.pageNumber = 1;
        this.relPathToDataModel = "data_models/" + dataModelId;
        this.description = description == null ? "Generated Table" : description;
        this.inputDataModelFile = new File(pathToInputDataModel);

    }

    private void outputMessage(String message) {
        if (!quiet) {
            System.out.println(message);
        }
    }

    private String getRelativePathToPage(int pageNumber) {
        return (pageNumber > 1) ? String.format("%s_%s", "data", pageNumber) : String.format("%s", "data");
    }

    private void writeCurrentPage(boolean isLastRecord) throws IOException {
        String fileName = getRelativePathToPage(pageNumber);
        TableData tableData = new TableData();
        Pagination pagination = new Pagination();
        if (!isLastRecord) {
            pagination.setNextPageUrl(getRelativePathToPage(pageNumber + 1));
            if (pageNumber > 1) {
                pagination.setPreviousPageUrl(getRelativePathToPage(pageNumber - 1));
            }
        } else if (pageNumber > 1) {
            pagination.setPreviousPageUrl(getRelativePathToPage(pageNumber - 1));
        }

        File destFile = new File(outputDir, TABLE_DIR + "/" + tableName + "/" + fileName);
        if (destFile.exists()) {
            throw new IllegalArgumentException(
                "Can't write dataset " + destFile.getAbsolutePath() + " as it collides with an existing dataset.");
        }

        tableData.setPagination(pagination);
        tableData.setData(currentPageObjects);
        LinkedHashMap<String, Object> dataModel = new LinkedHashMap<>();
        dataModel.put("$ref", relPathToDataModel);
        tableData.setDataModel(dataModel);
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(destFile, true)))) {
            writer.printf(objectMapper.writeValueAsString(tableData));
            pageNumber++;
        }
        outputMessage("Wrote data file " + destFile);
    }

    public void addRecord(Map<String, Object> json) throws IOException {
        if (currentPageObjects.size() == numPerPage) {
            writeCurrentPage(false);
            currentPageObjects.clear();
        }
        currentPageObjects.add(json);
    }

    @Override
    public void close() throws IOException {
        if (currentPageObjects.size() > 0) {
            writeCurrentPage(true);
        }
        //write out the dataset index.
        writeTablesList();
        writeDataModel();
        writeTableInfo();
    }

    private void writeTablesList() throws IOException {
        File tablesFile = new File(outputDir, TABLES_FILE);
        ListTableResponse infoList;
        if (tablesFile.exists()) {
            try {
                infoList = objectMapper.readValue(FileUtils.openInputStream(tablesFile),
                    new TypeReference<ListTableResponse>() {

                    });
            } catch (IOException ie) {
                throw new RuntimeException(
                    "Error writing out tables index: an index already exists at " + tablesFile.getCanonicalPath()
                        + ", but could not be appended to. ", ie);
            }
        } else {
            infoList = new ListTableResponse();
        }

        List<Table> tables = infoList.getTables();
        if (tables == null) {
            tables = new ArrayList<>();
        }

        Table tableInfo = new Table();
        tableInfo.setName(tableName);
        tableInfo.setDescription(description);
        LinkedHashMap<String, Object> dataModel = new LinkedHashMap<>();
        dataModel.put("$ref", TABLE_DIR + "/" + tableName + "/" + relPathToDataModel);
        tableInfo.setDataModel(dataModel);
        tables.add(tableInfo);
        infoList.setTables(tables);
        String json = objectMapper.writeValueAsString(infoList);
        Files.write(tablesFile.toPath(), json.getBytes(), StandardOpenOption.CREATE);
        outputMessage("Wrote dataset index " + json + " to " + tablesFile.toPath());

    }

    private void writeDataModel() throws IOException {
        //Write out the schema
        File dstSchemaFile = new File(outputDir, TABLE_DIR + "/" + tableName + "/" + relPathToDataModel);
        if (dstSchemaFile.exists()) {
            outputMessage("Not writing schema to " + dstSchemaFile.getAbsolutePath() + " -- already there.");
        } else {
            try (OutputStream outputStream = FileUtils.openOutputStream(dstSchemaFile)) {
                Files.copy(inputDataModelFile.toPath(), outputStream);
                outputMessage("Copied " + inputDataModelFile.toPath() + " to " + dstSchemaFile.toPath());
            }
        }
    }

    private void writeTableInfo() throws IOException {

        File dstInfoFile = new File(outputDir, TABLE_DIR + "/" + tableName + "/info");
        Table tableInfo = new Table();
        tableInfo.setName(tableName);
        tableInfo.setDescription(description);
        LinkedHashMap<String, Object> dataModel = new LinkedHashMap<>();
        dataModel.put("$ref", relPathToDataModel);
        tableInfo.setDataModel(dataModel);
        if (dstInfoFile.exists()) {
            throw new RuntimeException(
                "Error writing out table info: a Table info file already exists at " + dstInfoFile.getCanonicalPath());
        } else {
            try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(dstInfoFile, false)))) {
                writer.printf(objectMapper.writeValueAsString(tableInfo));
            }
        }

    }
}
