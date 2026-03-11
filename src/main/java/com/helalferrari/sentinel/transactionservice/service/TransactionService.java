package com.helalferrari.sentinel.transactionservice.service;

import com.helalferrari.sentinel.transactionservice.config.KafkaConfig;
import com.helalferrari.sentinel.transactionservice.model.Transaction;
import com.helalferrari.sentinel.transactionservice.model.TransactionStatus;
import com.helalferrari.sentinel.transactionservice.dto.TransactionRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public Transaction processTransaction(TransactionRequestDTO request) {
        log.info("Processing transaction for account: {}", request.getAccountId());

        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID().toString())
                .accountId(request.getAccountId())
                .amount(request.getAmount())
                .merchant(request.getMerchant())
                .timestamp(LocalDateTime.now())
                .status(TransactionStatus.PENDING)
                .build();

        // Envia para o Kafka
        kafkaTemplate.send(KafkaConfig.TRANSACTIONS_RAW_TOPIC, transaction.getAccountId(), transaction);
        
        log.info("Transaction sent to Kafka: {}", transaction.getId());
        return transaction;
    }
}
