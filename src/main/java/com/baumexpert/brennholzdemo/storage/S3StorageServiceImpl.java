package com.baumexpert.brennholzdemo.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.ServerSideEncryption;

import java.io.IOException;
import java.util.Optional;

/**
 * Bildspeicher in S3 (storage.type=s3) – in der Cloud ein privater Bucket, lokal LocalStack.
 * Objekte werden serverseitig mit AES-256 verschlüsselt; ausgeliefert wird über die App (/bilder/{key}).
 */
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class S3StorageServiceImpl implements FileStorageService {

    private final S3Client s3Client;
    private final String bucketName;

    public S3StorageServiceImpl(S3Client s3Client, @Value("${aws.s3.bucket-name}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    public String uploadFile(MultipartFile file) {
        String key = ImageUploadPolicy.newKey(file);
        try {
            return put(key, file.getBytes(), file.getContentType());
        } catch (IOException e) {
            throw new StorageException("Fehler beim Hochladen der Datei zu S3", e);
        }
    }

    @Override
    public String store(byte[] content, String contentType) {
        return put(ImageUploadPolicy.newKey(contentType), content, contentType);
    }

    private String put(String key, byte[] content, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .serverSideEncryption(ServerSideEncryption.AES256)
                    .build();
            s3Client.putObject(request, RequestBody.fromBytes(content));
            return key;
        } catch (RuntimeException e) {
            throw new StorageException("Fehler beim Hochladen der Datei zu S3", e);
        }
    }

    @Override
    public void deleteFile(String fileKey) {
        if (!ImageUploadPolicy.isValidKey(fileKey)) {
            return;
        }
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(fileKey).build());
        } catch (RuntimeException e) {
            throw new StorageException("Fehler beim Löschen der Datei aus S3: " + fileKey, e);
        }
    }

    @Override
    public Optional<StoredFile> load(String fileKey) {
        if (!ImageUploadPolicy.isValidKey(fileKey)) {
            return Optional.empty();
        }
        try {
            ResponseBytes<GetObjectResponse> object = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder().bucket(bucketName).key(fileKey).build());
            String type = object.response().contentType();
            return Optional.of(new StoredFile(object.asByteArray(), type != null ? type : "application/octet-stream"));
        } catch (NoSuchKeyException e) {
            return Optional.empty();
        }
    }
}
