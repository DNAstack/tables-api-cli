package com.dnastack.ga4gh.tables.cli.cmd;

import com.dnastack.ga4gh.tables.cli.config.Config;
import com.dnastack.ga4gh.tables.cli.config.ConfigUtil;
import com.dnastack.ga4gh.tables.cli.input.TableFetcher;
import com.dnastack.ga4gh.tables.cli.input.TableFetcherFactory;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.dnastack.ga4gh.tables.cli.output.OutputWriter;
import com.dnastack.ga4gh.tables.cli.util.option.OutputOptions;
import java.io.IOException;
import java.util.Iterator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "data", mixinStandardHelpOptions = true, description = "Get table data (*=required argument)", requiredOptionMarker = '*', sortOptions = false)
public class Data extends AuthorizedCmd {

    @Mixin
    private OutputOptions outputOptions;

    @Option(names = {"--max-pages"}, description = "Max number of pages to iterate through", defaultValue =
        Integer.MAX_VALUE + "")
    private int maxPages;

    @Parameters(index = "0", paramLabel = "tableName", description = "The name of the table")
    private String tableName;


    @Override
    public void runCmd () throws IOException {
        Config config = ConfigUtil.getUserConfig();
        TableFetcher tableDataFetcher = TableFetcherFactory
            .getTableFetcher(config.getApiUrl(), false, config.getRequestAuthorization());

        if (outputOptions.getDestinationTableName() == null){
            outputOptions.setDestinationTableName(tableName);
        }

        try (OutputWriter outputWriter = new OutputWriter(outputOptions)) {
            int pageNum = 0;
            Iterator<TableData> data = tableDataFetcher.getData(tableName);
            while (data.hasNext() && pageNum < maxPages) {
                TableData tableData = data.next();
                outputWriter.write(tableData);
                pageNum++;
            }
        }
    }
}
