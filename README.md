# Sentinel Transaction Service 🛡️

Microserviço de alta performance projetado para ingestão, processamento e análise de transações financeiras em tempo real, focado na detecção proativa de padrões de fraude.

## 🏦 Sobre o Projeto

Este serviço é o núcleo da solução **Sentinel**. Ele implementa uma arquitetura orientada a eventos (EDA) utilizando **Apache Kafka** para garantir escalabilidade e consistência no processamento de fluxos financeiros críticos.

## 🚀 Funcionalidades

* **Ingestão de Transações:** API REST resiliente para recebimento de fluxos financeiros.
* **Detecção de Fraude:** Processamento via Kafka Streams para identificação de comportamentos suspeitos em janelas de tempo.
* **Resiliência:** Implementação de estratégias de retentativa e Dead Letter Queues (DLQ).
* **Streaming de Dados:** Publicação de alertas imediatos para consumo de dashboards.

## 🛠️ Stack Tecnológica

* **Linguagem:** Java 21+
* **Framework:** Spring Boot 3.x
* **Messaging:** Apache Kafka & Kafka Streams
* **Container:** Docker & Docker Compose
* **Qualidade:** JUnit 5 & Mockito

---
Desenvolvido como projeto de estudo focado em arquiteturas event-driven para o setor bancário.
