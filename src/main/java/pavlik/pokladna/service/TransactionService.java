package pavlik.pokladna.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pavlik.pokladna.entity.Transaction;
import pavlik.pokladna.entity.User;
import pavlik.pokladna.repository.TransactionRepositoryInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Služba pro manipulaci s transakcemi.
 */
@Service
public class TransactionService {
    private final TransactionRepositoryInterface transactionRepository;
    private final SaleService saleService;
    private final UserService userService;

    /**
     * Konstruktor pro vytvoření instance TransactionService s určenými repozitáři a službami.
     *
     * @param transactionRepository Rozhraní repozitáře transakcí.
     * @param saleService           Služba pro prodej.
     * @param userService           Služba pro uživatele.
     */
    @Autowired
    public TransactionService(TransactionRepositoryInterface transactionRepository, SaleService saleService, UserService userService) {
        this.transactionRepository = transactionRepository;
        this.saleService = saleService;
        this.userService = userService;
    }

    /**
     * Metoda pro získání aktuálního stavu zůstatku.
     *
     * @return Aktuální stav zůstatku.
     */
    public int getCurrentStatusBalance() {
        // nastaveni balance Before z posledniho zaznanu z financialtransaction ze sloupce balance after
        Transaction lastTransaction = transactionRepository.findLastTransaction();
        int currentStatusBalance = 0;
        if (lastTransaction != null) {
            currentStatusBalance = lastTransaction.getBalanceAfter();
        }
        return currentStatusBalance;
    }

    /**
     * Metoda pro vložení peněz do pokladny.
     *
     * @param amount      Částka k vložení.
     * @param currentUser Aktuálně přihlášený uživatel.
     */
    @Transactional
    public void depositMoney(int amount, User currentUser) {

        if (amount <= 0) {
            throw new IllegalArgumentException("Částka musí být kladné číslo.");
        }

        Transaction lastTransaction = transactionRepository.findLastTransaction();
        int balanceBefore = 0;
        if (lastTransaction != null) {
            balanceBefore = lastTransaction.getBalanceAfter();
        }
        int balanceAfter = balanceBefore + amount;

        Transaction transaction = new Transaction();
        transaction.setDescription("Vklad peněz do pokladny");
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setSale(null);
        transaction.setUser(currentUser);

        transactionRepository.save(transaction);
    }

