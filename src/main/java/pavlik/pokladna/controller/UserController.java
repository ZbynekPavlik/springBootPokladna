package pavlik.pokladna.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pavlik.pokladna.entity.User;
import pavlik.pokladna.repository.UserRepositoryInterface;
import pavlik.pokladna.service.SaleService;
import pavlik.pokladna.service.TransactionService;
import pavlik.pokladna.service.UserService;

import java.util.List;
import java.util.Optional;

/**
 * Controller pro manipulaci s uživateli.
 */
@Controller
@RequestMapping("users")
public class UserController {

    private final UserService userService;
    private final SaleService saleService;
    private final TransactionService transactionService;

    private final UserRepositoryInterface userRepository;

    /**
     * Konstruktor pro UserController.
     *
     * @param userService        Služba pro manipulaci s uživateli.
     * @param saleService        Služba pro manipulaci s prodeji.
     * @param transactionService Služba pro manipulaci s transakcemi.
     * @param userRepository     Rozhraní repozitáře pro práci s uživatelskými daty.
     */
    @Autowired
    public UserController(UserService userService, SaleService saleService, TransactionService transactionService, UserRepositoryInterface userRepository) {
        this.userService = userService;
        this.saleService = saleService;
        this.transactionService = transactionService;
        this.userRepository = userRepository;
    }

    /**
     * Zobrazí stránkovaný seznam uživatelů.
     *
     * @param model Model pro uchování atributů.
     * @param page  Číslo stránky.
     * @param size  Počet uživatelů na stránce.
     * @return Název šablony pro zobrazení seznamu uživatelů.
     */
    @GetMapping("/index/{page}/{size}")
    public String index(Model model, @PathVariable("page") int page, @PathVariable("size") int size) {
        // Načtení uživatelů s podporou stránkování a seřazených podle ID uživatele od nejvyššího k nejnižšímu
        Pageable pageable = PageRequest.of(page, size, Sort.by("idUser").descending());
        Page<User> userPage = userRepository.findAll(pageable);

        // Seznam uživatelů na aktuální stránce
        List<User> users = userPage.getContent();

        // Celkový počet záznamů
        long totalElements = userPage.getTotalElements();

        // Vypočítání hodnot pro počet prvků
        int firstElement = page * size + 1;
        int lastElement = (int) Math.min((long) (page + 1) * size, totalElements);

        if (totalElements == 0)
            firstElement = 0;

        // Přidání seznamu uživatelů a informací o stránkování do modelu
        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalElements", totalElements);

        // Přidání proměnných page a size do modelu
        model.addAttribute("page", page);
        model.addAttribute("size", size);

        // Přidání informací o aktuálních záznamech do modelu
        model.addAttribute("firstElement", firstElement);
        model.addAttribute("lastElement", lastElement);

        return "users/index/index";
    }

    /**
     * Zobrazí formulář pro vytvoření nového uživatele.
     *
     * @param model Model pro uchování atributů.
     * @return Název šablony pro zobrazení formuláře pro vytvoření uživatele.
     */
    @GetMapping("/create")
    public String showCreateUserForm(Model model) {
        model.addAttribute("user", new User());
        return "users/create/createForm";
    }

    /**
     * Vytvoří nového uživatele na základě poskytnutých informací.
     *
     * @param user  Nový uživatel k vytvoření.
     * @param model Model pro uchování atributů.
     * @return Název šablony pro zobrazení potvrzení vytvoření uživatele.
     */
    @PostMapping("/create")
    public String createUser(User user, Model model) {
        try {
            userService.createUser(user);
            model.addAttribute("successMessage", "Uživatel byl vytvořen úspěšně");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Při vytváření uživatele nastala chyba: " + e.getMessage());
        }
        return "users/create/createConfirmation";
    }

    /**
     * Zobrazí formulář pro výběr uživatele pro aktualizaci.
     *
     * @return Název šablony pro zobrazení formuláře pro výběr uživatele.
     */
    @GetMapping("/update")
    public String showSelectFormUser() {
        return "users/update/userSelectForm";
    }

    /**
     * Zobrazí formulář pro aktualizaci konkrétního uživatele na základě zadaného ID.
     *
     * @param userId ID uživatele, který má být aktualizován.
     * @param model  Model pro uchování atributů.
     * @return Název šablony pro zobrazení formuláře pro aktualizaci uživatele.
     */
    @GetMapping("/update/{userId}")
    public String showUpdateUserForm(@PathVariable int userId, Model model) {

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {

            User user = userService.getUserById(userId);
            model.addAttribute("user", user);
            return "users/update/updateForm";


        } else {
            // Pokud prodej s daným ID není nalezen, můžete přesměrovat na chybovou stránku nebo zobrazit vhodnou zprávu
            return "errors/users/userNotFound"; // Název chybové stránky
        }


    }

