package com.blize.service.files;


import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class ImageUploadService{

    public static final int CROP_64 = 64;
    public static final int CROP_128 = 128;
    public static final int CROP_256 = 256;
    public static final int CROP_512 = 512;
    public static final int CROP_1024 = 1024;

    public BufferedImage cropRatio(BufferedImage image, double ratio) throws IOException {
        int width = image.getWidth();
        int height = image.getHeight();

        double targetWidth = height * ratio;
        double targetHeight = height;

        if (targetWidth > width) {
            double scale = (double) width / targetWidth;
            targetWidth = width;
            targetHeight = targetHeight * scale;
        }

        return Thumbnails.of(image)
                .crop(Positions.CENTER)
                .size((int)targetWidth, (int) targetHeight)
                .asBufferedImage();
    }

    public BufferedImage resizeMin(BufferedImage image, int width, int height) throws IOException {
        int imgWidth = image.getWidth();
        int imgHeight = image.getHeight();

        if (imgWidth > width || imgHeight > height) {
            return Thumbnails.of(image)
                    .size(width, height)
                    .asBufferedImage();
        }

        return image;
    }

    public void checkSize(BufferedImage image, int minWidth, int minHeight) {
        if (image.getWidth() < minWidth || image.getHeight() < minHeight) {
            throw new ResponseStatusException(BAD_REQUEST, "Image dimensions are smaller than the required size.");
        }
    }

    public FileUploader.FileInfo save(
            BufferedImage image,
            String rootPath,
            String path,
            String fileName,
            FileUploader fileUploader) throws IOException {

        Path tempfile = Files.createTempFile("image", ".jpeg");

        Thumbnails.of(image)
                .size(image.getWidth(), image.getHeight())
                .outputFormat("jpeg")
                .toFile(tempfile.toAbsolutePath().toString());

        return fileUploader.upload(tempfile, rootPath, path, fileName);
    }
}
