package com.baumexpert.brennholzdemo.storage;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.ServerSideEncryption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * F-040 S7: S3-Adapter lädt verschlüsselt hoch, lädt und löscht über den S3Client.
 */
class S3StorageServiceImplTest {

    private final S3Client s3 = mock(S3Client.class);
    private final S3StorageServiceImpl storage = new S3StorageServiceImpl(s3, "bilder-bucket");

    @Test
    void laedtMitAesVerschluesselungUndContentTypeHoch() {
        String key = storage.uploadFile(new MockMultipartFile("file", "logo.jpg", "image/jpeg", new byte[]{1}));

        ArgumentCaptor<PutObjectRequest> request = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3).putObject(request.capture(), any(RequestBody.class));
        assertThat(key).matches("[0-9a-f-]{36}\\.jpg");
        assertThat(request.getValue().bucket()).isEqualTo("bilder-bucket");
        assertThat(request.getValue().key()).isEqualTo(key);
        assertThat(request.getValue().contentType()).isEqualTo("image/jpeg");
        assertThat(request.getValue().serverSideEncryption()).isEqualTo(ServerSideEncryption.AES256);
    }

    @Test
    void laedtBildAusDemBucket() {
        GetObjectResponse response = GetObjectResponse.builder().contentType("image/png").build();
        when(s3.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenReturn(ResponseBytes.fromByteArray(response, new byte[]{4, 5}));

        StoredFile file = storage.load("a.png").orElseThrow();

        assertThat(file.content()).containsExactly(4, 5);
        assertThat(file.contentType()).isEqualTo("image/png");
    }

    @Test
    void fehlendesBildIstLeer() {
        when(s3.getObjectAsBytes(any(GetObjectRequest.class))).thenThrow(NoSuchKeyException.builder().build());

        assertThat(storage.load("fehlt.png")).isEmpty();
    }

    @Test
    void loeschtImBucket() {
        storage.deleteFile("a.png");

        ArgumentCaptor<DeleteObjectRequest> request = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3).deleteObject(request.capture());
        assertThat(request.getValue().key()).isEqualTo("a.png");
    }
}
