package com.helalferrari.sentinel.transactionservice.service;

import com.helalferrari.sentinel.transactionservice.model.Transaction;
import com.helalferrari.sentinel.transactionservice.model.TransactionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AlertConsumerServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private AlertConsumerService alertConsumerService;

    @Test
    @DisplayName("Should push alert to WebSocket when fraud is detected")
    void shouldPushAlertToWebSocket() {
        // Given
        Transaction alert = Transaction.builder()
                .id(UUID.randomUUID().toString())
                .accountId("ACC-123")
                .amount(new BigDecimal("1000.00"))
                .status(TransactionStatus.REJECTED)
                .build();

        // When
        alertConsumerService.consumeAlerts(alert);

        // Then
        verify(messagingTemplate).convertAndSend(eq("/topic/alerts"), eq(alert));
    }
}
