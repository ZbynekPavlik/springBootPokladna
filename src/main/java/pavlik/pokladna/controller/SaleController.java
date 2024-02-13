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
import pavlik.pokladna.entity.Sale;
import pavlik.pokladna.repository.SaleRepositoryInterface;
import pavlik.pokladna.service.SaleService;
import pavlik.pokladna.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("sales")
public class SaleController {
    private final SaleService saleService;
    private final UserService userService;
    private final SaleRepositoryInterface saleRepository;

    private final List<Sale> sales = new ArrayList<>();

    /**
     * Konstruktor pro SaleController.
     *
     * @param saleService    Služba pro práci s prodeji.
     * @param userService    Služba pro práci s uživateli.
     * @param saleRepository Rozhraní repozitáře pro přístup k datům prodejů.
     */
    @Autowired
    public SaleController(SaleService saleService, UserService userService, SaleRepositoryInterface saleRepository) {
        this.saleService = saleService;
        this.userService = userService;
        this.saleRepository = saleRepository;
    }

    /**
     * Metoda pro zobrazení seznamu prodejů s podporou stránkování.
     *
     * @param model Model pro předání dat do šablony.
     * @param page  Číslo aktuální stránky.
     * @param size  Velikost stránky.
     * @return Název šablony pro zobrazení seznamu prodejů.
     */
    @GetMapping("/index/{page}/{size}")
    public String index(Model model, @PathVariable("page") int page, @PathVariable("size") int size) {
        // Načtení prodejů s podporou stránkování
        Pageable pageable = PageRequest.of(page, size, Sort.by("idSale").descending());
        Page<Sale> salePage = saleRepository.findAll(pageable);

        // Seznam prodejů na aktuální stránce
        List<Sale> sales = salePage.getContent();

        // Celkový počet záznamů
        long totalElements = salePage.getTotalElements();

        // Vypočítání hodnot pro pocet prvku
        int firstElement = page * size + 1;
        int lastElement = (int) Math.min((long) (page + 1) * size, totalElements);

        if (totalElements == 0)
            firstElement = 0;

        // Přidání seznamu prodejů a informací o stránkování do modelu
        model.addAttribute("sales", sales);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", salePage.getTotalPages());
        model.addAttribute("totalElements", totalElements);

        // Přidání proměnných page a size do modelu
        model.addAttribute("page", page);
        model.addAttribute("size", size);

        // Přidání informací o aktuálních záznamech do modelu
        model.addAttribute("firstElement", firstElement);
        model.addAttribute("lastElement", lastElement);

        return "sales/index/index";
    }


    /**
     * Metoda pro zobrazení formuláře pro přidání nového prodeje.
     *
     * @param model Model pro komunikaci s Thymeleaf šablonou.
     * @return Název Thymeleaf šablony pro zobrazení formuláře pro přidání nového prodeje.
     */
    @GetMapping("/create")
    public String showAddSaleForm(Model model) {
        Sale sale = new Sale();

        // Předání aktuálního uživatele do modelu pro Thymeleaf

        model.addAttribute("sale", sale);

        return "sales/create/createForm";
    }

    /**
     * Metoda pro přidání prodeje s transakcí.
     *
     * @param sale  Objekt prodeje.
     * @param model Model pro komunikaci s Thymeleaf šablonou.
     * @return Název Thymeleaf šablony pro zobrazení potvrzení úspěšného přidání prodeje.
     */
    @PostMapping("/create")
    public String addSaleWithTransaction(@ModelAttribute("sale") Sale sale, Model model) {
        try {
            sale.setUser(userService.getCurrentUser());
            Sale savedSale = saleService.addSaleWithTransaction(sale);
            model.addAttribute("successMessage", "Tržba byla přidáná úspěšně.");
            return "sales/create/createConfirmation";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Nastala chyba při pridávání tržby: " + e.getMessage());
            return "sales/create/createForm"; // nebo jiná stránka pro zobrazení formuláře
        }
    }

    /**
     * Metoda pro zobrazení formuláře pro odstranění prodejů.
     *
     * @return Název Thymeleaf šablony pro zobrazení formuláře pro odstranění prodejů.
     */
    @GetMapping("/delete")
    public String showDeleteSalesForm() {
        return "sales/delete/deleteForm";
    }

