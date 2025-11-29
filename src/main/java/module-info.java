module com.example.shopgaubong {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires jakarta.validation;
    requires org.hibernate.validator;
    requires com.opencsv;
    requires org.apache.poi.ooxml;
    requires jbcrypt;
    requires org.slf4j;
    requires java.naming;


    opens com.example.shopgaubong to javafx.fxml;
    opens com.example.shopgaubong.controller to javafx.fxml;

    opens com.example.shopgaubong.entity.base to org.hibernate.orm.core;
    opens com.example.shopgaubong.entity;

    exports com.example.shopgaubong;
    exports com.example.shopgaubong.controller;
    exports com.example.shopgaubong.entity;
    exports com.example.shopgaubong.enums;
    exports com.example.shopgaubong.service;
}