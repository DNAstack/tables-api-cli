package com.dnastack.ga4gh.tables.cli.cmd;

import com.dnastack.ga4gh.tables.cli.config.Config;
import com.dnastack.ga4gh.tables.cli.config.ConfigUtil;
import com.dnastack.ga4gh.tables.cli.input.TableFetcher;
import com.dnastack.ga4gh.tables.cli.input.TableFetcherFactory;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.dnastack.ga4gh.tables.cli.output.OutputWriter;
import com.dnastack.ga4gh.tables.cli.util.option.OutputOptions;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "query", mixinStandardHelpOptions = true, description = "Query tables (*=required argument)", requiredOptionMarker = '*', sortOptions = false)
public class Query extends AuthorizedCmd {

    @Mixin
    private OutputOptions outputOptions;

    @Option(names = {"--max-pages"}, description = "Max number of pages to iterate through")
    private Integer maxPages;

    @ArgGroup(multiplicity = "1")
    ExclusiveQuery query;


    private static class ExclusiveQuery {

        @Option(
            names = {"-q", "--query"},
            description = "SQL search query",
            required = true)
        private String stringQuery;

        @Option(
            names = {"-i","--input-file"},
            description = "SQL search query contained in a file",
            required = true)
        private File fileQuery;

        String getQuery() {

            if (stringQuery != null && !stringQuery.isEmpty()) {
                return stringQuery;
            } else {
                try {
                    return Files.readString(fileQuery.toPath()).trim();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public void runCmd() {
        String q = query.getQuery();
        Config config = ConfigUtil.getUserConfig();
        TableFetcher tableDataFetcher = TableFetcherFactory
            .getTableFetcher(config.getApiUrl(), false, config.getRequestAuthorization());



        try (OutputWriter outputWriter = new OutputWriter(outputOptions)) {
            int pageNum = 0;
            if (maxPages == null) {
                maxPages = Integer.MAX_VALUE;
            }

            Iterator<TableData> data = tableDataFetcher.search(q);
            while (data.hasNext() && pageNum < maxPages) {
                outputWriter.writeSearchResult(data.next());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
