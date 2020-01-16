package com.dnastack.ga4gh.tables.cli.cmd;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.dnastack.ga4gh.tables.cli.config.Config;
import com.dnastack.ga4gh.tables.cli.config.ConfigUtil;
import com.dnastack.ga4gh.tables.cli.input.TableFetcher;
import com.dnastack.ga4gh.tables.cli.input.TableFetcherFactory;
import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Pagination;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.dnastack.ga4gh.tables.cli.output.OutputWriter;
import com.dnastack.ga4gh.tables.cli.output.publish.AWSPublisher;
import com.dnastack.ga4gh.tables.cli.output.publish.GCSPublisher;
import com.dnastack.ga4gh.tables.cli.util.AwsUtil;
import com.dnastack.ga4gh.tables.cli.util.option.OutputOptions;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "test", mixinStandardHelpOptions = true, description = "test method", requiredOptionMarker = '*', sortOptions = false)
public class Test extends AuthorizedCmd {

    @Mixin
    private OutputOptions outputOptions;


    @Override
    public void runCmd() {

        Config config = ConfigUtil.getUserConfig();
        TableFetcher tableDataFetcher = TableFetcherFactory
                .getTableFetcher(config.getApiUrl(), false, config.getRequestAuthorization());

        // Publish table

        File dataFile = getResource("test/sample_data/test-table/data");
        File infoFile = getResource("test/sample_data/test-table/info");
        File tablesFile = getResource("test/sample_data/tables");

        String tableName = "testTableBoo";

        Table sampleTableInfo = new Table();
        sampleTableInfo.setName(tableName);

        ListTableResponse sampleLTR = new ListTableResponse();

        TableData sampleData = new TableData();
        LinkedHashMap<String, Object> dataModel = new LinkedHashMap<String, Object>();

        ObjectMapper mapper = new ObjectMapper();

        try {
            sampleTableInfo = mapper.readValue(infoFile, Table.class);
            sampleLTR = mapper.readValue(tablesFile, ListTableResponse.class);
            sampleData = mapper.readValue(dataFile, TableData.class);
            String uri = "test/sample_data/test-table/" + sampleData.getDataModel().get("$ref");
            File dataModelFile = getResource(uri);
            dataModel = mapper.readValue(dataModelFile, LinkedHashMap.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        sampleData.setDataModel(dataModel);

        GCSPublisher publisher = new GCSPublisher(OutputOptions.OutputMode.JSON, tableName , config.getApiUrl());
        //assert publisher.getBucket().equals("fizz-dev-test");

        publisher.publish(sampleTableInfo);
        publisher.publish(sampleLTR);
        publisher.publish(sampleData, 0);

        // Fetch Table back

        int maxPages = Integer.MAX_VALUE;

        try {
            int pageNum = 0;
            Iterator<TableData> data = tableDataFetcher.getData(tableName);
            while (data.hasNext() && pageNum < maxPages) {
                TableData tableData = data.next();
                assert(tableData == sampleData);
                pageNum++;
            }
            assert(pageNum == 1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String bucketName = AwsUtil.getBucket(config.getApiUrl());
        final AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.CA_CENTRAL_1).build();


        if (s3Client.doesBucketExist(bucketName)) {
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                    .withBucketName(bucketName)
                    .withPrefix(publisher.getBlobRoot());

            ObjectListing objectListing = s3Client.listObjects(listObjectsRequest);

            while (true) {
                for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                    s3Client.deleteObject(bucketName, objectSummary.getKey());
                }
                s3Client.deleteObject(bucketName,"table/tables");
                if (objectListing.isTruncated()) {
                    objectListing = s3Client.listNextBatchOfObjects(objectListing);
                } else {
                    break;
                }
            }
        }
    }

    public File getResource(String fileURI) {

        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(fileURI);
        if (resource == null) {
            throw new IllegalArgumentException("file is not found!");
        } else {
            return new File(resource.getFile());
        }
    }
}
