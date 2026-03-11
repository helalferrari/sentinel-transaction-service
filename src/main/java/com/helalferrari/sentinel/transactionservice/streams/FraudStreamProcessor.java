package com.helalferrari.sentinel.transactionservice.streams;

import com.helalferrari.sentinel.transactionservice.config.KafkaConfig;
import com.helalferrari.sentinel.transactionservice.model.Transaction;
import com.helalferrari.sentinel.transactionservice.model.TransactionStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Slf4j
public class FraudStreamProcessor {

    private final Serde<String> stringSerde = Serdes.String();
    private final Serde<Transaction> transactionSerde = Serdes.serdeFrom(
            new JsonSerializer<>(),
            new JsonDeserializer<>(Transaction.class)
    );

    @Autowired
    public void process(StreamsBuilder streamsBuilder) {
        // 1. Consumir as transações brutas
        KStream<String, Transaction> rawStream = streamsBuilder.stream(
                KafkaConfig.TRANSACTIONS_RAW_TOPIC,
                Consumed.with(stringSerde, transactionSerde)
        );

        // 2. Definir a Janela de Tempo (2 minutos) para contagem por conta
        KTable<Windowed<String>, Long> countsTable = rawStream
                .groupByKey(Grouped.with(stringSerde, transactionSerde))
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(2)))
                .count();

        // 3. Unir (Join) o fluxo original com a contagem atual para marcar fraudes
        // Como o count é por janela, precisamos do stream para rotear o evento individual
        KStream<String, Transaction> analyzedStream = rawStream.leftJoin(
                countsTable,
                (transaction, count) -> {
                    if (count != null && count > 3) {
                        log.warn("SUSPICIOUS TRANSACTION DETECTED: Account {} has {} transactions in 2min window", 
                                 transaction.getAccountId(), count);
                        transaction.setStatus(TransactionStatus.REJECTED); // Ou criar um status SUSPICIOUS
                    } else {
                        transaction.setStatus(TransactionStatus.APPROVED);
                    }
                    return transaction;
                },
                JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofMinutes(2)),
                StreamJoined.with(stringSerde, transactionSerde, Serdes.Long())
        );

        // 4. Split / Roteamento
        Map<String, KStream<String, Transaction>> branches = analyzedStream.split(Named.as("fraud-"))
                .branch((key, value) -> value.getStatus() == TransactionStatus.REJECTED, Branched.as("alerts"))
                .defaultBranch(Branched.as("validated"));

        // 5. Enviar para os respectivos tópicos
        branches.get("fraud-alerts").to(KafkaConfig.TRANSACTIONS_ALERTS_TOPIC, Produced.with(stringSerde, transactionSerde));
        branches.get("fraud-validated").to(KafkaConfig.TRANSACTIONS_VALIDATED_TOPIC, Produced.with(stringSerde, transactionSerde));

        log.info("Fraud Detection Topology Ready: Window=2m, Limit=3 per account.");
    }
}
