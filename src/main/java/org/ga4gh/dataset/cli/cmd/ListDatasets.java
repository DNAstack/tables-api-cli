package org.ga4gh.dataset.cli.cmd;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.ga4gh.dataset.cli.AuthOptions;
import org.ga4gh.dataset.cli.util.ConfigUtil;
import org.ga4gh.dataset.cli.util.HttpUtils;
import org.ga4gh.dataset.cli.LoggingOptions;
import org.ga4gh.dataset.cli.OutputOptions;
import org.ga4gh.dataset.cli.util.Outputter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparing;

@Slf4j
@Command(name = "list", description = "List datasets",  requiredOptionMarker='*', sortOptions = false)
public class ListDatasets implements Runnable {

    private static final String DATASET_LIST_ENDPOINT = "datasets";

    @Mixin private AuthOptions authOptions;
    @Mixin private LoggingOptions loggingOptions;
    @Mixin private OutputOptions outputOptions;


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    private static class Datasets{
        private List<Map<String, Object>>  datasets;
    }

    private List<Map<String, Object>> getDatasetList(String datasetListUrl){
        return HttpUtils.getAs(datasetListUrl, Datasets.class).getDatasets();
    }

    private String getAbsoluteUrl(){
        String baseURL = ConfigUtil.getUserConfig().getApiUrl();
        return HttpUrl.parse(baseURL).newBuilder()
                      .addPathSegments(DATASET_LIST_ENDPOINT).build().url().toString();

    }

    @Override
    public void run() {
        loggingOptions.setupLogging();
        authOptions.initAuth();
        String datasetListUrl = getAbsoluteUrl();
        if( outputOptions.getOutputMode() == OutputOptions.OutputMode.JSON){
            System.out.println(HttpUtils.get(datasetListUrl));
        }else {
            var datasetList = getDatasetList(datasetListUrl);
            datasetList.sort(comparing(ds -> (String) ds.get("id")));

            try (Outputter outputter = outputOptions.getOutputter()) {
                outputter.emitHeader("Dataset ID", "Dataset Description");
                for (var info : datasetList) {
                    outputter.emitLine((String) info.get("id"), (String) info.get("description"));
                }
            }
        }
    }
}
