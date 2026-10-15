package com.baumexpert.brennholzdemo.company.profile.infrastructure.image;

import com.baumexpert.brennholzdemo.company.profile.application.outbound.LogoScaler;
import com.baumexpert.brennholzdemo.company.profile.domain.LogoNotReadableException;
import com.baumexpert.brennholzdemo.storage.StoredFile;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Verkleinert Logos mit Thumbnailator. Das Bild wird neu kodiert – dadurch fallen Metadaten weg.
 */
@Component
public class ThumbnailatorLogoScaler implements LogoScaler {

    static final int MAX_EDGE = 400;
    private static final float JPEG_QUALITY = 0.85f;

    @Override
    public StoredFile scale(StoredFile original) {
        boolean png = "image/png".equals(original.contentType());
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(original.content()));
            if (image == null) {
                throw new LogoNotReadableException("Logo ist kein lesbares Bild");
            }
            // Nur verkleinern, nie vergrößern
            double factor = Math.min(1.0, (double) MAX_EDGE / Math.max(image.getWidth(), image.getHeight()));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Thumbnails.Builder<BufferedImage> builder = Thumbnails.of(image)
                    .scale(factor)
                    .outputFormat(png ? "png" : "jpg");
            if (!png) {
                builder.outputQuality(JPEG_QUALITY);
            }
            builder.toOutputStream(out);
            return new StoredFile(out.toByteArray(), png ? "image/png" : "image/jpeg");
        } catch (IOException e) {
            throw new LogoNotReadableException("Logo konnte nicht verarbeitet werden", e);
        }
    }
}
