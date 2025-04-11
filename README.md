<h1 align="center">📄 PDF Insight</h1>
<p align="center">
  API inteligente para análise e extração de dados estruturados a partir de arquivos PDF. <br/>
  Feito com Java + Spring Boot ☕🚀
</p>


![Java](https://img.shields.io/badge/Java-21-blue?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3.4.4-brightgreen?style=flat-square&logo=spring-boot)
![PDFBox](https://img.shields.io/badge/PDFBox-3.0.4-red?style=flat-square&logo=adobeacrobatreader)

---

## 🚀 Funcionalidades

- Processa **vários PDFs de extratos bancários** ao mesmo tempo
- Extrai dados de transações como:
    - BUY / SELL
    - REG INT (juros de títulos)
    - DIV (dividendos)
    - INCOMING WIRE TRANSFER
    - CUSTOD FI (taxas de custódia)
- Gera um arquivo `.csv` consolidado com os dados extraídos
- Ordena as transações por **data (dd/MM/yyyy)**

---

## 📥 Como usar

### ▶️ Endpoint: `/analyze`

**Método:** `POST`  
**Tipo de conteúdo:** `multipart/form-data`  
**Campo(s):** `files` (pode conter um ou vários arquivos PDF)

---

## 🧪 Exemplos

### 📌 Usando Postman:

1. Vá em **Body > form-data**
2. Adicione vários campos com chave `files` e tipo `File`
3. Envie a requisição
4. O resultado virá como um arquivo `.csv` para download

### 📌 Usando cURL:

```bash
curl -X POST http://localhost:8080/analyze \
  -F "files=@/caminho/para/arquivo1.pdf" \
  -F "files=@/caminho/para/arquivo2.pdf" \
  -o resultado.csv
```

---

## 💾 Exemplo de saída `.csv`

```csv
DATA;HISTORICO;VALOR
06/06/2024;BUY QTY 23 PRICE 251.7156;5789.58
06/18/2024;SELL QTY 33 PRICE 105.7650;3489.98
10/06/2024;REG INT - 4.687%;4687.00
...
```

---

## 🛠️ Tecnologias usadas

- Java 21
- Spring Boot 3.4.4
- Apache PDFBox 3.0.4

---

## ▶️ Como rodar localmente

```bash
git clone https://github.com/andregnicoletti/pdf-insight.git
cd pdf-insight
./mvnw spring-boot:run
```

Acesse em: [http://localhost:8080/analyze](http://localhost:8080/analyze)

---

## 👨‍💻 Autor

**André Nicoletti**  
📧 andregnicoletti@gmail.com  
🔗 [linkedin.com/in/andre-nicoletti](https://www.linkedin.com/in/andre-nicoletti)

---

> “Código limpo é como uma boa piada – não precisa de explicação.”  
> — *Martin Fowler*