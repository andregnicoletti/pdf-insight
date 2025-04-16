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
@RequestMapping("/analyze")
@RequiredArgsConstructor
public class PdfAnalyzerController {

    private final PdfAnalyzerService service;

    private final OpenAiService openAiService;

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

    @PostMapping(value = "/upload")
    public ResponseEntity<Resource> uploadPdf(@RequestParam("file") MultipartFile file) throws IOException {
        System.out.println("Chegou aqui ✔️");

        // 1. Extrai texto do PDF
        String textoExtraido = this.service.extractText(file);

        // 2. Chama a OpenAI
        String csv = openAiService.gerarRespostaIA(textoExtraido);

        // 3. Constrói o CSV como recurso para download
        byte[] csvBytes = csv.getBytes(StandardCharsets.UTF_8);
        ByteArrayResource resource = new ByteArrayResource(csvBytes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=extrato.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }


}
