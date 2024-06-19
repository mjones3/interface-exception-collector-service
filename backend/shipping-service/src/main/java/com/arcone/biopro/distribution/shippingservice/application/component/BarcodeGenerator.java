package com.arcone.biopro.distribution.shippingservice.application.component;

import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;

@Component
public class BarcodeGenerator {

    public static String generateCode128BarcodeBase64(String barcodeText) {
        return imgToBase64String(generateCode128BarcodeImage(barcodeText), "png");
    }

    private static BufferedImage generateCode128BarcodeImage(String barcodeText) {
        var barcodeGenerator = new Code128Bean();
        var canvas = new BitmapCanvasProvider(130, BufferedImage.TYPE_BYTE_BINARY, false, 0);
        barcodeGenerator.generateBarcode(canvas, barcodeText);
        return canvas.getBufferedImage();
    }

    private static String imgToBase64String(final BufferedImage img, final String formatName) {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        try {
            ImageIO.write(img, formatName, os);
            return Base64.getEncoder().encodeToString(os.toByteArray());
        } catch (final IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }
}
