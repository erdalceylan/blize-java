package com.blize.service.files;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Path;

@Service
public class S3FileUploader implements FileUploader {

    @Value("${s3.endpoint}")
    private String endpoint;

    @Value("${s3.publicEndpoint}")
    private String publicEndpoint;

    @Value("${s3.bucketName}")
    private String bucketName;

    @Value("${s3.accessKey}")
    private String accessKey;

    @Value("${s3.secretKey}")
    private String secretKey;

    private S3Client s3Client;

    private S3Client getS3Client() {
        if (s3Client == null) {
            s3Client = S3Client.builder()
                    .region(Region.of("us-east-1"))
                    .endpointOverride(java.net.URI.create(endpoint)) // MinIO endpoint
                    .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                    .forcePathStyle(true) // MinIO important
                    .build();
            try {
                s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
            } catch (BucketAlreadyOwnedByYouException e) {
                System.out.println("Bucket already owned by you"+e.getMessage());
            }

        }

        return s3Client;
    }

    @Override
    public FileInfo upload(Path file, String toRootPath, String toPath, String toFileName) {

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(toRootPath + toPath + toFileName)
                .build();

        getS3Client().putObject(putObjectRequest, file);

        return new FileInfo(publicEndpoint+"/"+bucketName, toRootPath, toPath, toFileName, true);
    }
}