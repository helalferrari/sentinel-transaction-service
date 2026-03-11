package com.helalferrari.sentinel.transactionservice.service;

import com.helalferrari.sentinel.transactionservice.config.KafkaConfig;
import com.helalferrari.sentinel.transactionservice.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertConsumerService {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = KafkaConfig.TRANSACTIONS_ALERTS_TOPIC, groupId = "alert-group")
    public void consumeAlerts(Transaction alert) {
        log.warn("ALERT RECEIVED: Suspected fraud for account {}. Pushing to WebSocket...", alert.getAccountId());
        
        // Push em tempo real para o frontend React
        messagingTemplate.convertAndSend("/topic/alerts", alert);
    }
}
