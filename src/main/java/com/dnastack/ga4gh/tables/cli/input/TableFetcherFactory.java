package com.dnastack.ga4gh.tables.cli.input;

import com.dnastack.ga4gh.tables.cli.util.RequestAuthorization;
import java.io.File;
import java.net.URI;

public class TableFetcherFactory {

    public static TableFetcher getTableFetcher(String apiUrl, boolean recurse, RequestAuthorization authorization) {

        URI uri = URI.create(apiUrl);

        if (uri.getScheme() == null || uri.getScheme().equals("file")) {
            return new FileSystemTableFetcher(new File(uri.getPath()).getAbsolutePath(), recurse, authorization);
        } else if (uri.getScheme().equals("gs")) {
            return new GcsTableFetcher(apiUrl, recurse, authorization);
        } else if (uri.getScheme().equals("https") && uri.getHost().endsWith("blob.core.windows.net")) {
            return new ABSTableFetcher(apiUrl, recurse, authorization);
        } else if (uri.getScheme().equals("http") || uri.getScheme().equals("https")) {
            return new HttpTableFetcher(apiUrl, recurse, authorization);
        } else {
            throw new IllegalArgumentException("There are not table fetchers for ApiUrl: " + apiUrl);
        }
    }
}
