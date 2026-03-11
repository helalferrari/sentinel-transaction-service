# Sentinel Transaction Service

O **Sentinel Transaction Service** é um microserviço robusto focado no processamento de transações financeiras em tempo real, utilizando o ecossistema **Spring Boot** e **Apache Kafka**. O serviço atua como a porta de entrada para transações brutas, aplicando validações iniciais e utilizando **Kafka Streams** para detecção de fraudes através de janelas de tempo.

## 🚀 Funcionalidades Principais

- **Ingestão de Transações**: API REST para recebimento de transações financeiras.
- **Processamento em Tempo Real**: Integração nativa com Apache Kafka.
- **Detecção de Fraude (Kafka Streams)**: Análise de comportamento suspeito baseada em janelas de tempo (ex: mais de 3 transações por conta em menos de 2 minutos).
- **Roteamento Inteligente**: Direcionamento automático de transações validadas vs. alertas de fraude.
- **Notificações Push (WebSockets)**: Alertas de fraude enviados em tempo real para o frontend via WebSockets/STOMP.
- **Documentação Técnica Evolutiva**: Histórico de decisões e guias técnicos em `GEMINI.md` e na pasta `/docs`.

## 🛠️ Tech Stack

- **Linguagem:** Java 21 (GraalVM CE)
- **Framework:** Spring Boot 3.2.3
- **Mensageria:** Apache Kafka (Modo KRaft)
- **Streaming:** Kafka Streams
- **Comunicação:** WebSockets (STOMP + SockJS)
- **Ferramentas:** Lombok, Jakarta Validation, JUnit 5, Mockito.
- **Infraestrutura:** Docker & Docker Compose.

## 🏗️ Arquitetura

O projeto segue o padrão **MVC (Model-View-Controller)** adaptado para uma arquitetura orientada a eventos:

1. **Model**: Representação da transação e seu ciclo de vida.
2. **DTO**: Objetos de transferência para garantir a segurança da API.
3. **Service**: Lógica de negócio e integração com o broker Kafka.
4. **Controller**: Exposição de endpoints REST para os clientes.
5. **Streams**: Topologia de processamento para análise de fraude em voo.

## 🚦 Como Rodar o Projeto

### 1. Iniciar a Infraestrutura (Kafka + AKHQ)
```bash
docker compose up -d
```
*O Kafka estará em `localhost:9092` e o AKHQ em `http://localhost:8080`.*

### 2. Rodar a Aplicação Spring Boot
```bash
./mvnw spring-boot:run
```
*A aplicação subirá na porta `8081`.*

### 3. Executar Testes
```bash
./mvnw test
```

## 📖 Documentação Adicional

- [GEMINI.md](./GEMINI.md): Registro mandatório de decisões arquiteturais e regras do projeto.
- [/docs](./docs/index.html): Documentação técnica completa em formato HTML para navegação offline.

---
Desenvolvido como parte da **Sentinel Solution**.
