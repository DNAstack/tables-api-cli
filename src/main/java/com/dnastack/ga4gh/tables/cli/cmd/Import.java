package com.dnastack.ga4gh.tables.cli.cmd;

import com.dnastack.ga4gh.tables.cli.config.Config;
import com.dnastack.ga4gh.tables.cli.config.ConfigUtil;
import com.dnastack.ga4gh.tables.cli.input.TableFetcher;
import com.dnastack.ga4gh.tables.cli.input.TableFetcherFactory;
import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.util.Importer;
import com.dnastack.ga4gh.tables.cli.util.option.OutputOptions;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import picocli.CommandLine;
import picocli.CommandLine.Mixin;

import java.io.*;
import java.nio.file.NoSuchFileException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


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

    static{
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_VALUES, true);
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
    }

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
            System.out.println("file not ofund");
            fnfe.printStackTrace();
            throw new IllegalArgumentException(fnfe);
        } catch (IOException ie) {
            System.out.println("file not found");
            ie.printStackTrace();
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
        String cellValue = null;
        try {
            Map<String, Object> headerValueMap = new LinkedHashMap<>();
            for (Map.Entry<String, Integer> e : headerMap.entrySet()) {
                cellValue = getCell(record, (Integer) e.getValue());

                if (cellValue != null) {
                    //System.out.println("WARNING: assuming numeric looking CSV values are integers");
                    //                if(StringUtils.isNumeric(cellValue)){
                    //                    headerValueMap.put(e.getKey(), Integer.parseInt(cellValue));
                    //                }else {
                    if (cellValue.startsWith("{") && cellValue.endsWith("}")) {
                        //assume it's json
                        cellValue = cellValue.replaceAll(":\\s*False", ":false");
                        cellValue = cellValue.replaceAll(":\\s*True", ":true"); //couldn't get Jackson to do this automatically.
                        Map m = objectMapper.readValue(cellValue, new TypeReference<Map<String, Object>>() {
                        });
                        if(!m.containsKey("resource")){
                            throw new RuntimeException("Couldn't find expected resource.");
                        }
                        headerValueMap.put(e.getKey(), m.get("resource"));
                    } else {
                        //assume it's a string.
                        headerValueMap.put(e.getKey(), cellValue);
                    }
                    //}
                }
            }
            return headerValueMap;
        } catch (JsonMappingException e) {
            e.printStackTrace();
            System.err.println(cellValue);
            throw new RuntimeException("Can't continue", e);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            System.err.println(cellValue);
            throw new RuntimeException("Can't continue", e);
        }
    }

    @Override
    public void runCmd() throws IOException {
        System.err.println("WARNING: this version has been modified for importing CSVs containing bad json (mixed case Booleans), and is not suitable for general purpose tasks");
        System.err.println("It was used to import FHIR data from 'kidsfirst'");
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
            System.out.println("Inferring csv format");
            csvFormat = inferCsvFormatFromExtension(inputFile.toLowerCase());
        } else {
            System.out.println("using predefinedCsvFormat");
            csvFormat = predefinedCsvFormat.getFormat();
        }

        csvFormat = applyFormatOptions(csvFormat);
        System.out.println("CSVFormat: "+csvFormat.toString());
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
//        System.out.println("Checking headerMap");
//        headerMap.entrySet().forEach(entry-> {
//                                         System.out.println("Got entry: " + entry.getKey() + " => " + entry.getValue());
//                                     });

        if(schema != null) {
            assertHeaderInSchema(headerMap, schema);
        }

//        schema.getUnprocessedProperties().entrySet().stream().forEach((entry)->{
//            System.out.println("Got unprocessed prop map entry: " + entry.getKey() + " => " + entry.getValue()+ "(value type: "+entry.getValue().getClass().getCanonicalName()+")");
//        });

        for (CSVRecord record : csvParser) {
            JSONObject jsonObject = null;
            String jsonAsString = null;
            try {
//                System.out.println("Checking csv record "+record.toString());
                Map<String, Object> json = getJsonFromCsvRecord(headerMap, record);
//                System.out.println("json map dump:");
//                json.entrySet().stream().forEach((entry)->{
//                    System.out.println("Got json map entry: " + entry.getKey() + " => " + entry.getValue()+ "(value type: "+entry.getValue().getClass().getCanonicalName()+")");
//                });
//                System.out.println("end json map dump");
                if(schema != null) {
                    jsonAsString = objectMapper.writeValueAsString(json);
                    jsonObject=new JSONObject(jsonAsString);
                    schema.validate(jsonObject);
                    importer.addRecord(json);
                }
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
        System.out.println("Done.");
    }

    // List is empty, list doesn't exist
    private boolean isDestinationEmpty() throws IOException {
        Config config = ConfigUtil.getUserConfig();
        TableFetcher tableDataFetcher = TableFetcherFactory
                .getTableFetcher(outputOptions.getDestination(), false, config.getRequestAuthorization());

        try {
            ListTableResponse tableList = tableDataFetcher.list();
            return (tableList == null) || (tableList.getTables().size() == 0);
        } catch (NoSuchFileException | FileNotFoundException e) {
             return true;
        }
    }
}
