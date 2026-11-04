package com.psnf.api.psnf.infrastructure.storage;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class S3Config {

    @Bean(destroyMethod = "close")
    public S3Client s3Client(StorageProperties props) {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(props.region()))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(props.pathStyle()).build())
                // checksums só quando o serviço exige: maior compatibilidade com storages S3-compatíveis
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED);

        if (props.temEndpoint()) {
            builder.endpointOverride(URI.create(props.endpoint()));
        }
        builder.credentialsProvider(props.temCredenciais()
                ? StaticCredentialsProvider.create(AwsBasicCredentials.create(props.accessKey(), props.secretKey()))
                : DefaultCredentialsProvider.builder().build());

        return builder.build();
    }
}
