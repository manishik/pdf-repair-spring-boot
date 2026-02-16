package com.example.pdfrepair.controller;

import com.example.pdfrepair.service.PdfRepairService;
import dto.RepairRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

@RestController
@Slf4j
@RequestMapping("/pdf")
public class PdfRepairController {

    private final PdfRepairService pdfRepairService;

    public PdfRepairController(PdfRepairService pdfRepairService) {
        this.pdfRepairService = pdfRepairService;
    }

    @PostMapping(value = "/repair-sync", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InputStreamResource> repairSync(@ModelAttribute("file") RepairRequest repairRequest) throws IOException {
        log.info("Inside repairSync()");
        log.info("Repair Request: {}", repairRequest);
        File repaired = pdfRepairService.repairPdf(repairRequest.getFile());

        byte[] fileBytes = Files.readAllBytes(repaired.toPath());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=repaired.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(fileBytes.length)
                .body(new InputStreamResource(new ByteArrayInputStream(fileBytes)));
    }

    @PostMapping(value = "/repair-async", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InputStreamResource> repairAsync(
            @ModelAttribute("file") RepairRequest repairRequest) throws IOException {
        log.info("Inside repairAsync()");
        log.info("Repair Request: {}", repairRequest);
        MultipartFile multipartFile = repairRequest.getFile();
        CompletableFuture<byte[]> completableFuture = pdfRepairService.repairPdfAsync(repairRequest.getFile());
        return completableFuture.thenApply(bytes -> buildPdfResp(bytes, multipartFile.getOriginalFilename(),"-repaired.pdf")).join();
    }

    public ResponseEntity<InputStreamResource> buildPdfResp(byte[] fileBytes, String baseFileName, String suffix) {
        String headerValue = "attachment; filename=\"" + baseFileName + "." + suffix + "\"";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(fileBytes.length)
                .body(new InputStreamResource(new ByteArrayInputStream(fileBytes)));
    }
}
