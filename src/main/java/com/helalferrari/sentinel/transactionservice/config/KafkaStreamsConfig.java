package com.helalferrari.sentinel.transactionservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@Configuration
@EnableKafkaStreams
@Profile("!test") // Não carrega esta configuração no perfil 'test'
public class KafkaStreamsConfig {
}
