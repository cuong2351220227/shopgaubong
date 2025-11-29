package com.example.shopgaubong;

import com.example.shopgaubong.util.DatabaseInitializer;
import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        // Khởi tạo dữ liệu mẫu khi ứng dụng chạy lần đầu
        DatabaseInitializer.initializeSampleData();

        // Khởi chạy ứng dụng JavaFX
        Application.launch(HelloApplication.class, args);
    }
}


