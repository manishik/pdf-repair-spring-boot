package com.example.pdfrepair.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class PdfRepairService {

    public File repairPdf(MultipartFile multipartFile) {
        try {
            File inputPdf = convertMultipartFileToFile(multipartFile);
            log.info("Inside repairPdf()");
            log.info("Input PDF Absolute Path = {}", inputPdf.getAbsolutePath());
            File tempPdfFile = createTempPdfFromExisting(multipartFile);
            log.info("Repaired PDF Absolute Path = {}", tempPdfFile.getAbsolutePath());

            PDDocument pdDocumentInput = PDDocument.load(inputPdf);
            PDDocument pdDocumentOutput = new PDDocument();
            PDFRenderer renderer = new PDFRenderer(pdDocumentInput);

            for (int i = 0; i < pdDocumentInput.getNumberOfPages(); i++) {
                BufferedImage bim = renderer.renderImageWithDPI(i, 300);
                PDPage page = new PDPage(new PDRectangle(bim.getWidth(), bim.getHeight()));
                pdDocumentOutput.addPage(page);
                PDPageContentStream contentStream = new PDPageContentStream(pdDocumentOutput, page);
                contentStream.drawImage(JPEGFactory.createFromImage(pdDocumentOutput, bim, 0.9f), 0, 0);
                contentStream.close();
            }
            pdDocumentOutput.save(tempPdfFile);
            pdDocumentOutput.close();
            return tempPdfFile;
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    @Async("pdfExecutor")
    public CompletableFuture<byte[]> repairPdfAsync(MultipartFile multipartFile) throws IOException {
        File repairedFile = repairPdf(multipartFile);
        byte[] fileBytes = Files.readAllBytes(repairedFile.toPath());
        return CompletableFuture.completedFuture(fileBytes);
    }


    private File convertMultipartFileToFile(MultipartFile multipartFile) {
        File convFile = new File(multipartFile.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(multipartFile.getBytes());
        } catch (FileNotFoundException fileNotFoundException) {
            throw new RuntimeException(fileNotFoundException);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
        return convFile;
    }

    public File createTempPdfFromExisting(MultipartFile multipartFile) throws IOException {
        String resourcesPath = System.getProperty("user.dir") + "/target";
        File targetDir = new File(resourcesPath);
        File tempFile = new File(targetDir, multipartFile.getOriginalFilename());
        multipartFile.transferTo(tempFile);
        tempFile.deleteOnExit();
        return tempFile;
    }
}
