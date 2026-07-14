package com.paymentrouter.service;

import com.paymentrouter.dto.ReceiverDetails;
import com.paymentrouter.dto.TransferRequest;
import com.paymentrouter.dto.TransferResponse;
import com.paymentrouter.exception.InvalidAmountException;
import com.paymentrouter.exception.InvalidReceiverDetailsException;
import com.paymentrouter.exception.InvalidSenderDetailsException;
import com.paymentrouter.exception.SameAccountTransferException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TransferService {

    private static final BigDecimal UPI_LIMIT = new BigDecimal("100000");
    private static final BigDecimal IMPS_LIMIT = new BigDecimal("200000");

    public TransferResponse transfer(TransferRequest request) {
        validateSender(request);
        validateReceiver(request.receiver());
        validateAmount(request.amount());
        validateNotSameAccount(request.accNo(), request.receiver().accNo());

        String method = resolvePaymentMethod(request.amount());
        ReceiverDetails receiver = request.receiver();

        return new TransferResponse(receiver.receiverName(), receiver.accNo(), request.amount(), method);
    }

    private String resolvePaymentMethod(BigDecimal amount) {
        return switch (amount) {
            case BigDecimal amt when amt.compareTo(UPI_LIMIT) <= 0 -> "UPI";
            case BigDecimal amt when amt.compareTo(IMPS_LIMIT) <= 0 -> "IMPS";
            default -> "NEFT";
        };
    }

    private void validateSender(TransferRequest request) {
        if (request.senderName() == null || !request.senderName().matches("[a-zA-Z ]+")) {
            throw new InvalidSenderDetailsException("Sender name should contain only alphabets and spaces");
        }
        if (request.accNo() == null || !request.accNo().matches("\\d{10}")) {
            throw new InvalidSenderDetailsException("Sender account number must contain exactly 10 digits");
        }
        if (request.bankName() == null || request.bankName().isBlank()) {
            throw new InvalidSenderDetailsException("Sender bank name must not be empty");
        }
    }

    private void validateReceiver(ReceiverDetails receiver) {
        if (receiver == null) {
            throw new InvalidReceiverDetailsException("Receiver details must be provided");
        }
        if (receiver.receiverName() == null || !receiver.receiverName().matches("[a-zA-Z ]+")) {
            throw new InvalidReceiverDetailsException("Receiver name should contain only alphabets and spaces");
        }
        if (receiver.accNo() == null || !receiver.accNo().matches("\\d{10}")) {
            throw new InvalidReceiverDetailsException("Receiver account number must contain exactly 10 digits");
        }
        if (receiver.bankName() == null || receiver.bankName().isBlank()) {
            throw new InvalidReceiverDetailsException("Receiver bank name must not be empty");
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Transfer amount must be greater than 0");
        }
    }

    private void validateNotSameAccount(String senderAccNo, String receiverAccNo) {
        if (senderAccNo.equals(receiverAccNo)) {
            throw new SameAccountTransferException("Sender and receiver accounts cannot be the same");
        }
    }
}
