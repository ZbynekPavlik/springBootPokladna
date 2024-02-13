package pavlik.pokladna.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pavlik.pokladna.entity.Authority;
import pavlik.pokladna.entity.Sale;
import pavlik.pokladna.entity.Transaction;
import pavlik.pokladna.entity.User;
import pavlik.pokladna.repository.AuthorityRepositoryInterface;
import pavlik.pokladna.repository.SaleRepositoryInterface;
import pavlik.pokladna.repository.TransactionRepositoryInterface;
import pavlik.pokladna.repository.UserRepositoryInterface;

import java.util.List;
import java.util.Optional;

/**
 * Služba pro manipulaci s uživateli.
 */
@Service
public class UserService {

    private final UserRepositoryInterface userRepository;
    private final AuthorityRepositoryInterface authorityRepository;

    private final SaleRepositoryInterface saleRepository;

    private final TransactionRepositoryInterface transactionRepository;

    /**
     * Konstruktor služby pro manipulaci s uživateli.
     *
     * @param userRepository        Rozhraní pro přístup k datům uživatelů v databázi.
     * @param authorityRepository   Rozhraní pro přístup k datům autorit uživatelů v databázi.
     * @param saleRepository        Rozhraní pro přístup k datům prodejů v databázi.
     * @param transactionRepository Rozhraní pro přístup k datům transakcí v databázi.
     */
    @Autowired
    public UserService(UserRepositoryInterface userRepository, AuthorityRepositoryInterface authorityRepository, SaleRepositoryInterface saleRepository, TransactionRepositoryInterface transactionRepository) {
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.saleRepository = saleRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Metoda pro získání ID aktuálně přihlášeného uživatele.
     *
     * @return ID aktuálně přihlášeného uživatele, nebo -1, pokud uživatel není přihlášen.
     */
    public int getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            User currentUser = (User) authentication.getPrincipal();
            return currentUser.getIdUser();
        }
        return -1; // Vrací -1, pokud uživatel není přihlášen
    }

    /**
     * Metoda pro získání aktuálně přihlášeného uživatele.
     *
     * @return Aktuálně přihlášený uživatel nebo null, pokud není žádný uživatel přihlášen nebo není nalezen uživatel.
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();


            Optional<User> optionalUser = userRepository.findByUsername(username);

            // Pokud uživatel existuje, vrátíme ho, jinak null.
            return optionalUser.orElse(null);
        }

        return null; // Vrací null, pokud není přihlášený žádný uživatel nebo není nalezen uživatel.
    }

    /**
     * Metoda pro získání uživatele podle ID.
     *
     * @param id ID uživatele.
     * @return Uživatel nebo null, pokud uživatel není nalezen.
     */
    public User getUserById(int id) {
        Optional<User> optionalUser = userRepository.findById(id);
        return optionalUser.orElse(null); // Vrací null, pokud uživatel není nalezen
    }

    // Metody pro manipulaci s uživateli...

    /**
     * Metoda pro vytvoření nového uživatele.
     *
     * @param user Nový uživatel.
     */
    public void createUser(User user) {
        // Zašifrujeme heslo uživatele - zde zatim bez šifrace

        user.setPassword("{noop}" + user.getPassword());


        // Uložíme uživatele do databáze
        userRepository.save(user);

        // Vytvoření nových záznamů v tabulce authorities pro daného uživatele
        Authority authorityEmployee = new Authority();
        authorityEmployee.setUser(user);
        authorityEmployee.setAuthority("ROLE_EMPLOYEE");
        authorityRepository.save(authorityEmployee);

        if (user.getRole().equals("ADMIN")) {
            Authority authorityAdmin = new Authority();
            authorityAdmin.setUser(user);
            authorityAdmin.setAuthority("ROLE_ADMIN");
            authorityRepository.save(authorityAdmin);
        }

    }

    /**
     * Metoda pro aktualizaci informací o uživateli.
     *
     * @param updatedUser Aktualizovaný uživatel.
     */
    @Transactional
    public void updateUser(User updatedUser) {
        // Získání uživatele k aktualizaci z databáze
        User existingUser = userRepository.findById(updatedUser.getIdUser())
                .orElseThrow(() -> new IllegalArgumentException("Uživatel nebyl nalezen"));

        // smazání záznamů v tabulce authorities pro daného uživatele
        authorityRepository.deleteByUsername(existingUser.getUsername());

        // Aktualizace atributů
        existingUser.setPassword("{noop}" + updatedUser.getPassword());
        existingUser.setEnabled(updatedUser.getEnabled());
        existingUser.setRole(updatedUser.getRole());

        // Uložení aktualizovaného uživatele do databáze
        userRepository.save(existingUser);

        // Vytvoření nových záznamů v tabulce authorities pro daného uživatele
        Authority authorityEmployee = new Authority();
        authorityEmployee.setUser(existingUser);
        authorityEmployee.setAuthority("ROLE_EMPLOYEE");
        authorityRepository.save(authorityEmployee);

        if (existingUser.getRole().equals("ADMIN")) {
            Authority authorityAdmin = new Authority();
            authorityAdmin.setUser(existingUser);
            authorityAdmin.setAuthority("ROLE_ADMIN");
            authorityRepository.save(authorityAdmin);
        }
    }

    /**
     * Metoda pro smazání uživatele podle ID a nastavení uživatele na null v ostatních tabulkách.
     *
     * @param userId ID uživatele k odstranění.
     */
    @Transactional
    public void deleteUserByIdAndSetNullUserInOthersTable(int userId) {
        // Získání uživatele k odstranění z databáze
        User userToDelete = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Uživatel nebyl nalezen."));

        // Odstranění záznamů v tabulce authorities pro daného uživatele
        authorityRepository.deleteByUsername(userToDelete.getUsername());

        // removeUserIdFromSales
        List<Sale> sales = saleRepository.findByUser_IdUser(userId);
        for (Sale sale : sales) {
            sale.setUser(null); // Nastaví user_id na null
        }
        saleRepository.saveAll(sales);

        // removeUserIdFromTransactions
        List<Transaction> transactions = transactionRepository.findByUser_IdUser(userId);
        for (Transaction transaction : transactions) {
            transaction.setUser(null); // Nastaví user_id na null

        }
        transactionRepository.saveAll(transactions);

        // Odstranění uživatele z databáze
        userRepository.delete(userToDelete);
    }

    /**
     * Metoda pro smazání všech uživatelů a všech záznamů o nich v tabulce authorities.
     */
    @Transactional
    public void deleteAllUsers() {
        authorityRepository.deleteAll(); // Smazání všech záznamů v tabulce authorities
        userRepository.deleteAll(); // Smazání všech uživatelů
    }

    /**
     * Metoda pro smazání všech uživatelů a nastavení jejich ID na null v ostatních tabulkách.
     */
    @Transactional
    public void deleteAllUsersAndSetNullUserInOthersTable() {
        // Nastavení user_id na NULL v tabulce sales
        saleRepository.updateSalesWithNullUserId();

        // Nastavení user_id na NULL v tabulce transactions
        transactionRepository.updateTransactionsWithNullUserId();

        authorityRepository.deleteAll(); // Smazání všech záznamů v tabulce authorities

        // Smazání všech uživatelů
        userRepository.deleteAll();


    }


}
