package com.dnastack.ga4gh.tables.cli.input;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.dnastack.ga4gh.tables.cli.util.AwsUtil;
import com.dnastack.ga4gh.tables.cli.util.HttpUtils;
import com.dnastack.ga4gh.tables.cli.util.RequestAuthorization;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.amazonaws.AmazonServiceException;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;


import java.io.IOException;

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
    public TableData getDataPage(String conext) {
        return getBlobAs(conext, TableData.class);
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

    private <T> T getBlobAs(String gsUrl, Class<T> clazz) {
        String data = getBlobData(gsUrl);
        try {
            return HttpUtils.getMapper().readValue(data, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T getBlobAs(String gsUrl, TypeReference<T> typeReference) {
        String data = getBlobData(gsUrl);
        try {
            return HttpUtils.getMapper().readValue(data, typeReference);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getBlobData(String awsUrl) {

        String bucket_name = AwsUtil.getBucket(awsUrl);
        String key_name = AwsUtil.getObjectRoot(awsUrl);
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.CA_CENTRAL_1).build();
        try {
            S3Object o = s3.getObject(bucket_name, key_name);
            S3ObjectInputStream s3is = o.getObjectContent();
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
            String output = "";
            while ((line = reader.readLine()) != null) {
                output += line;
            }
            return output;
        } catch (IOException e) {
            return null;
        }
    }
}
