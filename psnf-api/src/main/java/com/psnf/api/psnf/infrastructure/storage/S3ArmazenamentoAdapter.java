package com.psnf.api.psnf.infrastructure.storage;

import com.psnf.api.psnf.domain.arquivo.model.ConteudoArquivo;
import com.psnf.api.psnf.domain.arquivo.port.out.ArmazenamentoArquivoPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.Optional;

/**
 * Armazena os arquivos num bucket S3 (MinIO em desenvolvimento, AWS S3 em produção).
 * O bucket é privado: os arquivos são servidos pela própria API (GET /arquivos/{nome}).
 * O bucket é criado sob demanda, assim a API sobe mesmo com o storage fora do ar.
 */
@Component
public class S3ArmazenamentoAdapter implements ArmazenamentoArquivoPort {

    private static final Logger logger = LoggerFactory.getLogger(S3ArmazenamentoAdapter.class);

    private final S3Client s3;
    private final String bucket;
    private volatile boolean bucketVerificado = false;

    public S3ArmazenamentoAdapter(S3Client s3, StorageProperties props) {
        this.s3 = s3;
        this.bucket = props.bucket();
    }

    @Override
    public void salvar(String nome, String contentType, byte[] conteudo) {
        garantirBucket();
        s3.putObject(b -> b.bucket(bucket).key(nome).contentType(contentType), RequestBody.fromBytes(conteudo));
    }

    @Override
    public Optional<ConteudoArquivo> carregar(String nome) {
        try {
            ResponseInputStream<GetObjectResponse> objeto = s3.getObject(b -> b.bucket(bucket).key(nome));
            GetObjectResponse r = objeto.response();
            return Optional.of(new ConteudoArquivo(nome, r.contentType(), r.contentLength(), objeto));
        } catch (NoSuchKeyException | NoSuchBucketException e) {
            return Optional.empty();
        }
    }

    private void garantirBucket() {
        if (bucketVerificado) return;
        synchronized (this) {
            if (bucketVerificado) return;
            try {
                s3.headBucket(b -> b.bucket(bucket));
            } catch (NoSuchBucketException e) {
                criarBucket();
            } catch (S3Exception e) {
                if (e.statusCode() != 404) throw e;
                criarBucket();
            }
            bucketVerificado = true;
        }
    }

    private void criarBucket() {
        logger.info("Criando bucket '{}' no storage", bucket);
        s3.createBucket(b -> b.bucket(bucket));
    }
}
