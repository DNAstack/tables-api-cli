package com.dnastack.ga4gh.tables.cli.cmd;

import com.dnastack.ga4gh.tables.cli.config.Config;
import com.dnastack.ga4gh.tables.cli.config.ConfigUtil;
import com.dnastack.ga4gh.tables.cli.fetch.TableFetcher;
import com.dnastack.ga4gh.tables.cli.fetch.TableFetcherFactory;
import com.dnastack.ga4gh.tables.cli.output.OutputWriter;
import com.dnastack.ga4gh.tables.cli.util.option.OutputOptions;
import java.io.IOException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

//import lombok.extern.slf4j.Slf4j;

//@Slf4j
@Command(name = "list", mixinStandardHelpOptions = true, description = "List Tables", requiredOptionMarker = '*', sortOptions = false)
public class ListTables extends AuthorizedCmd {

    private static final String TABLES_LIST_ENDPOINT = "tables";

    @Mixin
    private OutputOptions outputOptions;


    @Override
    public void runCmd() {
        Config config = ConfigUtil.getUserConfig();
        TableFetcher tableDataFetcher = TableFetcherFactory
            .getTableFetcher(config.getApiUrl(), false, config.getRequestAuthorization());
        try (OutputWriter outputWriter = outputOptions.getWriter()) {
            outputWriter.write(tableDataFetcher.list());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
