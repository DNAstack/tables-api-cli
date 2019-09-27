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

    @Override
    public void run() {
        loggingOptions.setupLogging();
        authOptions.initAuth();
        DatasetFetcher datasetFetcher = new DatasetFetcher(datasetId, false);
        if(datasetEndpoint != null){
            datasetFetcher.setDatasetEndpoint(datasetEndpoint);
        }
        Outputter outputter = outputOptions.getOutputter();
        outputter.output(datasetFetcher.getPage());
    }

}
