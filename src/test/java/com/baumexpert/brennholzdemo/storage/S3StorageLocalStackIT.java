package com.baumexpert.brennholzdemo.storage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.HttpURLConnection;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * F-040 S7: Echter Durchlauf gegen LocalStack (docker compose up -d localstack).
 * Wird übersprungen, wenn LocalStack nicht läuft.
 */
class S3StorageLocalStackIT {

    private static final String ENDPOINT = "http://localhost:4566";
    private static final String BUCKET = "local-company-images";

    @BeforeAll
    static void localStackLaeuft() {
        boolean up;
        try {
            HttpURLConnection connection = (HttpURLConnection) URI.create(ENDPOINT + "/_localstack/health").toURL().openConnection();
            connection.setConnectTimeout(1000);
            up = connection.getResponseCode() == 200;
        } catch (Exception e) {
            up = false;
        }
        assumeTrue(up, "LocalStack läuft nicht – Test übersprungen");
    }

    @Test
    void hochladenLadenLoeschen() throws Exception {
        S3StorageConfig config = new S3StorageConfig();
        S3Client client = config.s3Client("eu-central-1", ENDPOINT, true, "test", "test");
        config.createBucketIfMissing(client, BUCKET).run(null);
        S3StorageServiceImpl storage = new S3StorageServiceImpl(client, BUCKET);

        String key = storage.uploadFile(new MockMultipartFile("file", "logo.png", "image/png", new byte[]{7, 8, 9}));

        StoredFile file = storage.load(key).orElseThrow();
        assertThat(file.content()).containsExactly(7, 8, 9);
        assertThat(file.contentType()).isEqualTo("image/png");

        storage.deleteFile(key);
        assertThat(storage.load(key)).isEmpty();
    }
}
