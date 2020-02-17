package com.dnastack.ga4gh.tables.cli.util;

import com.dnastack.ga4gh.tables.cli.config.ConfigUtil;
import java.net.URI;

public class AbsUtil {

    public static String getObjectRoot(String destination) {
        String path = URI.create(destination).getPath();
        return path.substring(path.indexOf('/', 1) + 1);
    }

    public static String getAccount(String destination) {
        String host = URI.create(destination).getHost();
        return host.substring(0, host.indexOf('.'));
    }

    public static String getContainerName(String destination) {
        String path = URI.create(destination).getPath();
        if (path.startsWith("/")) {
            return path.substring(1, path.indexOf('/', 1));
        }
        return path.substring(0, path.indexOf('/'));
    }

    public static String getConnectionString(String account) {
        final String STORAGE_CONNECTION_STRING_TEMPLATE = "DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s";

        String connectionString = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
        return connectionString != null ? connectionString : String.format(
                STORAGE_CONNECTION_STRING_TEMPLATE, account, getAccountKey());
    }

    public static String getAccountKey() {
        String accountKey = System.getenv("AZURE_STORAGE_KEY");
        return accountKey != null ? accountKey : ConfigUtil.getUserConfig().getAbsAccountKey();
    }
}
