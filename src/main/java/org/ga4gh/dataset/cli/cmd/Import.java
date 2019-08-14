package org.ga4gh.dataset.cli.cmd;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.ga4gh.dataset.cli.AuthOptions;
import org.ga4gh.dataset.cli.LoggingOptions;
import org.ga4gh.dataset.cli.util.Importer;
import org.jetbrains.annotations.TestOnly;
import org.json.JSONObject;
import org.json.JSONTokener;
import picocli.CommandLine;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@CommandLine.Command(name = "import", description = "Import dataset (*=required argument)", requiredOptionMarker='*', sortOptions = false)
@Slf4j
public class Import implements Runnable{
    @CommandLine.Mixin
    private LoggingOptions loggingOptions;

    @CommandLine.Mixin
    private AuthOptions authOptions;

    @CommandLine.Option(
            names = {"-I", "--dataset-id", "--id"},
            description = "Output Dataset ID",
            required = true)
    private String datasetId;

    @CommandLine.Option(
            names = {"-i", "--input-file"},
            description = "Input file in CSV or TSV format.  Column headers must be present.",
            required = true)
    private String inputFile;

    @CommandLine.Option(
            names = {"-is", "--input-schema"},
            description = "Input Schema in JSON format",
            required = true)
    private String inputSchema;


    @CommandLine.Option(
            names = {"-o", "--output-dir"},
            description = "Set output directory",
            required = true)
    private String outputDir;

    @CommandLine.Option(names = {"-q", "--quiet"}, description = "If set, output messages are suppressed", required = false)
    private boolean quiet=false;

    @CommandLine.Option(names = {"-f", "--input-format"}, description = "Valid values: ${COMPLETION-CANDIDATES}", required = false)
    private CSVFormat.Predefined predefinedCsvFormat = null;

    @CommandLine.Option(names = {"--delimiter"}, description = "Delimiter character in input file", required=false)
    private Character delimiter;

    @CommandLine.Option(names = {"--quote"}, description = "Quote character in input file", required=false)
    private Character quoteChar;

    @CommandLine.Option(names = {"--record-seperator"}, description = "Record seperator in input file", required=false)
    private String recordSeperator;

    @CommandLine.Option(names = {"--ignore-empty-lines"}, description = "Whether to ignore empty lines in input file", required=false)
    private Boolean ignoreEmptyLines;

    @CommandLine.Option(names = {"--skip-malformed-lines"}, description = "Whether to skip malformed inputs instead of exiting", required=false)
    private Boolean skipMalformedLines;

    @CommandLine.Option(names = {"--page-size"}, description = "max Number of entries per page in the generated output", required=false)
    private int pageSize = 25;


    private static final ObjectMapper objectMapper = new ObjectMapper();

    private Schema getSchema(){
        try (InputStream inputStream = new FileInputStream(inputSchema)) {
            JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
            SchemaLoader loader = SchemaLoader.builder()
                                              .schemaJson(rawSchema)
                                              .draftV7Support() // or draftV7Support()
                                              .build();
            Schema schema =  loader.load().build();
            if(schema.getId() == null){
                throw new IllegalArgumentException("The provided input schema has no id");
            }
            return schema;
        }catch(FileNotFoundException fnfe){
            throw new IllegalArgumentException(fnfe);
        }catch(IOException ie){
            throw new UncheckedIOException(ie);
        }
    }

    private CSVFormat inferCsvFormatFromExtension(String lcFilename){
        String ext = FilenameUtils.getExtension(lcFilename);
        if(ext.equals("tdf") || ext.equals("tsv")){
            return CSVFormat.TDF;
        }else if(ext.equals("csv")) {
            return CSVFormat.DEFAULT;
        }
        return CSVFormat.DEFAULT;
    }

    private CSVFormat applyFormatOptions(CSVFormat format){
        if(delimiter != null){
            format = format.withDelimiter(delimiter);
        }

        if(quoteChar != null){
            format = format.withQuote(quoteChar);
        }

        if(recordSeperator != null){
            format = format.withRecordSeparator(recordSeperator);
        }

        if(ignoreEmptyLines != null){
            format = format.withIgnoreEmptyLines(ignoreEmptyLines);
        }
        return format.withAllowMissingColumnNames(false).withHeader();
    }


    private void assertHeaderInSchema(Map<String, Integer> headerMap, Schema schema){
        Set<String> schemaProperties = schema.getUnprocessedProperties().keySet();
        Set<String> headerProperties = headerMap.keySet();

        schemaProperties.containsAll(headerProperties);

    }

    private String getCell(CSVRecord record, Integer index){
        String result = record.get(index);
        return (result==null || result.isEmpty()) ? null : result;
    }

    private String getJsonFromCsvRecord(Map<String, Integer> headerMap, CSVRecord record) throws JsonProcessingException{
        Map<String, Object> headerValueMap = new LinkedHashMap<>();
        for(Map.Entry<String, Integer> e : headerMap.entrySet()){
            String cellValue = getCell(record, (Integer)e.getValue());
            if(cellValue != null){
                headerValueMap.put(e.getKey(), cellValue);
            }
        }
        return objectMapper.writeValueAsString(headerValueMap);
    }

    @Override
    public void run() {
        loggingOptions.setupLogging();
        authOptions.initAuth();
        //Read and validate schema.
        Schema schema = getSchema();

        //Read in the CSV.
        CSVFormat csvFormat;
        if(predefinedCsvFormat == null){
            csvFormat = inferCsvFormatFromExtension(inputFile.toLowerCase());
        }else{
            csvFormat = predefinedCsvFormat.getFormat();
        }

        csvFormat = applyFormatOptions(csvFormat);

        try (CSVParser csvParser = new CSVParser(new BufferedReader(new FileReader(inputFile)), csvFormat)){
            Map<String, Integer> headerMap = csvParser.getHeaderMap();
            assertHeaderInSchema(headerMap, schema);
            schema.getId();
            try(Importer importer = new Importer(outputDir, datasetId, pageSize, inputSchema, schema.getId(), quiet)) {
                for (CSVRecord record : csvParser) {
                    try {
                        String json = getJsonFromCsvRecord(headerMap, record);
                        schema.validate(new JSONObject(json));
                        importer.addRecord(json);
                    } catch (JsonProcessingException | ValidationException ex) {
                        if (skipMalformedLines != null && skipMalformedLines == true) {
                            System.err.println("Error processing record " + record.getRecordNumber() + " -- " + ex.getMessage() + ", skipping.");
                        } else {
                            throw new IllegalArgumentException(ex);
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
