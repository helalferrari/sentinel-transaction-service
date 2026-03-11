package com.helalferrari.sentinel.transactionservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafkaStreams
public class KafkaConfig {

    public static final String TRANSACTIONS_RAW_TOPIC = "transactions-raw";
    public static final String TRANSACTIONS_VALIDATED_TOPIC = "transactions-validated";
    public static final String TRANSACTIONS_ALERTS_TOPIC = "transactions-alerts";

    @Bean
    public NewTopic transactionsRawTopic() {
        return TopicBuilder.name(TRANSACTIONS_RAW_TOPIC).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic transactionsValidatedTopic() {
        return TopicBuilder.name(TRANSACTIONS_VALIDATED_TOPIC).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic transactionsAlertsTopic() {
        return TopicBuilder.name(TRANSACTIONS_ALERTS_TOPIC).partitions(3).replicas(1).build();
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
