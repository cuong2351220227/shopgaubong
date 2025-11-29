package com.example.shopgaubong;

import com.example.shopgaubong.util.HibernateUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 500);
        stage.setTitle("UCOP - Đăng Nhập");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        // Đóng EntityManagerFactory khi ứng dụng thoát
        HibernateUtil.shutdown();
    }
}

