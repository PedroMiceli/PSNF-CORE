package com.psnf.api.psnf.infrastructure.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuração do object storage (prefixo psnf.storage no application.properties).
 *
 * MinIO (local): endpoint=http://localhost:9000, path-style=true, access/secret = usuário do MinIO.
 * AWS S3:       endpoint vazio, path-style=false, region da AWS e access/secret vazios para usar as
 *               credenciais padrão da AWS (variáveis de ambiente, ~/.aws, IAM role).
 */
@ConfigurationProperties(prefix = "psnf.storage")
public record StorageProperties(
        String endpoint,
        String region,
        String accessKey,
        String secretKey,
        String bucket,
        boolean pathStyle
) {
    public StorageProperties {
        if (region == null || region.isBlank()) region = "us-east-1";
        if (bucket == null || bucket.isBlank()) bucket = "psnf-arquivos";
    }

    boolean temEndpoint() { return endpoint != null && !endpoint.isBlank(); }

    boolean temCredenciais() { return accessKey != null && !accessKey.isBlank(); }
}
