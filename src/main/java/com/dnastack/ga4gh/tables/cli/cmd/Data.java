package com.dnastack.ga4gh.tables.cli.cmd;

import com.dnastack.ga4gh.tables.cli.config.ConfigUtil;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.dnastack.ga4gh.tables.cli.publisher.Publisher;
import com.dnastack.ga4gh.tables.cli.util.TableFetcher;
import com.dnastack.ga4gh.tables.cli.util.option.OutputOptions;
import com.dnastack.ga4gh.tables.cli.util.option.PublishOptions;
import com.dnastack.ga4gh.tables.cli.util.outputter.Outputter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Parameters;

@Command(name = "data", mixinStandardHelpOptions = true, description = "Get table data (*=required argument)", requiredOptionMarker = '*', sortOptions = false)
public class Data extends BaseCmd {

    @Mixin
    private OutputOptions outputOptions;
    @Mixin
    private PublishOptions publishOptions;

    @Parameters(index = "0", paramLabel = "tableName", description = "The name of the table")
    private String tableName;


    @Override
    public void runExceptionally() {
        TableFetcher tableDataFetcher = new TableFetcher(tableName, false, ConfigUtil.getUserConfig()
            .getRequestAuthorization());
        try (Outputter outputter = outputOptions.getOutputter()) {
            Publisher publisher = publishOptions.getPublisher(tableName);
            int pageNum = 0;
            for (TableData dataset : tableDataFetcher.getDataPages()) {
                outputter.output(dataset, pageNum == 0);
                publisher.publish(dataset, pageNum);
                pageNum++;
            }
        }
    }
}
