package com.paymentrouter;

import com.paymentrouter.dto.ReceiverDetails;
import com.paymentrouter.dto.TransferRequest;
import com.paymentrouter.dto.TransferResponse;
import com.paymentrouter.service.TransferService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@SpringBootApplication
public class IndiaPaymentRouterApplication {

    public static void main(String[] args) {
        SpringApplication.run(IndiaPaymentRouterApplication.class, args);
    }

    @Bean
    public CommandLineRunner interactiveConsole(TransferService transferService) {
        return args -> {
            String intro = """
                    IndiaPaymentRouter is ready
                    REST API: POST http://localhost:8082/api/transfer   (unchanged)
                    """;
            System.out.println(intro);

            Scanner scanner = new Scanner(System.in);
            List<TransferResponse> history = new ArrayList<>();
            boolean running = true;

            while (running) {
                printMenu();
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1" -> performTransfer(scanner, transferService, history);
                    case "2" -> printHistory(history);
                    case "3" -> {
                        running = false;
                        System.out.println("Exiting console. REST API is still running on port 8082.");
                    }
                    default -> System.out.println("Invalid choice. Please enter 1, 2, or 3.\n");
                }
            }
        };
    }

    private void printMenu() {
        String menu = """
                ================================
                 IndiaPaymentRouter - Main Menu
                ================================
                 1. New Transfer
                 2. View Transfer History
                 3. Exit
                --------------------------------""";
        System.out.println(menu);
        System.out.print("Choose an option: ");
    }

    private void performTransfer(Scanner scanner, TransferService transferService, List<TransferResponse> history) {
        System.out.println("\n---- New Transfer ----");

        System.out.print("Sender name: ");
        String senderName = scanner.nextLine().trim();

        System.out.print("Sender account number: ");
        String senderAccNo = scanner.nextLine().trim();

        System.out.print("Sender bank name: ");
        String senderBankName = scanner.nextLine().trim();

        System.out.print("Amount: ");
        String amountInput = scanner.nextLine().trim();

        System.out.print("Receiver name: ");
        String receiverName = scanner.nextLine().trim();

        System.out.print("Receiver account number: ");
        String receiverAccNo = scanner.nextLine().trim();

        System.out.print("Receiver bank name: ");
        String receiverBankName = scanner.nextLine().trim();

        try {
            BigDecimal amount = new BigDecimal(amountInput);
            ReceiverDetails receiver = new ReceiverDetails(receiverName, receiverAccNo, receiverBankName);
            TransferRequest request = new TransferRequest(senderName, senderAccNo, senderBankName, amount, receiver);

            TransferResponse response = transferService.transfer(request);
            history.add(response);

            System.out.println();
            System.out.println("Transfer successful:");
            System.out.println("  Receiver name : " + response.receiverName());
            System.out.println("  Receiver acc  : " + response.receiverAccNo());
            System.out.println("  Amount        : " + response.amount());
            System.out.println("  Method        : " + response.method());
            System.out.println();
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount entered. Please enter a numeric value.\n");
        } catch (RuntimeException e) {
            System.out.println("Transfer failed: " + e.getMessage() + "\n");
        }
    }

    private void printHistory(List<TransferResponse> history) {
        System.out.println();
        if (history.isEmpty()) {
            System.out.println("No transfers yet in this session.\n");
            return;
        }
        System.out.println("---- Transfer History (" + history.size() + ") ----");
        for (int i = 0; i < history.size(); i++) {
            TransferResponse r = history.get(i);
            System.out.printf("%d) %s -> %s | %s | %s%n",
                    i + 1, r.receiverName(), r.receiverAccNo(), r.amount(), r.method());
        }
        System.out.println();
    }
}
