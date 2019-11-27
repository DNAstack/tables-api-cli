package com.dnastack.ga4gh.tables.cli.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GcsUtil {

    private final static Pattern GSPattern = Pattern.compile("^gs://(?<bucket>[0-9a-zA-Z_\\-.]+)(/(?<object>.*))?$");

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
            String obj = matcher.group("object");
            if (obj != null && obj.equals("")) {
                return "table";
            } else {
                return obj;
            }
        } else {
            return "table";
        }
    }


}
