package com.baumexpert.brennholzdemo.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

import java.net.URI;

/**
 * S3-Client für storage.type=s3 (ADR-0003).
 * Cloud: Region + Standard-Zugangsdaten (IAM-Rolle/Umgebung). LocalStack: Endpoint, Path-Style und Test-Zugangsdaten.
 */
@Configuration
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class S3StorageConfig {

    private static final Logger log = LoggerFactory.getLogger(S3StorageConfig.class);

    @Bean
    public S3Client s3Client(@Value("${aws.s3.region}") String region,
                             @Value("${aws.s3.endpoint:}") String endpoint,
                             @Value("${aws.s3.path-style:false}") boolean pathStyle,
                             @Value("${aws.s3.access-key:}") String accessKey,
                             @Value("${aws.s3.secret-key:}") String secretKey) {
        AwsCredentialsProvider credentials = accessKey.isBlank()
                ? DefaultCredentialsProvider.builder().build()
                : StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));

        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentials)
                .forcePathStyle(pathStyle);
        if (!endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }
        return builder.build();
    }

    /** Legt den Bucket an, falls er fehlt – nur für LocalStack gedacht (aws.s3.create-bucket=true). */
    @Bean
    @ConditionalOnProperty(name = "aws.s3.create-bucket", havingValue = "true")
    public ApplicationRunner createBucketIfMissing(S3Client s3Client, @Value("${aws.s3.bucket-name}") String bucketName) {
        return args -> {
            try {
                s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            } catch (NoSuchBucketException e) {
                s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
                log.info("S3-Bucket {} angelegt", bucketName);
            }
        };
    }
}
