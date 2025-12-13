package com.example.shopgaubong.util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;

/**
 * Utility class để xử lý chuyển đổi ảnh sang/từ Base64
 */
public class ImageUtil {

    /**
     * Convert File ảnh thành chuỗi Base64
     */
    public static String fileToBase64(File imageFile) throws IOException {
        if (imageFile == null || !imageFile.exists()) {
            return null;
        }

        try (FileInputStream fis = new FileInputStream(imageFile);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            
            byte[] imageBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        }
    }

    /**
     * Convert chuỗi Base64 thành JavaFX Image
     */
    public static Image base64ToImage(String base64String) {
        if (base64String == null || base64String.trim().isEmpty()) {
            return null;
        }

        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64String);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            return new Image(bis);
        } catch (Exception e) {
            System.err.println("Error converting Base64 to Image: " + e.getMessage());
            return null;
        }
    }

    /**
     * Resize ảnh nếu quá lớn (để tiết kiệm dung lượng database)
     */
    public static String resizeAndConvertToBase64(File imageFile, int maxWidth, int maxHeight) throws IOException {
        if (imageFile == null || !imageFile.exists()) {
            return null;
        }

        BufferedImage originalImage = ImageIO.read(imageFile);
        if (originalImage == null) {
            throw new IOException("Không thể đọc file ảnh");
        }

        // Calculate new dimensions maintaining aspect ratio
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);
        
        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);

        // Only resize if image is larger than max dimensions
        if (ratio < 1.0) {
            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = resizedImage.createGraphics();
            g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            g.dispose();

            // Convert resized image to Base64
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ImageIO.write(resizedImage, "jpg", baos);
                byte[] imageBytes = baos.toByteArray();
                return Base64.getEncoder().encodeToString(imageBytes);
            }
        } else {
            // Image is already small enough, use original
            return fileToBase64(imageFile);
        }
    }

    /**
     * Kiểm tra file có phải là ảnh hợp lệ không
     */
    public static boolean isValidImageFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }

        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".jpg") || 
               fileName.endsWith(".jpeg") || 
               fileName.endsWith(".png") || 
               fileName.endsWith(".gif") || 
               fileName.endsWith(".bmp");
    }

    /**
     * Lấy kích thước file theo MB
     */
    public static double getFileSizeMB(File file) {
        if (file == null || !file.exists()) {
            return 0;
        }
        return file.length() / (1024.0 * 1024.0);
    }

    /**
     * Load ảnh vào ImageView với kích thước cố định
     */
    public static void loadImageToView(String base64String, ImageView imageView, double width, double height) {
        Image image = base64ToImage(base64String);
        if (image != null) {
            imageView.setImage(image);
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
        } else {
            imageView.setImage(null);
        }
    }
}
