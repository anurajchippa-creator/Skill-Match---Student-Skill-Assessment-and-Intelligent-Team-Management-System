package com.skillmatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SkillMatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(SkillMatchApplication.class, args);
        System.out.println("========================================");
        System.out.println("  SkillMatch Backend is running on :8080");
        System.out.println("========================================");
    }
}
