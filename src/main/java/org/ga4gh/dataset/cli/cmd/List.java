package org.ga4gh.dataset.cli.cmd;

import org.ga4gh.dataset.Dataset;
import org.ga4gh.dataset.DatasetManager;
import org.ga4gh.dataset.cli.ClientUtil;
import org.ga4gh.dataset.cli.ConfigUtil;

import com.fasterxml.jackson.databind.node.ObjectNode;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "list", description = "List datasets")
public class List implements Runnable {

    @Option(names = "--dataset-id")
    private String datasetId;

    @Override
    public void run() {
        DatasetManager client = ClientUtil.createClient(ConfigUtil.getUserConfig());
        // TODO: this should be a dataset listing
        // we need to add this to the dataset-api-java-client
        Dataset<ObjectNode> dataset = client.getDataset(datasetId);
    }
}
