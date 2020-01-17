package com.dnastack.ga4gh.tables.cli.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GcsUtil {

    private final static Pattern GSPattern = Pattern.compile("^gs://(?<bucket>[0-9a-zA-Z_\\-.]+)(?<object>.*)$");

    public static String getBucket(String gsUrl) {
        Matcher matcher = GSPattern.matcher(gsUrl);
        if (matcher.find()) {
            return matcher.group("bucket");
        } else {
            throw new IllegalArgumentException("Could not handle transfer, this is not a google file");
        }
    }


    public static String getObjectRoot(String gsUrl) {
        Matcher matcher = GSPattern.matcher(gsUrl);
        if (matcher.find()) {
            String object = matcher.group("object");
            if (object == null) {
                object = "table";
            } else if (object.equals("/") || object.equals("")) {
                object = "table";
            } else if (object.startsWith("/")) {
                object = object.substring(1);
            }

            return object;
        } else {
            return "table";
        }
    }
}
