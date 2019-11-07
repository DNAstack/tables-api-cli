package com.dnastack.ga4gh.tables.cli.publisher;

import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileSystemPublisher extends Publisher {

    public FileSystemPublisher(String tableName, String destination) {
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
            System.out.println("Publishing info to: " + destinationFile.toString());
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
            System.out.println("Publishing data to: " + destinationFile.toString());
            destinationFile.getParentFile().mkdirs();
            mapper.writeValue(destinationFile, tableData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    String getObjectRoot(String destination) {
        Path currentDirectory = Paths.get(".");
        Path destinationPath = Paths.get(destination);
        Path resolvedPath = currentDirectory.resolve(destinationPath);

        return resolvedPath.toString();
    }
}
