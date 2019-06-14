package org.ga4gh.dataset.cli;

import org.ga4gh.dataset.DatasetManager;
import org.ga4gh.dataset.client.DatasetManagerFactory;

public class ClientUtil {

    public static DatasetManager createClient(Config userConfig) {
        DatasetManagerFactory factory = new DatasetManagerFactory();

        return factory.createManager(
                userConfig.getApiUrl(), userConfig.getUsername(), userConfig.getPassword());
    }
}
