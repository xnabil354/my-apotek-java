package com.example.My.Apotek.config;

import com.example.My.Apotek.model.User;
import com.example.My.Apotek.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin123");
            admin.setRole("ADMIN_APOTEK");
            admin.setNamaLengkap("Admin Apotek");
            userRepository.save(admin);

            User kepala = new User();
            kepala.setUsername("kepala");
            kepala.setPassword("kepala123");
            kepala.setRole("KEPALA_APOTEK");
            kepala.setNamaLengkap("Kepala Apotek");
            userRepository.save(kepala);
        }
    }
}
