package com.dnastack.ga4gh.tables.cli.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AwsUtil {

    private final static Pattern AWSPattern = Pattern.compile("^s3://(?<bucket>[0-9a-zA-Z_\\-.]+)(?<object>.*)$");

    public static String getBucket(String gsUrl) {
        Matcher matcher = AWSPattern.matcher(gsUrl);
        if (matcher.find()) {
            return matcher.group("bucket");
        } else {
            throw new IllegalArgumentException("Could not handle transfer, this is not a AWS S3 file");
        }
    }


    public static String getObjectRoot(String gsUrl) {
        Matcher matcher = AWSPattern.matcher(gsUrl);
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
