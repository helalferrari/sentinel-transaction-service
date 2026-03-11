package com.helalferrari.sentinel.transactionservice.service;

import com.helalferrari.sentinel.transactionservice.dto.TransactionRequestDTO;
import com.helalferrari.sentinel.transactionservice.model.Transaction;
import com.helalferrari.sentinel.transactionservice.model.TransactionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    @DisplayName("Should process transaction and send to Kafka successfully")
    void shouldProcessTransactionSuccessfully() {
        // Given
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setAccountId("ACC123");
        request.setAmount(new BigDecimal("100.50"));
        request.setMerchant("Amazon");

        // When
        Transaction result = transactionService.processTransaction(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotBlank();
        assertThat(result.getAccountId()).isEqualTo("ACC123");
        assertThat(result.getAmount()).isEqualTo(new BigDecimal("100.50"));
        assertThat(result.getMerchant()).isEqualTo("Amazon");
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(result.getTimestamp()).isNotNull();

        // Verify Kafka interaction
        verify(kafkaTemplate).send(eq("transactions-topic"), eq("ACC123"), any(Transaction.class));
    }
}