    /**
     * Metoda pro odebrání peněz z pokladny.
     *
     * @param amount      Částka k odebrání.
     * @param currentUser Aktuálně přihlášený uživatel.
     * @throws IllegalArgumentException Pokud je zadaná záporná nebo nulová částka.
     * @throws IllegalStateException    Pokud není dostatek finančních prostředků na účtu.
     */
    @Transactional
    public void withdrawMoney(int amount, User currentUser) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Částka musí být kladné číslo.");
        }


        Transaction lastTransaction = transactionRepository.findLastTransaction();
        int balanceBefore = 0;
        if (lastTransaction != null) {
            balanceBefore = lastTransaction.getBalanceAfter();
        }


        if (balanceBefore < amount) {
            throw new IllegalStateException("Nedostatek peněz v pokladně.");
        }

        int balanceAfter = balanceBefore - amount;

        Transaction transaction = new Transaction();
        transaction.setDescription("Výběr peněz z pokladny");
        transaction.setAmount(-amount); // Negative amount for withdrawal
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setSale(null);
        transaction.setUser(currentUser);

        transactionRepository.save(transaction);
    }

    /**
     * Odstraní transakci podle zadaného ID.
     * Kontroluje, zda transakce již byla smazána a zda patří k prodeji. Pokud ano, provede také odstranění tržby z prodeje.
     * Pokud ne, vytvoří negační transakci a označí původní transakci jako smazanou.
     *
     * @param transactionId ID transakce k odstranění.
     * @throws EntityNotFoundException Pokud transakce s daným ID nebyla nalezena.
     * @throws IllegalStateException   Pokud transakce již byla smazána nebo pokud transakce patří k prodeji, ale odstranění tržby z prodeje selže.
     */
    @Transactional
    public void deleteTransactionById(int transactionId) {
        // Získání transakce pro odstranění
        Transaction transactionToDelete = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction with ID " + transactionId + " not found."));

        // Kontrola dostatečnosti peněz v pokladně pro vytvoření negační transakce
        int balanceAfter = calculateBalanceBefore() - transactionToDelete.getAmount();
        if (balanceAfter < 0) {
            throw new IllegalStateException("Operace odstranění transakce není možná, protože není dostatek peněz v pokladně.");
        }

        if (transactionToDelete.isDeleted()) { // Kontrola, zda transakce již není smazána
            throw new IllegalStateException("Transaction with ID " + transactionId + " has already been deleted.");
        }

        // Kontrola, zda transakce patří k prodeji
        if (transactionToDelete.getSale() != null) {
            // Pokud transakce patří k prodeji, použij metodu pro odstranění tržby z prodeje
            saleService.deleteSaleById(transactionToDelete.getSale().getIdSale());
            return; // Metoda se zde ukončí, pokračování se nedostane k vytvoření negační transakce
        }

        transactionToDelete.setDeleted(true);

        int balanceBefore = calculateBalanceBefore();
        int balanceAfterCurrent = balanceBefore - transactionToDelete.getAmount();

        String originalDescription = transactionToDelete.getDescription();
        transactionToDelete.setDescription("(smazáno) - " + transactionToDelete.getDescription());

        // Vytvoření nové transakce, která neguje původní transakci
        Transaction repairTransaction = new Transaction();
        repairTransaction.setDescription("Zrušení transakce ID: " + transactionId + " - " + originalDescription);
        repairTransaction.setAmount(-transactionToDelete.getAmount());
        repairTransaction.setBalanceBefore(balanceBefore);
        repairTransaction.setBalanceAfter(balanceAfterCurrent);
        repairTransaction.setSale(null); // Nepřiřazujeme k žádnému prodeji
        repairTransaction.setUser(userService.getCurrentUser());
        repairTransaction.setDeleted(true);

        // Uložení nové transakce
        transactionRepository.save(repairTransaction);
    }

    /**
     * Odstraní všechny transakce, které nebyly smazány.
     * Kontroluje, zda součet hodnoty transakcí nepřesahuje aktuální stav pokladny.
     *
     * @return Počet odstraněných transakcí.
     * @throws IllegalStateException Pokud součet hodnoty transakcí přesahuje aktuální stav pokladny.
     */
    @Transactional
    public int deleteAllTransactions() {

        // Získání všech transakcí, které nebyly smazány
        List<Transaction> transactions = transactionRepository.findByDeletedFalse();
        int deletedCount = 0;

        // Výpočet součtu hodnoty transakcí
        int totalTransactionAmount = transactions.stream().mapToInt(Transaction::getAmount).sum();

        // Získání posledního záznamu z finanční transakce
        Transaction lastTransaction = transactionRepository.findLastTransaction();

        // Kontrola, zda součet hodnoty transakcí nepřesahuje balance after
        int balanceAfter = 0;
        if (lastTransaction != null) {
            balanceAfter = lastTransaction.getBalanceAfter();
        }

        if (totalTransactionAmount > balanceAfter) {
            throw new IllegalStateException("Operace odstranění není možná, protože není dostatek peněz v pokladně.");
        }

        // Nejdrive odstranim ostatni transakce aby byl dostatek penez na odstraneni trzeb
        List<Transaction> saleTransactions = new ArrayList<>();
        List<Transaction> otherTransactions = new ArrayList<>();


        for (Transaction transaction : transactions) {
            if (transaction.getSale() != null) {
                saleTransactions.add(transaction);
            } else {
                otherTransactions.add(transaction);
            }
        }


        // nejdrive smazu ostatni transakce
        for (Transaction transactionToDelete : otherTransactions) {
            // Kontrola, zda transakce patří k prodeji
            if (transactionToDelete.getSale() != null) {
                // Pokud transakce patří k prodeji, použij metodu pro odstranění tržby z prodeje
                saleService.deleteSaleById(transactionToDelete.getSale().getIdSale());
                deletedCount++;
            } else {

                // Nastavení sloupce deleted na true
                transactionToDelete.setDeleted(true);
                transactionRepository.save(transactionToDelete);

                int balanceBefore = calculateBalanceBefore();
                int balanceAfterCurrent = balanceBefore - transactionToDelete.getAmount();


                int idCurrentTransaction = transactionToDelete.getIdTransaction();

                // Vytvoření nové transakce, která neguje původní transakci
                Transaction repairTransaction = new Transaction();
                String originalDescription = transactionToDelete.getDescription();
                repairTransaction.setDescription("Zrušení transakce ID: " + idCurrentTransaction + " - " + originalDescription);
                repairTransaction.setAmount(-transactionToDelete.getAmount());
                repairTransaction.setBalanceBefore(balanceBefore);
                repairTransaction.setBalanceAfter(balanceAfterCurrent);
                repairTransaction.setSale(null); // Nepřiřazujeme k žádnému prodeji
                repairTransaction.setUser(transactionToDelete.getUser());
                repairTransaction.setDeleted(true);

                // Uložení nové transakce
                transactionRepository.save(repairTransaction);
                deletedCount++;
            }

        }

        // nasledne smazu transakce s trzbami
        for (Transaction transactionToDelete : saleTransactions) {
            // Kontrola, zda transakce patří k prodeji
            if (transactionToDelete.getSale() != null) {
                // Pokud transakce patří k prodeji, použij metodu pro odstranění tržby z prodeje
                saleService.deleteSaleById(transactionToDelete.getSale().getIdSale());
                deletedCount++;
            } else {

                // Nastavení sloupce deleted na true
                transactionToDelete.setDeleted(true);
                transactionRepository.save(transactionToDelete);

                int balanceBefore = calculateBalanceBefore();
                int balanceAfterCurrent = balanceBefore - transactionToDelete.getAmount();


                int idCurrentTransaction = transactionToDelete.getIdTransaction();

                // Vytvoření nové transakce, která neguje původní transakci
                Transaction repairTransaction = new Transaction();
                String originalDescription = transactionToDelete.getDescription();
                repairTransaction.setDescription("Zrušení transakce ID: " + idCurrentTransaction + " - " + originalDescription);
                repairTransaction.setAmount(-transactionToDelete.getAmount());
                repairTransaction.setBalanceBefore(balanceBefore);
                repairTransaction.setBalanceAfter(balanceAfterCurrent);
                repairTransaction.setSale(null); // Nepřiřazujeme k žádnému prodeji
                repairTransaction.setUser(transactionToDelete.getUser());
                repairTransaction.setDeleted(true);

                // Uložení nové transakce
                transactionRepository.save(repairTransaction);
                deletedCount++;
            }

        }

        return deletedCount;
    }

    /**
     * Metoda pro odebrání ID uživatele z transakcí.
     *
     * @param userId ID uživatele k odebrání
     */
    @Transactional
    public void removeUserIdFromTransactions(int userId) {
        List<Transaction> transactions = transactionRepository.findByUser_IdUser(userId);
        for (Transaction transaction : transactions) {
            transaction.setUser(null); // Nastaví user_id na null
        }
        transactionRepository.saveAll(transactions);
    }

    /**
     * Metoda pro výpočet zůstatku před provedením transakce.
     *
     * @return zůstatek před transakcí
     */
    public int calculateBalanceBefore() {
        Transaction lastTransaction = transactionRepository.findLastTransaction();
        int balanceBefore = 0;
        if (lastTransaction != null) {
            balanceBefore = lastTransaction.getBalanceAfter();
        }
        return balanceBefore;
    }

    /**
     * Metoda pro získání prvních 20 transakcí seřazených sestupně podle ID transakce.
     *
     * @return Seznam prvních 20 transakcí seřazených sestupně podle ID transakce.
     */
    public List<Transaction> getFirst20Transactions() {
        // Vytvoření instance Pageable s požadovaným počtem transakcí
        Pageable pageable = PageRequest.of(0, 20, Sort.by("idTransaction").descending());
        // Získání prvních 20 transakcí
        return transactionRepository.findFirst20Transactions(pageable);
    }

    /**
     * Metoda pro získání prvních 20 transakcí seřazených vzestupně podle ID transakce.
     *
     * @return Seznam prvních 20 transakcí seřazených vzestupně podle ID transakce.
     */
    public List<Transaction> getFirst20TransactionsAscending() {
        // Vytvoření instance Pageable s požadovaným počtem transakcí
        Pageable pageable = PageRequest.of(0, 20, Sort.by("idTransaction").ascending());
        // Získání prvních 20 transakcí

        return transactionRepository.findFirst20Transactions(pageable);
    }
}
