package com.helalferrari.sentinel.transactionservice.streams;

import com.helalferrari.sentinel.transactionservice.config.KafkaConfig;
import com.helalferrari.sentinel.transactionservice.model.Transaction;
import com.helalferrari.sentinel.transactionservice.model.TransactionStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

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

        // 3. Converter a KTable em KStream e extrair a chave String pura
        KStream<String, Long> countStream = countsTable
                .toStream()
                .map((windowedKey, count) -> KeyValue.pair(windowedKey.key(), count));

        // 4. Unir (Join) o fluxo original com a contagem atual
        KStream<String, Transaction> analyzedStream = rawStream.join(
                countStream,
                (transaction, count) -> {
                    if (count != null && count > 3) {
                        log.warn("🚨 FRAUDE DETECTADA: Conta {} com {} transações na janela.", 
                                 transaction.getAccountId(), count);
                        transaction.setStatus(TransactionStatus.REJECTED);
                    } else {
                        transaction.setStatus(TransactionStatus.APPROVED);
                    }
                    return transaction;
                },
                JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofMinutes(2)),
                StreamJoined.with(stringSerde, transactionSerde, Serdes.Long())
        );

        // 5. Roteamento (Split)
        Map<String, KStream<String, Transaction>> branches = analyzedStream.split(Named.as("fraud-"))
                .branch((key, value) -> value.getStatus() == TransactionStatus.REJECTED, Branched.as("alerts"))
                .defaultBranch(Branched.as("validated"));

        // 6. Enviar para os respectivos tópicos
        branches.get("fraud-alerts").to(KafkaConfig.TRANSACTIONS_ALERTS_TOPIC, Produced.with(stringSerde, transactionSerde));
        branches.get("fraud-validated").to(KafkaConfig.TRANSACTIONS_VALIDATED_TOPIC, Produced.with(stringSerde, transactionSerde));

        log.info("Topologia de Detecção de Fraude Corrigida e Ativa!");
    }
}
