package com.example.My.Apotek;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MyApotekWebLauncher {

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true");
        SpringApplication.run(MyApotekWebLauncher.class, args);
        System.out.println("🌐 MyApotek Web Server is Running at http://localhost:8081");
    }
}
