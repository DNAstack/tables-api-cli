package org.ga4gh.dataset.cli.cmd;

import java.io.File;

import org.ga4gh.dataset.cli.ClientUtil;
import org.ga4gh.dataset.cli.ConfigUtil;
import org.ga4gh.dataset.client.DatasetFileStorage;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "download", description = "Download dataset")
public class Download implements Runnable {

    @Option(
            names = {"-I", "--dataset-id", "--id"},
            description = "Dataset ID",
            required = true)
    private String datasetId;

    @Option(
            names = {"-o", "--output-dir", "--output"},
            description = "Output directory",
            required = true)
    private File outputDir;

    @Override
    public void run() {
        try {
            var client = ClientUtil.createClient(ConfigUtil.getUserConfig());

            var dataset = client.getDataset(datasetId);
            DatasetFileStorage fileStorage = new DatasetFileStorage(outputDir);
            fileStorage.add(datasetId, dataset);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
