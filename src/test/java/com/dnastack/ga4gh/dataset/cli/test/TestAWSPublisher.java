package com.dnastack.ga4gh.dataset.cli.test;

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
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.dnastack.ga4gh.tables.cli.output.publish.AWSPublisher;
import com.dnastack.ga4gh.tables.cli.util.AwsUtil;
import com.dnastack.ga4gh.tables.cli.util.option.OutputOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;

@DisplayName("AWS Publisher Tests")
public class TestAWSPublisher {

    @Test
    public void awsPublishAndFetch() {

        String destination = "s3://fizz-dev-test";
        String tableName = "aws-publisher-test-table";
        String TEST_TABLES_LIST_URI = "test/sample_data/";
        String TEST_TABLE_URI = TEST_TABLES_LIST_URI + "test-table/";
        String TABLE_LIST_ENDPOINT = "tables";
        String TABLE_INFO_ENDPOINT = "info";
        String TABLE_DATA_ENDPOINT = "data";

        Table sampleTableInfo = new Table();
        sampleTableInfo.setName(tableName);
        ListTableResponse sampleLTR = new ListTableResponse();
        TableData sampleData = new TableData();
        LinkedHashMap dataModel = new LinkedHashMap<String, Object>();

        Config config = ConfigUtil.getUserConfig();
        TableFetcher tableDataFetcher = TableFetcherFactory
                .getTableFetcher(destination , false, config.getRequestAuthorization());

        // Publish table

        File infoFile = getResource(TEST_TABLE_URI + TABLE_INFO_ENDPOINT);
        File tablesFile = getResource(TEST_TABLES_LIST_URI + TABLE_LIST_ENDPOINT);
        File dataFile = getResource(TEST_TABLE_URI + TABLE_DATA_ENDPOINT);

        ObjectMapper mapper = new ObjectMapper();
        try {
            sampleTableInfo = mapper.readValue(infoFile, Table.class);
            sampleLTR = mapper.readValue(tablesFile, ListTableResponse.class);
            sampleData = mapper.readValue(dataFile, TableData.class);
            String uri = TEST_TABLE_URI + sampleData.getDataModel().get("$ref");
            File dataModelFile = getResource(uri);
            dataModel = mapper.readValue(dataModelFile, LinkedHashMap.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        sampleData.setDataModel(dataModel);

        AWSPublisher publisher = new AWSPublisher(OutputOptions.OutputMode.JSON, tableName , destination );
        publisher.publish(sampleTableInfo);
        publisher.publish(sampleLTR);
        publisher.publish(sampleData, 0);

        // Fetch Table back

        Iterator<TableData> data = tableDataFetcher.getData(tableName);
        TableData tableData = data.next();
        publisher.publish(tableData, 0);
        assert(tableData.equals(sampleData));

        String bucketName = AwsUtil.getBucket(destination);
        final AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.CA_CENTRAL_1).build();


        if (s3Client.doesBucketExistV2(bucketName)) {
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
