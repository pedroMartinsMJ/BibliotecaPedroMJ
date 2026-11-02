package com.pedroMartinsMJ.bibliotecaPedroMJ.services;

import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.FalhaDeArmazenamentoException;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {

    private static final String SEM_EXTENSAO = "";

    private final MinioClient minioClient;
    private final AtomicBoolean bucketConferido = new AtomicBoolean(false);

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.url-expiracao-minutos:15}")
    private int expiracaoMinutos;

    /**
     * Cria o bucket se ele ainda não existe. Chamado sob demanda antes do primeiro
     * upload, e não na inicialização da aplicação: o {@code @SpringBootTest} sobe o
     * {@link MinioClient} real, e uma checagem de rede no boot faria os testes (e
     * qualquer subida com o MinIO em manutenção) falharem.
     */
    public void inicializarBucket() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                log.info("Bucket '{}' criado com sucesso!", bucketName);
            }
        } catch (Exception e) {
            throw new FalhaDeArmazenamentoException("Falha ao preparar o bucket '" + bucketName + "'", e);
        }
    }

    /**
     * Envia o arquivo com content type já conferido pelo conteúdo (não pelo que o
     * cliente declarou), o que evita servir script disfarçado de imagem.
     */
    public String uploadArquivo(MultipartFile file, String prefixo, String contentType) {
        garantirBucket();
        String fileName = gerarNomeArquivo(file.getOriginalFilename(), prefixo, contentType);
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );

            log.info("Arquivo '{}' enviado para MinIO com sucesso!", fileName);
            return fileName;

        } catch (Exception e) {
            throw new FalhaDeArmazenamentoException("Falha ao enviar arquivo para o storage", e);
        }
    }

    public InputStream downloadArquivo(String fileKey) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileKey)
                            .build()
            );
        } catch (Exception e) {
            throw new FalhaDeArmazenamentoException("Falha ao baixar arquivo do storage", e);
        }
    }

    public void deletarArquivo(String fileKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileKey)
                            .build()
            );
            log.info("Arquivo '{}' deletado do MinIO!", fileKey);
        } catch (Exception e) {
            throw new FalhaDeArmazenamentoException("Falha ao deletar arquivo do storage", e);
        }
    }

    public String gerarUrlDownload(String fileKey) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(fileKey)
                            .expiry(expiracaoMinutos, TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            throw new FalhaDeArmazenamentoException("Falha ao gerar link de download", e);
        }
    }

    private void garantirBucket() {
        if (bucketConferido.compareAndSet(false, true)) {
            try {
                inicializarBucket();
            } catch (RuntimeException e) {
                // Sem o latch liberado a próxima tentativa tenta de novo, em vez de
                // ficar com o storage quebrado até reiniciar a aplicação.
                bucketConferido.set(false);
                throw e;
            }
        }
    }

    /**
     * Nome de objeto independente do que o cliente enviou: originalFilename pode
     * vir nulo, vazio ou com caminho de outro sistema, e não é usado para nada
     * além da extensão.
     */
    private String gerarNomeArquivo(String nomeOriginal, String prefixo, String contentType) {
        return prefixo + "/" + LocalDate.now() + "/" + UUID.randomUUID() + extensao(nomeOriginal, contentType);
    }

    private String extensao(String nomeOriginal, String contentType) {
        String porConteudo = switch (contentType) {
            case "application/pdf" -> ".pdf";
            case "application/epub+zip" -> ".epub";
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> SEM_EXTENSAO;
        };
        return porConteudo;
    }
}
