package com.dnastack.ga4gh.tables.cli.output.publish;

import com.dnastack.ga4gh.tables.cli.config.ConfigUtil;
import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.dnastack.ga4gh.tables.cli.util.option.OutputOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConsolePublisher extends AbstractPublisher {


    public ConsolePublisher(String tableName, String destination) {
        super(tableName, destination);
    }

    @Override
    public void publish(Table table) {
        if (!tableName.equals(table.getName())) {
            table.setName(tableName);
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            File destinationFile = new File(blobRoot, "info");
            destinationFile.getParentFile().mkdirs();
            mapper.writeValue(destinationFile, table);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void publish(ListTableResponse table) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File destinationFile = new File(destination, "tables");
            destinationFile.getParentFile().mkdirs();
            mapper.writeValue(destinationFile, table);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void publish(TableData tableData, int pageNum) {

        try {
            TableData modifiedData = new TableData();
            modifiedData.setDataModel(tableData.getDataModel());
            modifiedData.setData(tableData.getData());
            modifiedData.setPagination(getAbsolutePagination(tableData.getPagination(), pageNum));

            ObjectMapper mapper = new ObjectMapper();
            String filename = "data" + (pageNum == 0 ? "" : "." + pageNum);
            File destinationFile = new File(blobRoot, filename);
            destinationFile.getParentFile().mkdirs();
            mapper.writeValue(destinationFile, modifiedData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public String getObjectRoot(String destination) {
        Path currentDirectory = Paths.get(".");
        Path destinationPath = Paths.get(destination);
        Path resolvedPath = currentDirectory.resolve(destinationPath);

        return resolvedPath.toString();
    }
}
