package com.example.pdfrepair;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PdfRepairApplication {

    public static void main(String[] args) {
        SpringApplication.run(PdfRepairApplication.class, args);
    }
}
