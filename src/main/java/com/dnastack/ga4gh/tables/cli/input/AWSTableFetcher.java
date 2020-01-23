package com.dnastack.ga4gh.tables.cli.input;


import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.dnastack.ga4gh.tables.cli.util.AwsUtil;
import com.dnastack.ga4gh.tables.cli.util.HttpUtils;
import com.dnastack.ga4gh.tables.cli.util.RequestAuthorization;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class AWSTableFetcher extends AbstractTableFetcher {

    public AWSTableFetcher(String rootApiTarget, boolean recursePropertyRefs, RequestAuthorization authorization) {
        super(rootApiTarget, recursePropertyRefs, authorization);
    }

    @Override
    protected LinkedHashMap<String, Object> resolveRefs(String absoluteRefs) {
        TypeReference<LinkedHashMap<String, Object>> typeReference = new TypeReference<LinkedHashMap<String, Object>>() {
        };
        if (absoluteRefs.startsWith("s3://")) {
            return getBlobAs(absoluteRefs, typeReference);

        } else {
            return HttpUtils.getAs(absoluteRefs, typeReference, authorization);
        }
    }

    @Override
    public TableData getDataPage(String url) {
        return getBlobAs(url, TableData.class);
    }

    @Override
    public ListTableResponse list() {
        return getBlobAs(getListAbsoluteUrl(), ListTableResponse.class);
    }

    @Override
    public Iterator<TableData> search(String query) {
        throw new UnsupportedOperationException("Searching AWS buckets is not currently supported");
    }

    @Override
    public Table getInfo(String tableName) {
        Table info = getBlobAs(getInfoAbsoluteUrl(tableName), Table.class);
        info.setDataModel(resolveRefs(info.getDataModel(), getInfoAbsoluteUrl(tableName)));
        return info;
    }

    protected String getBlobData(String s3Url) {
        String bucket_name = AwsUtil.getBucket(s3Url);
        String key_name = AwsUtil.getObjectRoot(s3Url);
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().build();
        try {
            S3ObjectInputStream s3is = s3.getObject(bucket_name, key_name).getObjectContent();
            return displayTextInputStream(s3is);
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
        }
        return null;
    }

    private static String displayTextInputStream(InputStream input) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line = null;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
            return output.toString();
        } catch (IOException e) {
            return null;
        }
    }
}
