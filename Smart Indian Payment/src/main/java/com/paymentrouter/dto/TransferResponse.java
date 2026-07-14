package com.paymentrouter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record TransferResponse(
        @JsonProperty("receiver_name") String receiverName,
        @JsonProperty("receiver_acc_no") String receiverAccNo,
        @JsonProperty("amount") BigDecimal amount,
        @JsonProperty("method") String method
) {
}
