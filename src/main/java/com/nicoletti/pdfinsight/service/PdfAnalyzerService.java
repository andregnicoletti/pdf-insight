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
import java.util.*;
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

    public ByteArrayInputStream extractAndGenerateCsv(MultipartFile[] files) throws IOException {
        List<TransacaoDTO> todasTransacoes = new ArrayList<>();
        for (MultipartFile file : files) {
            byte[] pdfBytes = file.getInputStream().readAllBytes();

            try (PDDocument document = Loader.loadPDF(pdfBytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);
                List<TransacaoDTO> transacoes = extractTransacoes(text);
                todasTransacoes.addAll(transacoes);
            }
        }

        todasTransacoes.sort(Comparator.comparing(t -> {
            try {
                return LocalDate.parse(t.getData(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (DateTimeParseException e) {
                return LocalDate.MIN;
            }
        }));

        return gerarCsv(todasTransacoes);
    }

    public List<TransacaoDTO> extractTransacoes(String textoPdf) {
        List<TransacaoDTO> transacoes = new ArrayList<>();

        String normalizado = textoPdf.replaceAll("\n", " ");

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
                "(\\d{2}/\\d{2}/\\d{4})\\s+BUY\\s+([A-Z0-9\\s\\.&-]+?)\\s+QTY\\s+(\\d+(?:\\.\\d+)?)\\s+PRICE\\s+(\\d+(?:\\.\\d+)?).*?(\\d{1,3}(?:,\\d{3})*\\.\\d{2})",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(texto);
        while (matcher.find()) {
            String data = formatarData(matcher.group(1));
            String emitente = matcher.group(2).trim();
            String qtd = matcher.group(3);
            String preco = matcher.group(4);
            String valor = matcher.group(5).replace(",", "");

            String historico = String.format("BUY QTY %s PRICE %s - %s", qtd, preco, emitente);
            lista.add(new TransacaoDTO(data, historico, new BigDecimal(valor)));
        }

        return lista;
    }

    private List<TransacaoDTO> extrairSell(String texto) {
        List<TransacaoDTO> lista = new ArrayList<>();

        Pattern pattern = Pattern.compile(
                "(\\d{2}/\\d{2}/\\d{4})\\s+SELL\\s+([A-Z0-9\\s\\.&-]+?)\\s+QTY\\s+-?(\\d+(?:\\.\\d+)?)\\s+PRICE\\s+(\\d+(?:\\.\\d+)?).*?(\\d{1,3}(?:,\\d{3})*\\.\\d{2})",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(texto);
        while (matcher.find()) {
            String data = formatarData(matcher.group(1));
            String emitente = matcher.group(2).trim();
            String qtd = matcher.group(3);
            String preco = matcher.group(4);
            String valor = matcher.group(5).replace(",", "");

            String historico = String.format("SELL QTY %s PRICE %s - %s", qtd, preco, emitente);
            lista.add(new TransacaoDTO(data, historico, new BigDecimal(valor)));
        }

        return lista;
    }

    private List<TransacaoDTO> extrairRegInt(String texto) {
        List<TransacaoDTO> lista = new ArrayList<>();

        Pattern pattern = Pattern.compile(
                "(\\d{2}/\\d{2}/\\d{4})\\s+(.*?)\\s+(\\d+(?:\\.\\d+))\\s+REG INT\\s+ON.*?\\$?(\\d{1,3}(?:,\\d{3})*\\.\\d{2})",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(texto);
        while (matcher.find()) {
            String data = formatarData(matcher.group(1));
            String emitente = matcher.group(2).replaceAll("\s+", " ").trim();
            String taxa = matcher.group(3);
            String valor = matcher.group(4).replace(",", "");

            BigDecimal valorDecimal = new BigDecimal(valor);
            if (valorDecimal.compareTo(new BigDecimal("10")) < 0) continue;

            // Reduzir excesso no nome do emitente se estiver muito longo
            if (emitente.length() > 40) {
                emitente = emitente.substring(0, 40).trim();
            }

            String historico = String.format("REG INT - %s%% - %s", taxa, emitente);
            lista.add(new TransacaoDTO(data, historico, valorDecimal));
        }

        return lista;
    }



    private List<TransacaoDTO> extrairCustodia(String texto) {
        List<TransacaoDTO> lista = new ArrayList<>();

        Pattern pattern = Pattern.compile(
                "(\\d{2}/\\d{2}/\\d{4}).*?CUSTOD.*?\\$?(\\d{1,3}(?:,\\d{3})*\\.\\d{2})",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(texto);
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
                "(\\d{2}/\\d{2}/\\d{4}).*?([A-Z0-9\\s\\.\\-&]{5,}?)\\s+CASH DIV.*?(\\d{1,3}(?:,\\d{3})*\\.\\d{2})",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(texto);
        while (matcher.find()) {
            String data = formatarData(matcher.group(1));
            String emitente = matcher.group(2).trim();
            String valor = matcher.group(3).replace(",", "");

            String historico = "DIV - " + emitente;
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

            String historico = "INCOMING WIRE TRANSFER - " + nome;
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
            return dataAmericana;
        }
    }
}