    /**
     * Metoda pro zpracování formuláře pro odstranění prodeje.
     *
     * @param saleId ID prodeje k odstranění.
     * @param model  Model pro komunikaci s Thymeleaf šablonou.
     * @return Název Thymeleaf šablony pro potvrzení odstranění prodeje nebo chybové zprávy.
     */
    @PostMapping("/delete")
    public String processDeleteSaleForm(@RequestParam("transactionId") int saleId, Model model) {

        Optional<Sale> saleOptional = saleRepository.findById(saleId);
        if (saleOptional.isPresent()) {
            try {
                saleService.deleteSaleById(saleId);
                model.addAttribute("successMessage", "Tržba byla odstraněna úspěšně.");
            } catch (EntityNotFoundException | IllegalArgumentException e) {
                model.addAttribute("errorMessage", "Při odstranění tržby nastala chyba: " + e.getMessage());
            }
            return "sales/delete/deleteConfirmation";
        } else {
            // Pokud prodej s daným ID není nalezen, můžete přesměrovat na chybovou stránku nebo zobrazit vhodnou zprávu
            return "errors/sales/saleNotFound"; // Název chybové stránky
        }


    }

    /**
     * Metoda pro odstranění prodeje.
     *
     * @param saleId ID prodeje k odstranění.
     * @param model  Model pro komunikaci s Thymeleaf šablonou.
     * @return Název Thymeleaf šablony pro potvrzení odstranění prodeje nebo chybové zprávy.
     */
    @PostMapping("/delete/{id}")
    public String deleteSale(@PathVariable("id") int saleId, Model model) {
        Optional<Sale> saleOptional = saleRepository.findById(saleId);
        if (saleOptional.isPresent()) {
            try {
                saleService.deleteSaleById(saleId); // Odstraní prodej podle ID
                model.addAttribute("successMessage", "Tržba byla odstraněna úspěšně.");
            } catch (EntityNotFoundException | IllegalArgumentException e) {
                model.addAttribute("errorMessage", "Při odstranění tržby nastala chyba: " + e.getMessage());
            }
            return "sales/delete/deleteConfirmation";
        } else {
            // Pokud prodej s daným ID není nalezen, můžete přesměrovat na chybovou stránku nebo zobrazit vhodnou zprávu
            return "errors/sales/saleNotFound"; // Název chybové stránky
        }


    }


    /**
     * Metoda pro zobrazení formuláře pro odstranění všech prodejů.
     *
     * @param model Model pro komunikaci s Thymeleaf šablonou.
     * @return Název Thymeleaf šablony pro zobrazení formuláře pro odstranění všech prodejů.
     */
    @GetMapping("/deleteAll")
    public String deleteSaleAll(Model model) {

        return "sales/delete/deleteAllForm"; // Můžete změnit cílovou stránku přesměrování podle potřeby
    }

    /**
     * Metoda pro zpracování formuláře pro odstranění všech prodejů.
     *
     * @param model Model pro komunikaci s Thymeleaf šablonou.
     * @return Název Thymeleaf šablony pro potvrzení odstranění všech prodejů nebo chybové zprávy.
     */
    @PostMapping("/deleteAll")
    public String processDeleteAllSales(Model model) {
        try {
            int deletedCount = saleService.deleteAllSales();
            model.addAttribute("successMessage", "Všechny tržby byly odstraněny úspěšně. Celkem odstraněno: " + deletedCount);
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", "Při odstranění tržeb nastala chyba: " + e.getMessage());
        }
        return "sales/delete/deleteAllConfirmation";
    }


    /**
     * Metoda pro zobrazení formuláře pro zobrazení detailů prodeje.
     *
     * @return Název Thymeleaf šablony pro zobrazení formuláře pro zobrazení detailů prodeje.
     */
    @GetMapping("/show")
    public String showSaleDetailsForm() {
        return "sales/show/saleDetailsForm";
    }

    /**
     * Metoda pro zobrazení detailů konkrétního prodeje.
     *
     * @param id    ID prodeje, jehož detaily se mají zobrazit.
     * @param model Model pro komunikaci s Thymeleaf šablonou.
     * @return Název Thymeleaf šablony pro zobrazení detailů prodeje nebo chybové zprávy, pokud prodej není nalezen.
     */
    @GetMapping("show/{id}")
    public String showSaleDetails(@PathVariable("id") Integer id, Model model) {
        Optional<Sale> saleOptional = saleRepository.findById(id);
        if (saleOptional.isPresent()) {
            model.addAttribute("sale", saleOptional.get());
            return "sales/show/showSaleDetail"; // Název šablony pro zobrazení detailů prodeje
        } else {
            // Pokud prodej s daným ID není nalezen, můžete přesměrovat na chybovou stránku nebo zobrazit vhodnou zprávu
            return "errors/sales/saleNotFound"; // Název chybové stránky
        }
    }

}
