<h1 align="center">📄 PDF Insight</h1>
<p align="center">
  API inteligente para análise e extração de dados estruturados a partir de arquivos PDF. <br/>
  Feito com Java + Spring Boot ☕🚀
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white"/>
  <img src="https://img.shields.io/badge/PDFBox-008CBA?style=for-the-badge"/>
</p>

---

## 🚀 Funcionalidades

- Upload de arquivos PDF via API
- Extração de texto utilizando Apache PDFBox
- Processamento e retorno dos dados em formato JSON
- Arquitetura limpa com Spring Boot
- Pronto para integração com serviços de IA (ex: OpenAI)

---

## 🛠️ Tecnologias utilizadas

- Java 21
- Spring Boot 3
- Apache PDFBox
- Maven
- Lombok

---

## ▶️ Como rodar localmente

### Pré-requisitos
- Java 21 (LTS)
- Maven

### Passos:

```bash
# Clone o repositório
git clone https://github.com/andregnicoletti/pdf-insight.git
cd pdf-insight

# Rode o projeto
./mvnw spring-boot:run
```

Acesse: `http://localhost:8080`

---

## 📁 Estrutura do projeto

```
pdf-insight/
├── controller/       # Endpoints REST
├── service/          # Lógica de negócio
├── model/            # DTOs e modelos
├── PdfInsightApp.java
```

---

## 📚 Exemplo de uso da API

### 📥 POST `/analyze`

Envia um arquivo PDF para análise.

**Requisição:**
- Content-Type: `multipart/form-data`
- Body: arquivo PDF

**Resposta:**
```json
{
  "nome": "André Nicoletti",
  "cpf": "000.000.000-00",
  "dataNascimento": "1987-06-12",
  "email": "andregnicoletti@gmail.com"
}
```

> 🔎 Os dados variam de acordo com o conteúdo do PDF

---

## 💡 Próximos passos

- Integração com IA (OpenAI / HuggingFace) para extração mais inteligente
- Suporte a OCR (PDFs escaneados)
- Persistência dos dados em banco
- Interface web para upload e visualização

---

## 👨‍💻 Autor

Feito com 💙 por [André Nicoletti](https://www.linkedin.com/in/andre-nicoletti)  
📧 andregnicoletti@gmail.com

---

> "Código limpo é como uma boa piada – não precisa de explicação." — *Martin Fowler*