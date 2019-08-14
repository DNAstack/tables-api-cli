package org.ga4gh.dataset.cli.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.ga4gh.dataset.cli.ga4gh.DatasetInfo;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class Importer implements Closeable {

    private static final String DATASETS_FILE = "datasets";
    private static final String DATASET_DIR = "dataset";

    private final File outputDir;
    private final File dataDir;
    private final File inputSchemaFile;
    private final boolean quiet;
    private String datasetId;
    private String relPathToSchema;

    private DatasetInfo infoToAppend;
    private ObjectMapper objectMapper = new ObjectMapper();
    private final int numPerPage;
    private List<String> currentPageObjects;
    private int pageNumber;


    public Importer(String outputDirectory, String datasetId, int numPerPage, String pathToInputSchema, String schemaId, boolean quiet) {
        this.numPerPage = numPerPage;
        this.datasetId = datasetId;
        this.currentPageObjects = new ArrayList<>(numPerPage);
        this.outputDir = new File(outputDirectory);
        this.dataDir = new File(outputDir, DATASET_DIR);
        this.quiet = quiet;

        if (!dataDir.exists()) {
            dataDir.mkdirs();
        } else if (!dataDir.isDirectory()) {
            throw new IllegalArgumentException("Can't output datasets to directory " + dataDir.getAbsolutePath() + " -- path already exists, but is not a directory.");
        }
        this.pageNumber = 1;
        this.relPathToSchema = "schema/" + schemaId;
        Map<String, Object> schemaRef = new HashMap<>();
        schemaRef.put("$ref", DATASET_DIR+"/"+relPathToSchema);
        this.infoToAppend = DatasetInfo.builder()
                                       .id(datasetId)
                                       .description("Generated dataset")
                                       .schema(schemaRef)
                                       .build();
        this.inputSchemaFile = new File(pathToInputSchema);

    }

    private void outputMessage(String message){
        if(!quiet){
            System.out.println(message);
        }
    }

    private String getRelativePathToPage(int pageNumber) {
        return (pageNumber > 1) ? String.format("%s_%s", datasetId, pageNumber) : String.format("%s",datasetId);
    }

    private void writeCurrentPage(boolean isLastRecord) throws IOException {
        String fileName = getRelativePathToPage(pageNumber);

        String pagination = null;
        if (!isLastRecord) {
            if (pageNumber == 1) {
                pagination = String.format("{\"next_page_url\":\"%s\"}", getRelativePathToPage(pageNumber + 1));
            } else {
                pagination = String.format("{\"next_page_url\":\"%s\", \"prev_page_url\":\"%s\"}",
                                           getRelativePathToPage(pageNumber + 1),
                                           getRelativePathToPage(pageNumber - 1));
            }
        }else if(pageNumber > 1){
            pagination = String.format("{\"prev_page_url\":\"%s\"}", getRelativePathToPage(pageNumber - 1));
        }

        File destFile = new File(outputDir, DATASET_DIR+"/"+fileName);
        if(destFile.exists()){
            throw new IllegalArgumentException("Can't write dataset "+destFile.getAbsolutePath()+" as it collides with an existing dataset.");
        }

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(destFile, true)))) {
            writer.printf("{\"objects\":[%s]", StringUtils.join(currentPageObjects, ","));
            if (pagination != null) {
                writer.printf(",\"pagination\":%s", pagination);
            }
            writer.printf(",\"schema\":{\"$ref\":\"%s\"}}", relPathToSchema);
            pageNumber++;
        }
        outputMessage("Wrote data file "+destFile);

    }

    public void addRecord(String json) throws IOException {
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
        File datasetFile = new File(outputDir, DATASETS_FILE);
        List<DatasetInfo> infoList;
        if(datasetFile.exists()){
            try {
                infoList = objectMapper.readValue(FileUtils.openInputStream(datasetFile),
                                                  new TypeReference<List<DatasetInfo>>() {

                                                  });
            }catch(IOException ie){
                throw new RuntimeException("Error writing out dataset index: an index already exists at "+datasetFile.getCanonicalPath()+", but could not be appended to. ", ie);
            }
        }else{
            infoList = new ArrayList<>(1);
        }

        infoList.add(infoToAppend);

        String json = "{\"datasets\":"+objectMapper.writeValueAsString(infoList)+"}";

        Files.write(datasetFile.toPath(), json.getBytes(), StandardOpenOption.CREATE);
        outputMessage("Wrote dataset index "+json+" to "+datasetFile.toPath());

        //Write out the schema
        File dstSchemaFile = new File(outputDir, DATASET_DIR+"/"+relPathToSchema);
        if(dstSchemaFile.exists()){
            outputMessage("Not writing schema to "+dstSchemaFile.getAbsolutePath()+" -- already there.");
        }else {
            try (OutputStream outputStream = FileUtils.openOutputStream(dstSchemaFile)) {
                Files.copy(inputSchemaFile.toPath(), outputStream);
                outputMessage("Copied " + inputSchemaFile.toPath() + " to " + dstSchemaFile.toPath());
            }
        }
    }
}
