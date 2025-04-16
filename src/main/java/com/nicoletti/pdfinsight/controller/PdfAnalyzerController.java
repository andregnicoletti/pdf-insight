package com.nicoletti.pdfinsight.controller;

import com.nicoletti.pdfinsight.service.OpenAiService;
import com.nicoletti.pdfinsight.service.PdfAnalyzerService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/pdf")
@RequiredArgsConstructor
public class PdfAnalyzerController {

    private final PdfAnalyzerService service;

    @PostMapping("/upload")
    public ResponseEntity<byte[]> analyzePdf(@RequestPart("files") MultipartFile[] files) {
        try {
            ByteArrayInputStream csvStream = service.extractAndGenerateCsv(files);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"transacoes.csv\"")
                    .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                    .body(csvStream.readAllBytes());

        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .body(("Erro ao processar PDF: " + e.getMessage()).getBytes());
        }
    }

}
