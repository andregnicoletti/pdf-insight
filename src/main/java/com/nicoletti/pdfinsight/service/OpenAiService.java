package com.nicoletti.pdfinsight.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    @Value("${openai.api.key}")
    private String API_KEY;

    private final RestTemplate restTemplate = new RestTemplate();

    public String gerarRespostaIA(String textoExtraido) {
        String prompt = """
            Você é um extrator inteligente de dados financeiros. Dado um extrato bancário, quero que você identifique e converta para formato CSV **apenas** as seguintes transações:
            BUY, SELL, REG INT, DIV, CUSTOD FI, INCOMING WIRE TRANSFER.

            A saída deve ser um CSV com as colunas:
            DATA;HISTORICO;VALOR

            Regras:
            1. O campo DATA deve estar no formato **dd/MM/yyyy**.
            2. O campo VALOR deve ser numérico, com **ponto como separador decimal** (ex: 1234.56).
            3. O campo HISTÓRICO deve incluir informações detalhadas como: tipo da transação, nome do ativo ou empresa, QTY, PRICE ou TAXA se disponíveis.
            4. Extraia transações de todas as páginas do PDF.
            5. Faça a ordenação por data, da menor para a maior.

            Texto do extrato:
            """ + textoExtraido;

//        Map<String, Object> requestBody = Map.of(
//                "model", "gpt-4-turbo",
//                "messages", List.of(Map.of("role", "user", "content", prompt)),
//                "temperature", 0.2
//        );

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-3.5-turbo", // <- troque aqui
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.2
        );


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(API_KEY);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(OPENAI_URL, entity, Map.class);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

        return (String) message.get("content");
    }
}
