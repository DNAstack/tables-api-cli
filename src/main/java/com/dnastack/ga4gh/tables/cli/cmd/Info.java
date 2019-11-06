package com.dnastack.ga4gh.tables.cli.cmd;

import com.dnastack.ga4gh.tables.cli.config.ConfigUtil;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.publisher.Publisher;
import com.dnastack.ga4gh.tables.cli.util.TableFetcher;
import com.dnastack.ga4gh.tables.cli.util.option.OutputOptions;
import com.dnastack.ga4gh.tables.cli.util.option.PublishOptions;
import com.dnastack.ga4gh.tables.cli.util.outputter.Outputter;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Parameters;

@Command(name = "info", mixinStandardHelpOptions = true, description = "Get table info (*=required argument)", requiredOptionMarker = '*', sortOptions = false)
public class Info extends BaseCmd {

    @Mixin
    private OutputOptions outputOptions;
    @Mixin
    private PublishOptions publishOptions;

    @Parameters(index = "0", paramLabel = "tableName", description = "The name of the table")
    private String tableName;


    @Override
    public void runExceptionally() {
        TableFetcher tableInfoFetcher = new TableFetcher(tableName, false, ConfigUtil.getAccessTokenOrNull());
        try (Outputter outputter = outputOptions.getOutputter()) {
            Publisher publisher = publishOptions.getPublisher(tableName);
            Table info = tableInfoFetcher.getInfo();
            outputter.output(info);
            publisher.publish(info);

        }
    }

}
