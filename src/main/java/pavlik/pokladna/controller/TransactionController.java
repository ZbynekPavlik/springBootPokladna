package pavlik.pokladna.controller;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pavlik.pokladna.entity.Transaction;
import pavlik.pokladna.entity.User;
import pavlik.pokladna.repository.TransactionRepositoryInterface;
import pavlik.pokladna.service.TransactionService;
import pavlik.pokladna.service.UserService;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService userService;

    private final TransactionRepositoryInterface transactionRepository;

    /**
     * Konstruktor třídy TransactionController.
     *
     * @param transactionService    Servisní třída pro manipulaci s transakcemi.
     * @param userService           Servisní třída pro manipulaci s uživateli.
     * @param transactionRepository Rozhraní pro přístup k datům transakcí v databázi.
     */
    @Autowired
    public TransactionController(TransactionService transactionService, UserService userService, TransactionRepositoryInterface transactionRepository) {
        this.transactionService = transactionService;
        this.userService = userService;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Metoda pro zobrazení seznamu transakcí s možností stránkování.
     *
     * @param model Model pro komunikaci s Thymeleaf šablonou.
     * @param page  Číslo aktuální stránky.
     * @param size  Velikost stránky.
     * @return Název Thymeleaf šablony pro zobrazení seznamu transakcí.
     */
    @GetMapping("/index/{page}/{size}")
    public String index(Model model, @PathVariable("page") int page, @PathVariable("size") int size) {
        // Načtení transakcí s podporou stránkování a seřazených podle ID transakce od nejvyššího k nejnižšímu
        Pageable pageable = PageRequest.of(page, size, Sort.by("idTransaction").descending());
        Page<Transaction> transactionPage = transactionRepository.findAll(pageable);

        // Seznam transakcí na aktuální stránce
        List<Transaction> transactions = transactionPage.getContent();

        // Celkový počet záznamů
        long totalElements = transactionPage.getTotalElements();

        // Vypočítání hodnot pro počet prvků
        int firstElement = page * size + 1;
        int lastElement = (int) Math.min((long) (page + 1) * size, totalElements);

        if (totalElements == 0)
            firstElement = 0;

        // Přidání seznamu transakcí a informací o stránkování do modelu
        model.addAttribute("transactions", transactions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactionPage.getTotalPages());
        model.addAttribute("totalElements", totalElements);

        // Přidání proměnných page a size do modelu
        model.addAttribute("page", page);
        model.addAttribute("size", size);

        // Přidání informací o aktuálních záznamech do modelu
        model.addAttribute("firstElement", firstElement);
        model.addAttribute("lastElement", lastElement);

        int currentBalance = transactionService.getCurrentStatusBalance();
        model.addAttribute("balance", currentBalance);

        return "transactions/index/index";
    }


    /**
     * Zobrazí formulář pro vkládání peněz do pokladny.
     *
     * @param model Model pro ukládání atributů.
     * @return Název šablony pro formulář pro vkládání peněz.
     */
    @GetMapping("/deposit")
    public String showDepositForm(Model model) {
        model.addAttribute("amount", 0); // Výchozí hodnota pro množství
        return "transactions/moveCash/depositForm";
    }

    /**
     * Zpracuje vložení peněz do pokladny.
     *
     * @param amount Množství peněz k vložení.
     * @param model  Model pro ukládání atributů.
     * @return Název šablony pro výsledek vkládání peněz.
     */
    @PostMapping("/deposit")
    public String processDeposit(@RequestParam("amount") int amount, Model model) {
        User currentUser = userService.getCurrentUser();
        try {
            transactionService.depositMoney(amount, currentUser);
            model.addAttribute("successMessage", "Peníze byly vloženy úspěšně.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", "Při vkládání peněz nastala chyba: " + e.getMessage());
        }
        // Přesměrování na jinou stránku
        return "transactions/moveCash/depositResult";
    }

    /**
     * Zobrazí formulář pro výběr peněz z pokladny.
     *
     * @param model Model pro ukládání atributů.
     * @return Název šablony pro formulář pro výběr peněz.
     */
    @GetMapping("/withdraw")
    public String showWithdrawForm(Model model) {
        model.addAttribute("amount", 0); // Výchozí hodnota pro množství
        return "transactions/moveCash/withdrawForm";
    }

    /**
     * Zpracuje výběr peněz z pokladny.
     *
     * @param amount Množství peněz k vybrání.
     * @param model  Model pro ukládání atributů.
     * @return Název šablony pro výsledek výběru peněz.
     */
    @PostMapping("/withdraw")
    public String processWithdraw(@RequestParam("amount") int amount, Model model) {
        User currentUser = userService.getCurrentUser();
        try {
            transactionService.withdrawMoney(amount, currentUser);
            model.addAttribute("successMessage", "Peníze byly vybrány úspěšně.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("errorMessage", "Při vybírání peněz nastala chyba: " + e.getMessage());
        }
        // Přesměrování na jinou stránku
        return "transactions/moveCash/withdrawResult";
    }


    /**
     * Zobrazí aktuální stav pokladny.
     *
     * @param model Model pro ukládání atributů.
     * @return Název šablony zobrazující aktuální stav pokladny.
     */
    @GetMapping("/currentBalance")
    public String getCurrentBalance(Model model) {
        int currentBalance = transactionService.getCurrentStatusBalance();
        model.addAttribute("balance", currentBalance);
        return "transactions/currentBalance";
    }

    /**
     * Zobrazí formulář pro odstranění transakce.
     *
     * @return Název šablony formuláře pro odstranění transakce.
     */
    @GetMapping("/delete")
    public String showDeleteTransactionForm() {
        return "transactions/delete/deleteForm";
    }

    /**
     * Zpracuje formulář pro odstranění transakce.
     *
     * @param transactionId ID transakce k odstranění.
     * @param model         Model pro ukládání atributů.
     * @return Název šablony potvrzující úspěšné odstranění transakce nebo chybového hlášení.
     */
    @PostMapping("/delete")
    public String processDeleteTransactionForm(@RequestParam("transactionId") int transactionId, Model model) {

        Optional<Transaction> transactionOptional = transactionRepository.findById(transactionId);
        if (transactionOptional.isPresent()) {
            Transaction transaction = transactionOptional.get();

            if (transaction.isDeleted()) {
                return "errors/transactions/transactionIsDeleted";
            } else {
                try {
                    transactionService.deleteTransactionById(transactionId);
                    model.addAttribute("successMessage", "Transakce byla odstraněna úspěšně.");
                } catch (IllegalStateException | EntityNotFoundException e) {
                    model.addAttribute("errorMessage", "Při odstranění transakce nastala chyba: " + e.getMessage());
                    return "transactions/delete/deleteConfirmation";
                }
            }
            return "transactions/delete/deleteConfirmation";

        } else {
            return "errors/transactions/transactionNotFound";
        }


    }

    /**
     * Zpracuje odstranění transakce pomocí ID.
     *
     * @param transactionId ID transakce k odstranění.
     * @param model         Model pro ukládání atributů.
     * @return Název šablony potvrzující úspěšné odstranění transakce nebo chybového hlášení.
     */
    @PostMapping("/delete/{id}")
    public String processDeleteIdTransactionForm(@PathVariable("id") int transactionId, Model model) {
        Optional<Transaction> transactionOptional = transactionRepository.findById(transactionId);
        if (transactionOptional.isPresent()) {


            Transaction transaction = transactionOptional.get();

            if (transaction.isDeleted()) {
                return "errors/transactions/transactionIsDeleted";
            } else {
                try {
                    transactionService.deleteTransactionById(transactionId);
                    model.addAttribute("successMessage", "Transakce byla odstraněna úspěšně.");
                } catch (IllegalStateException | EntityNotFoundException e) {
                    model.addAttribute("errorMessage", "Při odstranění transakce nastala chyba: " + e.getMessage());
                    return "transactions/delete/deleteConfirmation";
                }

            }
            return "transactions/delete/deleteConfirmation";
        } else {
            return "errors/transactions/transactionNotFound";
        }
    }

    /**
     * Zobrazí formulář pro odstranění všech transakcí.
     *
     * @param model Model pro ukládání atributů.
     * @return Název šablony formuláře pro odstranění všech transakcí.
     */
    @GetMapping("/deleteAll")
    public String showDeleteAllTransactionsForm(Model model) {
        return "transactions/delete/deleteAllForm";
    }

    /**
     * Zpracuje odstranění všech transakcí.
     *
     * @param model Model pro ukládání atributů.
     * @return Název šablony potvrzující úspěšné odstranění všech transakcí nebo chybového hlášení.
     */
    @PostMapping("/deleteAll")
    public String processDeleteAllTransactions(Model model) {
        try {
            int deletedCount = transactionService.deleteAllTransactions();
            model.addAttribute("successMessage", "Všechny transakce byly odstraněny úspěšně. Celkem odstraněno: " + deletedCount);
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", "Při odstranění transakcí nastala chyba: " + e.getMessage());
        }
        return "transactions/delete/deleteAllConfirmation";
    }


    /**
     * Zobrazí formulář pro zobrazení detailů transakce.
     *
     * @return Název šablony formuláře pro zobrazení detailů transakce.
     */
    @GetMapping("/show")
    public String showTransactionDetailsForm() {
        return "transactions/show/transactionDetailsForm";
    }

    /**
     * Zobrazí detaily transakce podle zadaného ID.
     *
     * @param id    ID transakce.
     * @param model Model pro ukládání atributů.
     * @return Název šablony pro zobrazení detailů transakce nebo chybové hlášení, pokud transakce s daným ID neexistuje.
     */
    @GetMapping("/show/{id}")
    public String showSaleDetails(@PathVariable("id") Integer id, Model model) {
        Optional<Transaction> transactionOptional = transactionRepository.findById(id);
        if (transactionOptional.isPresent()) {
            Transaction transaction = transactionOptional.get();
            model.addAttribute("transaction", transaction);
            if (transaction.getSale() == null) {
                model.addAttribute("sale.saleId", "null");
            } else {
                model.addAttribute("sale.saleId", transaction.getSale().getIdSale());
            }

            if (transaction.getUser() == null) {
                model.addAttribute("user.idUser", "null");
            } else {
                model.addAttribute("user.idUser", transaction.getUser().getIdUser());
            }
            // Add other attributes if needed
            return "transactions/show/transactionDetail";
        } else {
            return "errors/transactions/transactionNotFound";
        }
    }


}

