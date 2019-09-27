package org.ga4gh.dataset.cli.cmd;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ClassUtils;
import org.ga4gh.dataset.cli.AuthOptions;
import org.ga4gh.dataset.cli.LoggingOptions;
import org.ga4gh.dataset.cli.OutputOptions;
import org.ga4gh.dataset.cli.ga4gh.Dataset;
import org.ga4gh.dataset.cli.util.DatasetSearcher;
import org.ga4gh.dataset.cli.util.Outputter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.util.*;

@Command(name = "query", description = "Query dataset (*=required argument)", requiredOptionMarker='*', sortOptions = false)
public class Query implements Runnable {

    @Mixin private LoggingOptions loggingOptions;
    @Mixin private OutputOptions outputOptions;
    @Mixin private AuthOptions authOptions;

    @Option(
            names = {"-q", "--query"},
            description = "SQL search query",
            required = true)
    private String query;

    @Override
    public void run() {
        loggingOptions.setupLogging();
        authOptions.initAuth();
        DatasetSearcher datasetSearcher = new DatasetSearcher(query, false);
        Outputter outputter = outputOptions.getOutputter();
        outputter.output(datasetSearcher.getPage());
    }
}
