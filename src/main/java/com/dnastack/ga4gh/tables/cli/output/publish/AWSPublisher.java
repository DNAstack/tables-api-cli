package com.dnastack.ga4gh.tables.cli.output.publish;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.dnastack.ga4gh.tables.cli.util.AwsUtil;
import com.dnastack.ga4gh.tables.cli.util.option.OutputOptions.OutputMode;

public class AWSPublisher extends AbstractPublisher {

    private final String bucket;


    public AWSPublisher(OutputMode mode, String tableName, String destination) {
        super(mode, tableName, destination);
        if (destination == null) {
            this.bucket = null;
            return;
        }
        if (!destination.startsWith("s3://")) {
            throw new RuntimeException("Publish destinations must be AWS URIs.");
        }
        this.bucket = AwsUtil.getBucket(destination);
    }

    public String getBucket() {
        return this.bucket;
    }

    public String getBlobRoot() {
        return blobRoot;
    }


    @Override
    public void publish(Table table) {
        if (!tableName.equals(table.getName())) table.setName(tableName);
        String tableInfoJson = format(table);
        String tableInfoPage = this.blobRoot + "/info";

        final AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
        s3Client.putObject(this.bucket, tableInfoPage, tableInfoJson);
    }

    @Override
    public void publish(ListTableResponse table) {
        String tableListJson = format(table);
        String tableListPage = "tables";

        final AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
        s3Client.putObject(this.bucket, tableListPage, tableListJson);
    }

    @Override
    public void publish(TableData tableData, int pageNum) {
        if (this.blobRoot == null) return;
        TableData modifiedData = new TableData();

        modifiedData.setDataModel(tableData.getDataModel());
        modifiedData.setData(tableData.getData());
        modifiedData.setPagination(getAbsolutePagination(tableData.getPagination(), pageNum));
        String dataJson = format(modifiedData);
        String dataPage = this.blobRoot + "/data" + (pageNum > 0 ? "." + pageNum : "");

        final AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
        s3Client.putObject(this.bucket, dataPage, dataJson);
    }


    @Override
    public String getObjectRoot(String awsUrl) {
        return AwsUtil.getObjectRoot(awsUrl);
    }
}