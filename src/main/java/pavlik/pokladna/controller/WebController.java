package pavlik.pokladna.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pavlik.pokladna.entity.Transaction;
import pavlik.pokladna.service.TransactionService;

import java.util.List;

/**
 * Controller pro obsluhu webového rozhraní aplikace.
 */
@Controller
public class WebController {

    private final TransactionService transactionService;

    /**
     * Konstruktor třídy WebController.
     *
     * @param transactionService Služba pro práci s transakcemi.
     */
    public WebController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Zobrazuje úvodní stránku aplikace s informacemi o posledních transakcích a aktuálním stavu účtu.
     *
     * @param model Model pro uchování atributů.
     * @return Název šablony pro úvodní stránku.
     */
    @GetMapping("/")
    public String webIndex(Model model) {

        List<Transaction> transactions = transactionService.getFirst20Transactions();
        model.addAttribute("transactions", transactions);

        int currentBalance = transactionService.getCurrentStatusBalance();
        model.addAttribute("balance", currentBalance);
        return "index";
    }

    /**
     * Přesměruje na úvodní stránku aplikace.
     *
     * @return Přesměrování na kořenový adresář.
     */
    @GetMapping("design/index")
    public String designIndex() {
        return "redirect:/";
    } // Přesměruje na kořenový adresář

}
