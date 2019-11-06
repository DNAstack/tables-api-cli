package com.dnastack.ga4gh.tables.cli.cmd;

import com.dnastack.ga4gh.tables.cli.config.ConfigUtil;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.dnastack.ga4gh.tables.cli.publisher.Publisher;
import com.dnastack.ga4gh.tables.cli.util.TableSearcher;
import com.dnastack.ga4gh.tables.cli.util.option.OutputOptions;
import com.dnastack.ga4gh.tables.cli.util.option.PublishOptions;
import com.dnastack.ga4gh.tables.cli.util.outputter.Outputter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "query", mixinStandardHelpOptions = true, description = "Query tables (*=required argument)", requiredOptionMarker = '*', sortOptions = false)
public class Query extends BaseCmd {

    @Mixin
    private OutputOptions outputOptions;
    @Mixin
    private PublishOptions publishOptions;


    @ArgGroup(multiplicity = "1")
    ExclusiveQuery query;

    private static class ExclusiveQuery {

        @Option(
            names = {"-q", "--query"},
            description = "SQL search query",
            required = true)
        private String stringQuery;

        @Option(
            names = {"-f", "--file"},
            description = "SQL search query contained in a file",
            required = true)
        private File fileQuery;

        String getQuery() {

            if (stringQuery != null) {
                return stringQuery;
            } else {
                try {
                    return Files.readString(fileQuery.toPath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public void runExceptionally() {
        TableSearcher datasetSearcher = new TableSearcher(query.getQuery(), false, ConfigUtil.getAccessTokenOrNull());
        try (Outputter outputter = outputOptions.getOutputter()) {
            Publisher publisher = publishOptions.getPublisher(null);
            int pageNum = 0;
            for (TableData dataset : datasetSearcher.getDataPages()) {
                outputter.output(dataset, pageNum == 0);
                publisher.publish(dataset, pageNum);
                pageNum++;
            }
        }
    }
}
