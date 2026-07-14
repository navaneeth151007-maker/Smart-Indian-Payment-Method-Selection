package com.paymentrouter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ReceiverDetails(
        @JsonProperty("receiver_name") String receiverName,
        @JsonProperty("acc_no") String accNo,
        @JsonProperty("bank_name") String bankName
) {
}
