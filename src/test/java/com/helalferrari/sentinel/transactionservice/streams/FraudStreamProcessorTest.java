package com.helalferrari.sentinel.transactionservice.streams;

import com.helalferrari.sentinel.transactionservice.config.KafkaConfig;
import com.helalferrari.sentinel.transactionservice.model.Transaction;
import com.helalferrari.sentinel.transactionservice.model.TransactionStatus;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class FraudStreamProcessorTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, Transaction> inputTopic;
    private TestOutputTopic<String, Transaction> validatedTopic;
    private TestOutputTopic<String, Transaction> alertsTopic;

    private final Serde<String> stringSerde = Serdes.String();
    private final Serde<Transaction> transactionSerde = Serdes.serdeFrom(
            new JsonSerializer<>(),
            new JsonDeserializer<>(Transaction.class)
    );

    @BeforeEach
    void setUp() {
        FraudStreamProcessor processor = new FraudStreamProcessor();
        StreamsBuilder builder = new StreamsBuilder();
        processor.process(builder);
        Topology topology = builder.build();

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-fraud-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);

        testDriver = new TopologyTestDriver(topology, props);

        inputTopic = testDriver.createInputTopic(KafkaConfig.TRANSACTIONS_RAW_TOPIC, 
                stringSerde.serializer(), transactionSerde.serializer());
        
        validatedTopic = testDriver.createOutputTopic(KafkaConfig.TRANSACTIONS_VALIDATED_TOPIC, 
                stringSerde.deserializer(), transactionSerde.deserializer());
        
        alertsTopic = testDriver.createOutputTopic(KafkaConfig.TRANSACTIONS_ALERTS_TOPIC, 
                stringSerde.deserializer(), transactionSerde.deserializer());
    }

    @AfterEach
    void tearDown() {
        testDriver.close();
    }

    @Test
    @DisplayName("Should detect fraud when more than 3 transactions occur in 2 minutes")
    void shouldDetectFraudOverLimit() {
        String accountId = "ACC-FRAUD-01";

        // Envia as 3 primeiras transações
        for (int i = 1; i <= 3; i++) {
            inputTopic.pipeInput(accountId, createTransaction(accountId, i));
        }

        // Envia a 4ª transação (Limite excedido)
        inputTopic.pipeInput(accountId, createTransaction(accountId, 4));

        // Lê todas as mensagens geradas no tópico de alerta
        List<Transaction> alerts = alertsTopic.readValuesToList();
        
        // Verifica se pelo menos um alerta foi gerado e se tem o status correto
        assertThat(alerts).isNotEmpty();
        boolean hasFraudStatus = alerts.stream()
                .anyMatch(tx -> tx.getStatus() == TransactionStatus.REJECTED);
        
        assertThat(hasFraudStatus).isTrue();
    }

    private Transaction createTransaction(String accountId, int id) {
        return Transaction.builder()
                .id("tx-" + id)
                .accountId(accountId)
                .amount(BigDecimal.TEN)
                .merchant("Test Merchant")
                .status(TransactionStatus.PENDING)
                .build();
    }
}
