package com.nicoletti.pdfinsight.service;

import com.nicoletti.pdfinsight.model.TransacaoDTO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfAnalyzerService {

    public String extractText(MultipartFile file) throws IOException {
        InputStream inputStream = file.getInputStream();
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    public ByteArrayInputStream extractAndGenerateCsv(MultipartFile file) throws IOException {
        byte[] pdfBytes = file.getInputStream().readAllBytes();

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            List<TransacaoDTO> transacoes = extractTransacoes(text);
            return gerarCsv(transacoes);
        }
    }

    public List<TransacaoDTO> extractTransacoes(String textoPdf) {
        List<TransacaoDTO> transacoes = new ArrayList<>();

        String normalizado = textoPdf.replaceAll("\\n", " ");

        transacoes.addAll(extrairBuy(normalizado));
        transacoes.addAll(extrairSell(normalizado));
        transacoes.addAll(extrairRegInt(normalizado));
        transacoes.addAll(extrairCustodia(normalizado));
        transacoes.addAll(extrairDiv(normalizado));
        transacoes.addAll(extrairIncomingWire(normalizado));

        return transacoes;
    }

    private List<TransacaoDTO> extrairBuy(String texto) {
        List<TransacaoDTO> lista = new ArrayList<>();

        Pattern pattern = Pattern.compile(
                "(\\d{2}/\\d{2}/\\d{4})\\s+BUY.*?QTY\\s+(\\d+(?:\\.\\d+)?)\\s+PRICE\\s+(\\d+(?:\\.\\d+)?).*?(\\d{1,3}(?:,\\d{3})*\\.\\d{2})"
        );
        Matcher matcher = pattern.matcher(texto);

        while (matcher.find()) {
            String data = formatarData(matcher.group(1));
            String qtd = matcher.group(2);
            String preco = matcher.group(3);
            String valor = matcher.group(4).replace(",", "");

            String historico = String.format("BUY QTY %s PRICE %s", qtd, preco);
            lista.add(new TransacaoDTO(data, historico, new BigDecimal(valor)));
        }

        return lista;
    }

    private List<TransacaoDTO> extrairSell(String texto) {
        List<TransacaoDTO> lista = new ArrayList<>();

        Pattern pattern = Pattern.compile(
                "(\\d{2}/\\d{2}/\\d{4})\\s+SELL.*?QTY\\s+-?(\\d+(?:\\.\\d+)?)\\s+PRICE\\s+(\\d+(?:\\.\\d+)?).*?(\\d{1,3}(?:,\\d{3})*\\.\\d{2})"
        );
        Matcher matcher = pattern.matcher(texto);

        while (matcher.find()) {
            String data = formatarData(matcher.group(1));
            String qtd = matcher.group(2);
            String preco = matcher.group(3);
            String valor = matcher.group(4).replace(",", "");

            String historico = String.format("SELL QTY %s PRICE %s", qtd, preco);
            lista.add(new TransacaoDTO(data, historico, new BigDecimal(valor)));
        }

        return lista;
    }

    private List<TransacaoDTO> extrairRegInt(String texto) {
        List<TransacaoDTO> lista = new ArrayList<>();

        Pattern pattern = Pattern.compile(
                "(\\d{2}/\\d{2}/\\d{4})[^$%]{0,100}?(\\d+(?:\\.\\d+))%?.*?\\$?(\\d{1,3}(?:,\\d{3})*\\.\\d{2})",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(texto);

        while (matcher.find()) {
            String trecho = matcher.group(0).toUpperCase();

            if (trecho.contains("REG") || trecho.contains("INTEREST") || trecho.contains("COUPON")) {
                String data = formatarData(matcher.group(1));
                String taxa = matcher.group(2);
                String valor = matcher.group(3).replace(",", "");

                BigDecimal valorDecimal = new BigDecimal(valor);

                // Ignorar valores muito baixos (ex: 3.25), que não representam pagamento de juros
                if (valorDecimal.compareTo(new BigDecimal("10")) < 0) {
                    continue;
                }

                String historico = "REG INT - " + taxa + "%";

                lista.add(new TransacaoDTO(data, historico, valorDecimal));
            }
        }

        return lista;
    }


    private List<TransacaoDTO> extrairCustodia(String texto) {
        List<TransacaoDTO> lista = new ArrayList<>();

        String normalizado = texto.replaceAll("\\n", " ");

        Pattern pattern = Pattern.compile(
                "(\\d{2}/\\d{2}/\\d{4}).*?CUSTOD.*?\\$?(\\d{1,3}(?:,\\d{3})*\\.\\d{2})",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(normalizado);

        while (matcher.find()) {
            String data = formatarData(matcher.group(1));
            String valor = matcher.group(2).replace(",", "");
            String historico = "CUSTOD FI - Taxa de custódia";

            lista.add(new TransacaoDTO(data, historico, new BigDecimal(valor)));
        }

        return lista;
    }


    private List<TransacaoDTO> extrairDiv(String texto) {
        List<TransacaoDTO> lista = new ArrayList<>();

        Pattern pattern = Pattern.compile(
                "(\\d{2}/\\d{2}/\\d{4}).*?([A-Z0-9\\s]{5,}?)\\s+CASH DIV.*?(\\d{1,3}(?:,\\d{3})*\\.\\d{2})",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(texto);

        while (matcher.find()) {
            String data = formatarData(matcher.group(1));
            String nome = matcher.group(2).trim();
            String valor = matcher.group(3).replace(",", "");

            String historico = "DIV " + nome;
            lista.add(new TransacaoDTO(data, historico, new BigDecimal(valor)));
        }

        return lista;
    }

    private List<TransacaoDTO> extrairIncomingWire(String texto) {
        List<TransacaoDTO> lista = new ArrayList<>();

        Pattern pattern = Pattern.compile(
                "(\\d{2}/\\d{2}/\\d{4}).*?Incoming Wire Transfer\\s+([A-Z ]+?)\\s+\\$?(\\d{1,3}(?:,\\d{3})*\\.\\d{2})",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(texto);

        while (matcher.find()) {
            String data = formatarData(matcher.group(1));
            String nome = matcher.group(2).trim();
            String valor = matcher.group(3).replace(",", "");

            String historico = "INCOMING WIRE TRANSFER " + nome;
            lista.add(new TransacaoDTO(data, historico, new BigDecimal(valor)));
        }

        return lista;
    }

    private ByteArrayInputStream gerarCsv(List<TransacaoDTO> transacoes) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        writer.println("DATA;HISTORICO;VALOR");
        for (TransacaoDTO t : transacoes) {
            writer.printf("%s;%s;%.2f%n", t.getData(), t.getHistorico(), t.getValor());
        }

        writer.flush();
        return new ByteArrayInputStream(out.toByteArray());
    }

    private String formatarData(String dataAmericana) {
        try {
            LocalDate date = LocalDate.parse(dataAmericana, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException e) {
            return dataAmericana; // fallback caso dê ruim
        }
    }

}
