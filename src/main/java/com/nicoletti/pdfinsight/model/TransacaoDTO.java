package com.nicoletti.pdfinsight.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransacaoDTO {

    private String data;
    private String historico;
    private BigDecimal valor;

}
