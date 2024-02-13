package pavlik.pokladna.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pavlik.pokladna.entity.Transaction;
import pavlik.pokladna.entity.TransactionSummary;
import pavlik.pokladna.service.TransactionService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * RestController pro manipulaci s transakcemi přes API.
 */
@RestController
@RequestMapping("/api/transactions")
public class ApiTransactionController {

    private final TransactionService transactionService;

    /**
     * Konstruktor pro ApiTransactionController.
     *
     * @param transactionService TransactionService pro manipulaci s transakcemi.
     */
    @Autowired
    public ApiTransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Metoda pro získání posledních 20 transakcí přes API.
     *
     * @return Seznam TransactionSummary obsahující posledních 20 transakcí.
     */
    @GetMapping("/last20")
    public List<TransactionSummary> getLast20Transactions() {
        List<Transaction> first20TransactionsAscending = transactionService.getFirst20TransactionsAscending();
        Collections.reverse(first20TransactionsAscending);

        // Vytvoření seznamu pro TransactionSummary
        List<TransactionSummary> transactionSummaries = new ArrayList<>();

        // Převod každé transakce na TransactionSummary
        for (Transaction transaction : first20TransactionsAscending) {
            TransactionSummary summary = new TransactionSummary();
            summary.setIdTransaction(transaction.getIdTransaction());
            summary.setBalanceAfter(transaction.getBalanceAfter());
            transactionSummaries.add(summary);
        }

        return transactionSummaries;
    }
}

