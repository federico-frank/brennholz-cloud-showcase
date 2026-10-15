package com.baumexpert.brennholzdemo.company.profile.infrastructure.image;

import com.baumexpert.brennholzdemo.company.profile.domain.LogoNotReadableException;
import com.baumexpert.brennholzdemo.storage.StoredFile;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * F-043 S7: Logos werden auf höchstens 400 × 400 px verkleinert, nie vergrößert, Format bleibt erhalten.
 */
class ThumbnailatorLogoScalerTest {

    private final ThumbnailatorLogoScaler scaler = new ThumbnailatorLogoScaler();

    @Test
    void grossesJpgWirdSeitenverhaeltnisTreuVerkleinert() throws IOException {
        StoredFile scaled = scaler.scale(image(1200, 600, "jpg", "image/jpeg"));

        BufferedImage result = read(scaled);
        assertThat(result.getWidth()).isEqualTo(400);
        assertThat(result.getHeight()).isEqualTo(200);
        assertThat(scaled.contentType()).isEqualTo("image/jpeg");
    }

    @Test
    void kleinesPngBleibtGleichGrossUndPng() throws IOException {
        StoredFile scaled = scaler.scale(image(120, 80, "png", "image/png"));

        BufferedImage result = read(scaled);
        assertThat(result.getWidth()).isEqualTo(120);
        assertThat(result.getHeight()).isEqualTo(80);
        assertThat(scaled.contentType()).isEqualTo("image/png");
    }

    @Test
    void keinBildWirdAlsUnlesbarAbgelehnt() {
        StoredFile garbage = new StoredFile("kein bild".getBytes(), "image/png");

        assertThatThrownBy(() -> scaler.scale(garbage)).isInstanceOf(LogoNotReadableException.class);
    }

    private static StoredFile image(int width, int height, String format, String contentType) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, format, out);
        return new StoredFile(out.toByteArray(), contentType);
    }

    private static BufferedImage read(StoredFile file) throws IOException {
        return ImageIO.read(new ByteArrayInputStream(file.content()));
    }
}
