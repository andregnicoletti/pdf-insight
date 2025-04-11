package com.nicoletti.pdfinsight.controller;

import com.nicoletti.pdfinsight.model.PdfResponseDto;
import com.nicoletti.pdfinsight.service.PdfAnalyzerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@RestController
@RequestMapping("/analyze")
@RequiredArgsConstructor
public class PdfAnalyzerController {

    private final PdfAnalyzerService service;

    @GetMapping("/health")
    public ResponseEntity<Boolean> status() {
        return ResponseEntity.ok(true);
    }

    @PostMapping
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
