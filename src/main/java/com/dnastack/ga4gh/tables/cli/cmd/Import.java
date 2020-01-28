package com.dnastack.ga4gh.tables.cli.cmd;

import com.dnastack.ga4gh.tables.cli.config.Config;
import com.dnastack.ga4gh.tables.cli.config.ConfigUtil;
import com.dnastack.ga4gh.tables.cli.input.TableFetcher;
import com.dnastack.ga4gh.tables.cli.input.TableFetcherFactory;
import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.util.Importer;
import com.dnastack.ga4gh.tables.cli.util.option.OutputOptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import picocli.CommandLine;
import picocli.CommandLine.Mixin;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

//import lombok.extern.slf4j.Slf4j;

@CommandLine.Command(name = "import", mixinStandardHelpOptions = true, description = "Import table (*=required argument)", requiredOptionMarker = '*', sortOptions = false)
//@Slf4j
public class Import extends BaseCmd {


    @Mixin
    private OutputOptions outputOptions;

    @CommandLine.Option(
            names = {"-d", "--table-description", "--description"},
            description = "Output Table Description",
            required = false)
    private String description;

    @CommandLine.Option(
            names = {"-i", "--input-file"},
            description = "Input file in CSV or TSV format.  Column headers must be present.",
            required = true)
    private String inputFile;

    @CommandLine.Option(
            names = {"-dm", "--data-model"},
            description = "Input data model in JSON SCHEMA format",
            required = true)
    private String inputModel;

    @CommandLine.Option(names = {"-q",
            "--quiet"}, description = "If set, output messages are suppressed", required = false)
    private boolean quiet = false;

    @CommandLine.Option(names = {"--input-format"}, description = "Valid values: ${COMPLETION-CANDIDATES}", required = false)
    private CSVFormat.Predefined predefinedCsvFormat = null;

    @CommandLine.Option(names = {"--delimiter"}, description = "Delimiter character in input file", required = false)
    private Character delimiter;

    @CommandLine.Option(names = {"--quote"}, description = "Quote character in input file", required = false)
    private Character quoteChar;

    @CommandLine.Option(names = {
            "--record-seperator"}, description = "Record seperator in input file", required = false)
    private String recordSeperator;

    @CommandLine.Option(names = {
            "--ignore-empty-lines"}, description = "Whether to ignore empty lines in input file", required = false)
    private Boolean ignoreEmptyLines;

    @CommandLine.Option(names = {
            "--skip-malformed-lines"}, description = "Whether to skip malformed inputs instead of exiting", required = false)
    private Boolean skipMalformedLines;

    @CommandLine.Option(names = {
            "--page-size"}, description = "max Number of entries per page in the generated output", required = false)
    private int pageSize = 25;


    private static final ObjectMapper objectMapper = new ObjectMapper();

    private Schema getDataModel() {
        try (InputStream inputStream = new FileInputStream(inputModel)) {
            JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
            SchemaLoader loader = SchemaLoader.builder()
                    .schemaJson(rawSchema)
                    .draftV7Support() // or draftV7Support()
                    .build();
            Schema schema = loader.load().build();
            if (schema.getId() == null) {
                throw new IllegalArgumentException("The provided input schema has no id");
            }
            return schema;
        } catch (FileNotFoundException fnfe) {
            throw new IllegalArgumentException(fnfe);
        } catch (IOException ie) {
            throw new UncheckedIOException(ie);
        }
    }

    private CSVFormat inferCsvFormatFromExtension(String lcFilename) {
        String ext = FilenameUtils.getExtension(lcFilename);
        if (ext.equals("tdf") || ext.equals("tsv")) {
            return CSVFormat.TDF;
        } else if (ext.equals("csv")) {
            return CSVFormat.DEFAULT;
        }
        return CSVFormat.DEFAULT;
    }

    private CSVFormat applyFormatOptions(CSVFormat format) {
        if (delimiter != null) {
            format = format.withDelimiter(delimiter);
        }

        if (quoteChar != null) {
            format = format.withQuote(quoteChar);
        }

        if (recordSeperator != null) {
            format = format.withRecordSeparator(recordSeperator);
        }

        if (ignoreEmptyLines != null) {
            format = format.withIgnoreEmptyLines(ignoreEmptyLines);
        }
        return format.withAllowMissingColumnNames(false).withHeader();
    }


    private void assertHeaderInSchema(Map<String, Integer> headerMap, Schema schema) {
        Set<String> schemaProperties = schema.getUnprocessedProperties().keySet();
        Set<String> headerProperties = headerMap.keySet();

        schemaProperties.containsAll(headerProperties);

    }

    private String getCell(CSVRecord record, Integer index) {
        String result = record.get(index);
        return (result == null || result.isEmpty()) ? null : result;
    }

    private Map<String, Object> getJsonFromCsvRecord(Map<String, Integer> headerMap, CSVRecord record) {
        Map<String, Object> headerValueMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : headerMap.entrySet()) {
            String cellValue = getCell(record, (Integer) e.getValue());
            if (cellValue != null) {
                headerValueMap.put(e.getKey(), cellValue);
            }
        }
        return headerValueMap;
    }

    @Override
    public void runCmd() {

        if (outputOptions.getDestination() == null) {
            outputOptions.setDestination(ConfigUtil.getUserConfig().getApiUrl());
        }

        if (outputOptions.getDestinationTableName() == null) {
            File f = new File(inputFile);
            outputOptions.setDestinationTableName(f.getName().replace('.', '_'));
        }

        if (!isDestinationEmpty()) {
            System.err.println("The bucket/directory you are trying to import into already has data. Please delete \n" +
                    "your existing data if you would like to import any new data into this bucket.");
            return;
        }

        //Read and validate schema.
        Schema schema = getDataModel();

        //Read in the CSV.
        CSVFormat csvFormat;
        if (predefinedCsvFormat == null) {
            csvFormat = inferCsvFormatFromExtension(inputFile.toLowerCase());
        } else {
            csvFormat = predefinedCsvFormat.getFormat();
        }

        csvFormat = applyFormatOptions(csvFormat);

        try (CSVParser csvParser = new CSVParser(new BufferedReader(new FileReader(inputFile)), csvFormat)) {
            try (Importer importer = new Importer(outputOptions, description, pageSize, inputModel)) {
                importCsvRecords(importer, schema, csvParser);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void importCsvRecords(Importer importer, Schema schema, CSVParser csvParser) {
        Map<String, Integer> headerMap = csvParser.getHeaderMap();
        assertHeaderInSchema(headerMap, schema);
        for (CSVRecord record : csvParser) {
            try {
                Map<String, Object> json = getJsonFromCsvRecord(headerMap, record);
                schema.validate(new JSONObject(objectMapper.writeValueAsString(json)));
                importer.addRecord(json);
            } catch (JsonProcessingException | ValidationException ex) {
                if (skipMalformedLines != null && skipMalformedLines == true) {
                    System.err.println(
                            "Error processing record " + record.getRecordNumber() + " -- " + ex.getMessage()
                                    + ", skipping.");
                } else {
                    throw new IllegalArgumentException(ex);
                }
            }
        }
    }

    // List is empty, list doesn't exist
    private Boolean isDestinationEmpty() {
        Config config = ConfigUtil.getUserConfig();
        TableFetcher tableDataFetcher = TableFetcherFactory
                .getTableFetcher(outputOptions.getDestination(), false, config.getRequestAuthorization());

        try {
            ListTableResponse tableList = tableDataFetcher.list();
            return (tableList == null) || (tableList.getTables().size() == 0);
        } catch (RuntimeException e) {
            return true;
        }
    }
}
