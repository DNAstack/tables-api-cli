package com.dnastack.ga4gh.tables.cli.cmd;

import com.dnastack.ga4gh.tables.cli.config.ConfigUtil;
import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.util.HttpUtils;
import com.dnastack.ga4gh.tables.cli.util.option.OutputOptions;
import com.dnastack.ga4gh.tables.cli.util.outputter.Outputter;
import okhttp3.HttpUrl;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

//import lombok.extern.slf4j.Slf4j;

//@Slf4j
@Command(name = "list", mixinStandardHelpOptions = true, description = "List Tables", requiredOptionMarker = '*', sortOptions = false)
public class ListTables extends BaseCmd {

    private static final String TABLES_LIST_ENDPOINT = "tables";

    @Mixin
    private OutputOptions outputOptions;


    private String getAbsoluteUrl() {
        String baseURL = ConfigUtil.getUserConfig().getApiUrl();

        if (baseURL == null) {
            throw new IllegalArgumentException("No Tables API has been set");
        }

        return HttpUrl.parse(baseURL).newBuilder()
            .addPathSegments(TABLES_LIST_ENDPOINT).build().url().toString();
    }

    @Override
    public void runExceptionally() {
        String tableListUrl = getAbsoluteUrl();
        ListTableResponse tableList = HttpUtils
            .getAs(tableListUrl, ListTableResponse.class, ConfigUtil.getUserConfig().getRequestAuthorization());
        try (Outputter outputter = outputOptions.getOutputter()) {
            outputter.output(tableList);
        }
    }


}
