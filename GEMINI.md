# Sentinel Transaction Service - Project Knowledge

## Project Overview
Serviço responsável pelo processamento inicial de transações financeiras, validação e integração com o ecossistema Sentinel via Apache Kafka.

## Architecture & Patterns
- **Pattern:** MVC (Model-View-Controller).
- **Packages:**
    - `model`: Entidades de negócio (`Transaction`, `TransactionStatus`).
    - `dto`: Objetos de transferência de dados (`TransactionRequestDTO`).
    - `service`: Lógica de negócio e integração com Kafka.
    - `controller`: Endpoints REST.
    - `config`: Configurações de infraestrutura (Kafka, WebSocket, etc).
    - `exception`: Tratamento global de exceções.
    - `streams`: Topologias de processamento em tempo real.
- **Messaging:** Apache Kafka em modo KRaft (Porta 9092).
- **Data Streaming & Fraud Detection:**
    - **Kafka Streams:** Processamento em tempo real habilitado.
    - **Topologia:**
        1. Consome de `transactions-raw`.
        2. Agrupa por `accountId`.
        3. Janela: **Tumbling Window de 2 minutos**.
        4. Regra de Fraude: Se `count > 3` transações na janela, marca como `REJECTED`.
        5. Roteamento (Branching):
            - Sucesso: `transactions-validated`.
            - Suspeita/Erro: `transactions-alerts`.
- **Persistence Strategy:** Por enquanto, as transações são enviadas apenas para os tópicos do Kafka.

## Technical Stack
- Java 21 (GraalVM CE)
- Spring Boot 3.2.3
- Lombok
- Spring Kafka
- JUnit 5 & Mockito

## Rules & Decisions
1. **Packages:** Sempre utilizar o prefixo `com.helalferrari.sentinel.transactionservice`.
2. **Naming:** Preferir o sufixo `DTO` para classes na camada de transferência.
3. **Kafka:** As mensagens devem ser enviadas com `accountId` como chave para garantir ordem por conta.
4. **Validation:** Utilizar `jakarta.validation` no Controller para fail-fast.
5. **Errors:** Utilizar o `GlobalExceptionHandler` para padronizar respostas de erro.
6. **Kafka Topics:** Os tópicos devem ser criados programaticamente via `KafkaConfig`.
7. **Kafka Streams Activation:** A anotação `@EnableKafkaStreams` deve residir em uma classe de configuração dedicada (`KafkaStreamsConfig`) protegida pelo profile `!test`. Isso evita falhas em testes de slice.
8. **Testing Strategy:**
    - **Unit:** Mockito para Service e Consumer.
    - **Web Slice:** `MockMvc` para Controllers (isolado do Kafka via profiles).
    - **Streams Topology:** Utilizar `TopologyTestDriver` para validar a lógica de fraude.
9. **Documentation Maintenance (Mandatory):** Toda evolução arquitetural ou mudança em regra de negócio DEVE ser refletida neste arquivo e em `/docs/`.
10. **Context Start:** Ao iniciar uma nova sessão, o Gemini CLI deve ler este arquivo primeiro.
