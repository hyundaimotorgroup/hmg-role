package com.hmg.role.common.config.aws;

import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@Slf4j
@Configuration
@ConditionalOnExpression("T(java.lang.Boolean).valueOf('${cdc.enabled:false}')")
public class AwsConfig {
    @Value("${cdc.s3.urlOverride:#{null}}")
    private String urlOverride;

    @Value("${cdc.s3.accessKey}")
    private String accessKey;

    @Value("${cdc.s3.secretKey}")
    private String secretKey;

    @Value("${cdc.s3.region:ap-northeast-2}")
    private String region;

    @Value("${cdc.s3.bucket}")
    private String bucketName;

    @Bean
    public S3Client s3Client() {
        log.info("Running S3 client from: {}", AwsConfig.class.getCanonicalName());
        S3ClientBuilder s3ClientBuilder =
                S3Client.builder()
                        .credentialsProvider(getWriterCredentialsProvider())
                        .region(Region.of(region));

        if (urlOverride != null && !urlOverride.isEmpty()) {
            s3ClientBuilder =
                    s3ClientBuilder.forcePathStyle(true).endpointOverride(URI.create(urlOverride));
        }

        return s3ClientBuilder.build();
    }

    private StaticCredentialsProvider getWriterCredentialsProvider() {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
    }
}
