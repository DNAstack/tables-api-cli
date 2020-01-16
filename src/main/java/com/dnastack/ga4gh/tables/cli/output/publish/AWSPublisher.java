package com.dnastack.ga4gh.tables.cli.output.publish;


import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.dnastack.ga4gh.tables.cli.util.AwsUtil;
import com.dnastack.ga4gh.tables.cli.util.option.OutputOptions.OutputMode;


import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

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
        if (!tableName.equals(table.getName())) {
            table.setName(tableName);
        }
        String tableInfoJson = format(table);
        String tableInfoPage = this.blobRoot + "/info";

        Region region = Region.CA_CENTRAL_1;
        S3Client s3 = S3Client.builder().region(region).build();

        s3.putObject(PutObjectRequest.builder().bucket(this.bucket).key(tableInfoPage)
                .build(),RequestBody.fromBytes(tableInfoJson.getBytes()));
    }

    @Override
    public void publish(ListTableResponse table) {
        String tableListJson = format(table);
        String root = AwsUtil.getObjectRoot(destination);
        String tableListPage = root == null ?  "tables" : root + "/tables";
        Region region = Region.CA_CENTRAL_1;
        S3Client s3 = S3Client.builder().region(region).build();

        s3.putObject(PutObjectRequest.builder().bucket(this.bucket).key(tableListPage)
                        .build(),RequestBody.fromBytes(tableListJson.getBytes()));
    }

    @Override
    public void publish(TableData tableData, int pageNum) {

        if (this.blobRoot == null){
            return;
        }

        TableData modifiedData = new TableData();
        //Throws error when null, should it throw a specific exception message
        modifiedData.setDataModel(tableData.getDataModel());
        modifiedData.setData(tableData.getData());
        modifiedData.setPagination(getAbsolutePagination(tableData.getPagination(), pageNum));
        String datasetJson = format(modifiedData);

        String blobPage = this.blobRoot + "/data" + (pageNum > 0 ? "." + pageNum : "");

        Region region = Region.CA_CENTRAL_1;
        S3Client s3 = S3Client.builder().region(region).build();

        s3.putObject(PutObjectRequest.builder().bucket(this.bucket).key(blobPage)
                .build(),RequestBody.fromBytes(datasetJson.getBytes()));

        //TODO: Create blob ACL just for this user
    }


    @Override
    public String getObjectRoot(String awsUrl) {
        return AwsUtil.getObjectRoot(awsUrl);
    }
}