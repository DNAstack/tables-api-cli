package org.ga4gh.dataset.cli.cmd;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ClassUtils;
import org.ga4gh.dataset.cli.AuthOptions;
import org.ga4gh.dataset.cli.LoggingOptions;
import org.ga4gh.dataset.cli.OutputOptions;
import org.ga4gh.dataset.cli.util.Outputter;
import org.ga4gh.dataset.cli.ga4gh.Dataset;
import org.ga4gh.dataset.cli.util.DatasetFetcher;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.util.*;

@Command(name = "get", description = "Get dataset (*=required argument)", requiredOptionMarker='*', sortOptions = false)
public class Get implements Runnable {

    @Mixin private LoggingOptions loggingOptions;
    @Mixin private OutputOptions outputOptions;
    @Mixin private AuthOptions authOptions;

    @Option(
            names = {"-I", "--dataset-id", "--id"},
            description = "Dataset ID",
            required = true)
    private String datasetId;

    @Option(names = {"--dataset-endpoint"},
            description = "Dataset endpoint (this argument will be deprecated soon)",
            required=false)
    private String datasetEndpoint;

    private ObjectMapper objectMapper = new ObjectMapper();

    private List<String> propertyKeys;

    private void assertSchemaPropertyKeysAreSameASFirstPagePropertyKeys(LinkedHashMap<String, Object> propertyMapToTest){
        Set<String> foundKeys = propertyMapToTest.keySet();
        if(!propertyKeys.containsAll(foundKeys)){
            throw new IllegalArgumentException("Unexpected schema properties found on a page that do not match schema on first page");
        }else if(!foundKeys.containsAll(propertyKeys)){
            throw new IllegalArgumentException("Missing schema properties on page that were present in first page schema.");
        }
    }

    private List<String> getRow(Map<String, Object> object){
        List<String> outputLine = new ArrayList<>(propertyKeys.size());
        for(String key : propertyKeys){
            Object value = object.get(key);
            if(value == null) {
                outputLine.add("");
            }else if((value instanceof String) || (ClassUtils.isPrimitiveOrWrapper(value.getClass()))){
                outputLine.add(value.toString());
            }else{
                try {

                    outputLine.add(objectMapper.writeValueAsString(value));
                }catch(JsonProcessingException jpe){
                    throw new IllegalArgumentException(jpe);
                }
            }
        }
        return outputLine;
    }

    private String getPropertyAsJson(Object property){
        try {
            return objectMapper.writeValueAsString(property);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }


    @Override
    public void run() {
        loggingOptions.setupLogging();
        authOptions.initAuth();
        DatasetFetcher datasetFetcher = new DatasetFetcher(datasetId, false);
        if(datasetEndpoint != null){
            datasetFetcher.setDatasetEndpoint(datasetEndpoint);
        }
        if(outputOptions.getOutputMode() == OutputOptions.OutputMode.JSON){
                System.out.print("[");
                boolean first = true;
                for (Dataset dataset : datasetFetcher.getPage()) {
                    if(!first){
                        System.out.print(",");
                    }
                    String propertyListAsJson = dataset.getObjects().stream()
                                                       .map(this::getPropertyAsJson)
                                                       .reduce((p1,p2)->p1+","+p2).get();
                    System.out.print(propertyListAsJson);
                    first = false;

                }
                System.out.println("]");
        }else {
            try (Outputter outputter = outputOptions.getOutputter()) {
                for (Dataset dataset : datasetFetcher.getPage()) {
                    if (propertyKeys == null) {
                        propertyKeys = new ArrayList<>(dataset.getSchema().getPropertyMap().keySet());
                        outputter.emitHeader(propertyKeys);
                    } else {
                        assertSchemaPropertyKeysAreSameASFirstPagePropertyKeys(dataset.getSchema().getPropertyMap());
                    }

                    for (Map<String, Object> object : dataset.getObjects()) {
                        outputter.emitLine(getRow(object));
                    }
                }
            }
        }
    }

}
