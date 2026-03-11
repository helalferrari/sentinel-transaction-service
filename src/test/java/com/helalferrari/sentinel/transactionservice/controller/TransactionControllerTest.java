package com.helalferrari.sentinel.transactionservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helalferrari.sentinel.transactionservice.dto.TransactionRequestDTO;
import com.helalferrari.sentinel.transactionservice.model.Transaction;
import com.helalferrari.sentinel.transactionservice.model.TransactionStatus;
import com.helalferrari.sentinel.transactionservice.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should create transaction and return 201 Created")
    void shouldCreateTransactionSuccessfully() throws Exception {
        // Given
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setAccountId("ACC123");
        request.setAmount(new BigDecimal("100.00"));
        request.setMerchant("Netflix");

        Transaction mockResponse = Transaction.builder()
                .id(UUID.randomUUID().toString())
                .accountId("ACC123")
                .amount(new BigDecimal("100.00"))
                .merchant("Netflix")
                .status(TransactionStatus.PENDING)
                .timestamp(LocalDateTime.now())
                .build();

        when(transactionService.processTransaction(any(TransactionRequestDTO.class))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value("ACC123"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when validation fails")
    void shouldReturnBadRequestWhenValidationFails() throws Exception {
        // Given (Missing accountId)
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setAmount(new BigDecimal("100.00"));
        request.setMerchant("Netflix");

        // When & Then
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
