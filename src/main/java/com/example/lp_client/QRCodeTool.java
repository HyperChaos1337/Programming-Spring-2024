package com.example.lp_client;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class QRCodeTool {
    public static void generateQRCode(String recordString, int id) throws WriterException, SQLException, IOException {

        String filename = "request_ID_" + String.valueOf(id) + ".png";
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            // Создаем Map для указания параметров кодирования
            Map<EncodeHintType, Object> hints = new HashMap<>();

            // Указываем кодировку UTF-8
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            // Создаем BitMatrix с указанием кодировки
            BitMatrix bitMatrix = qrCodeWriter.encode(recordString, BarcodeFormat.QR_CODE, 200, 200, hints);

            // Преобразуем BitMatrix в изображение
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            // Сохраняем изображение в файл
            File outputFile = new File(filename);
            javax.imageio.ImageIO.write(bufferedImage, "png", outputFile);

        } catch (WriterException | IOException e) {
            System.err.println("Ошибка при создании QR-кода: " + e.getMessage());
        }
    }
    public static String decodeQRCode(String imagePath) {
        try {

            Map<DecodeHintType, Object> hints = new HashMap<>();

            hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
            // Загружаем изображение
            File imageFile = new File(imagePath);
            BufferedImage bufferedImage = ImageIO.read(imageFile);

            // Создаем LuminanceSource из изображения
            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            // Создаем QR code reader
            // Декодируем QR-код
            QRCodeReader reader = new QRCodeReader();

            Result result = reader.decode(bitmap, hints);
            // Возвращаем декодированную строку
            return result.getText();

        } catch (IOException | NotFoundException e) {
            System.err.println("Ошибка при декодировании QR-кода: " + e.getMessage());
            return null;
        } catch (ChecksumException | FormatException e) {
            throw new RuntimeException(e);
        }
    }
}
