package com.pedroMartinsMJ.bibliotecaPedroMJ.services;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

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
            log.error("Erro ao inicializar bucket: {}", e.getMessage());
            throw new RuntimeException("Falha ao inicializar MinIO", e);
        }
    }

    public String uploadArquivo(MultipartFile file, String prefixo) {
        try {
            // Gera nome único para o arquivo
            String fileName = gerarNomeArquivo(file.getOriginalFilename(), prefixo);

            // Faz o upload
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("Arquivo '{}' enviado para MinIO com sucesso!", fileName);
            return fileName;

        } catch (Exception e) {
            log.error("Erro ao fazer upload: {}", e.getMessage());
            throw new RuntimeException("Falha ao enviar arquivo para MinIO", e);
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
            log.error("Erro ao baixar arquivo '{}': {}", fileKey, e.getMessage());
            throw new RuntimeException("Falha ao baixar arquivo do MinIO", e);
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
            log.error("Erro ao deletar arquivo '{}': {}", fileKey, e.getMessage());
            throw new RuntimeException("Falha ao deletar arquivo do MinIO", e);
        }
    }

    public String gerarUrlDownload(String fileKey) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(fileKey)
                            .expiry(7, TimeUnit.DAYS)
                            .build()
            );
        } catch (Exception e) {
            log.error("Erro ao gerar URL de download: {}", e.getMessage());
            throw new RuntimeException("Falha ao gerar URL", e);
        }
    }

    private String gerarNomeArquivo(String nomeOriginal, String prefixo) {
        String extensao = nomeOriginal.substring(nomeOriginal.lastIndexOf("."));
        return prefixo + "/" + UUID.randomUUID() + extensao;
    }
}