    /**
     * Aktualizuje existujícího uživatele na základě poskytnutých informací.
     *
     * @param user  Aktualizovaný uživatel.
     * @param model Model pro uchování atributů.
     * @return Název šablony pro zobrazení potvrzení aktualizace uživatele.
     */
    @PostMapping("/update")
    public String updateUser(@ModelAttribute("user") User user, Model model) {
        try {
            userService.updateUser(user);
            model.addAttribute("successMessage", "Uživatel byl upraven úspěšně.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", "Při úpravě uživatele nastala chyba: " + e.getMessage());
        }
        return "users/update/updateConfirmation";
    }

    /**
     * Zobrazí formulář pro odstranění uživatele.
     *
     * @return Název šablony pro zobrazení formuláře pro odstranění uživatele.
     */
    @GetMapping("/delete")
    public String showDeleteForm() {
        return "users/delete/deleteForm";
    }

    /**
     * Zpracuje požadavek na odstranění uživatele na základě poskytnutého ID.
     *
     * @param userId ID uživatele, který má být odstraněn.
     * @param model  Model pro uchování atributů.
     * @return Název šablony pro zobrazení potvrzení odstranění uživatele.
     */
    @PostMapping("/delete")
    public String processDeleteTransactionForm(@RequestParam("userId") int userId, Model model) {

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {

            try {
                userService.deleteUserByIdAndSetNullUserInOthersTable(userId);
                model.addAttribute("successMessage", "Uživatel byl odstraněn úspěšně.");
            } catch (IllegalArgumentException e) {
                model.addAttribute("errorMessage", "Při odstranění uživatele nastala chyba: " + e.getMessage());
            }
            return "transactions/delete/deleteConfirmation";


        } else {
            // Pokud prodej s daným ID není nalezen, můžete přesměrovat na chybovou stránku nebo zobrazit vhodnou zprávu
            return "errors/users/userNotFound"; // Název chybové stránky
        }


    }


    /**
     * Obsluhuje potvrzení odstranění uživatele na základě poskytnutého ID.
     *
     * @param userId ID uživatele, který má být odstraněn.
     * @param model  Model pro uchování atributů.
     * @return Název šablony pro zobrazení potvrzení odstranění uživatele.
     */
    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") int userId, Model model) {

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {

            try {
                //saleService.removeUserIdFromSales(userId); // Odstraní všechny vazby uživatele z prodejů
                //transactionService.removeUserIdFromTransactions(userId);
                userService.deleteUserByIdAndSetNullUserInOthersTable(userId);
                model.addAttribute("successMessage", "Uživatel byl odstraněn úspěšně.");
            } catch (IllegalArgumentException e) {
                model.addAttribute("errorMessage", "Při odstranění uživatele nastala chyba: " + e.getMessage());
            }
            return "users/delete/deleteConfirmation";


        } else {
            // Pokud prodej s daným ID není nalezen, můžete přesměrovat na chybovou stránku nebo zobrazit vhodnou zprávu
            return "errors/users/userNotFound"; // Název chybové stránky
        }


    }


    /**
     * Zobrazí stránku pro potvrzení odstranění všech uživatelů.
     *
     * @return Název šablony pro zobrazení stránky pro odstranění všech uživatelů.
     */
    @GetMapping("/deleteAll")
    public String showDeleteAllUsersPage() {
        return "users/delete/DeleteAllForm";
    }

    /**
     * Odstraní všechny uživatele a nastaví null všechny vazby na uživatele v ostatních tabulkách.
     *
     * @param model Model pro uchování atributů.
     * @return Název šablony pro zobrazení potvrzení odstranění všech uživatelů.
     */
    @PostMapping("/deleteAll")
    public String deleteAllUsers(Model model) {
        try {

            userService.deleteAllUsersAndSetNullUserInOthersTable();
            model.addAttribute("successMessage", "Všichni uživatelé byli odstraněni úspěšně.");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Při odstranění uživatelů nastala chyba: " + e.getMessage());
        }
        return "users/delete/deleteAllConfirmation";
    }

    /**
     * Zobrazí formulář pro zobrazení detailů uživatele.
     *
     * @return Název šablony pro zobrazení formuláře pro zobrazení detailů uživatele.
     */
    @GetMapping("/show")
    public String showSaleDetailsForm() {
        return "users/show/userDetailsForm";
    }

    /**
     * Zobrazí detaily uživatele na základě poskytnutého ID.
     *
     * @param id    ID uživatele, pro kterého se mají zobrazit detaily.
     * @param model Model pro uchování atributů.
     * @return Název šablony pro zobrazení detailů uživatele nebo chybové stránky, pokud uživatel není nalezen.
     */
    @GetMapping("/show/{id}")
    public String showSaleDetails(@PathVariable("id") Integer id, Model model) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            model.addAttribute("user", userOptional.get());
            return "users/show/showUserDetail"; // Název šablony pro zobrazení detailů prodeje
        } else {
            // Pokud prodej s daným ID není nalezen, můžete přesměrovat na chybovou stránku nebo zobrazit vhodnou zprávu
            return "errors/users/userNotFound"; // Název chybové stránky
        }
    }

}
