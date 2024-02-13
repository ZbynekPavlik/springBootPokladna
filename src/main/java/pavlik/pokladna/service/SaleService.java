package pavlik.pokladna.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pavlik.pokladna.entity.Sale;
import pavlik.pokladna.entity.Transaction;
import pavlik.pokladna.repository.SaleRepositoryInterface;
import pavlik.pokladna.repository.TransactionRepositoryInterface;

import java.util.List;

/**
 * Služba pro manipulaci s tržbami a transakcemi.
 */
@Service
public class SaleService {
    private final SaleRepositoryInterface saleRepository;
    private final TransactionRepositoryInterface transactionRepository;

    private final UserService userService;


    /**
     * Konstruktor pro SaleService.
     *
     * @param saleRepository        Repozitář pro tržby.
     * @param transactionRepository Repozitář pro transakce.
     * @param userService           Služba pro správu uživatelů.
     */
    @Autowired
    public SaleService(SaleRepositoryInterface saleRepository, TransactionRepositoryInterface transactionRepository, UserService userService) {
        this.saleRepository = saleRepository;
        this.transactionRepository = transactionRepository;
        this.userService = userService;

    }

    /**
     * Přidá tržbu s odpovídající transakcí.
     *
     * @param sale Přidávaná tržba.
     * @return Přidaná tržba.
     */
    public Sale addSaleWithTransaction(Sale sale) {
        // Přidání záznamu do tabulky 'Sale'
        Sale savedSale = saleRepository.save(sale);

        // nastaveni balance Before z posledniho zaznanu z financialtransaction ze sloupce balance after
        Transaction lastTransaction = transactionRepository.findLastTransaction();
        int balanceBefore = 0;
        if (lastTransaction != null) {
            balanceBefore = lastTransaction.getBalanceAfter();
        }

        // vypocet balance after

        int balanceAfter = balanceBefore + sale.getAmount();


        // Vytvoření záznamu pro tabulku 'Transaction'


        Transaction transaction = new Transaction();
        transaction.setDescription("Nová tržba - prodané zboží: " + sale.getSoldGoods());
        transaction.setAmount(sale.getAmount());
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setSale(savedSale);
        transaction.setUser(sale.getUser());
        transaction.setDeleted(false);

        // Přidání záznamu do tabulky 'Transaction'
        transactionRepository.save(transaction);

        return savedSale;
    }


    /**
     * Odstraní tržbu a odpovídající transakci podle zadaného ID tržby.
     *
     * @param saleID ID tržby k odstranění.
     */
    @Transactional
    public void deleteSaleById(int saleID) {
        // Ziskani trzby pro zruseni
        Sale sale = saleRepository.findById(saleID)
                .orElseThrow(() -> new EntityNotFoundException("Tržba s ID " + saleID + " nebyla nalezena."));

        Transaction transactionInDatabase = transactionRepository.findBySaleId(saleID);
        if (!transactionInDatabase.isDeleted()) { //musi byt true pro fungovani, neprisel jsem na to proc
            if (transactionInDatabase.getSale() != null)
                transactionInDatabase.setSale(null);

            if (!transactionInDatabase.isDeleted())
                transactionInDatabase.setDeleted(true);

            // nastaveni balance Before z posledniho zaznanu z financialtransaction ze sloupce balance after
            Transaction lastTransaction = transactionRepository.findLastTransaction();
            int balanceBefore = 0;
            if (lastTransaction != null) {
                balanceBefore = lastTransaction.getBalanceAfter();
            }

            // vypocet balance after

            int balanceAfter = balanceBefore + (-sale.getAmount());

            // Kontrola, zda balanceAfter není záporná
            if (balanceAfter < 0) {
                throw new IllegalStateException("Operace odstranění není možná, protože není dostatek peněz v pokladně.");
            }


            String newDescription = "(smazáno) " + transactionInDatabase.getDescription();
            transactionInDatabase.setDescription(newDescription);
            transactionInDatabase.setSale(null);

            int idTransactionInDatabase = transactionInDatabase.getIdTransaction();

            Transaction deleteTransaction = new Transaction();
            deleteTransaction.setDescription("Zrušená transankce ID: " + idTransactionInDatabase + " Tržba - " + sale.getSoldGoods());
            deleteTransaction.setAmount(-sale.getAmount()); // Záporná hodnota pro zrušení
            deleteTransaction.setBalanceBefore(balanceBefore);
            deleteTransaction.setBalanceAfter(balanceAfter); // Odečtení hodnoty tržby
            deleteTransaction.setSale(null);
            deleteTransaction.setUser(userService.getCurrentUser());
            deleteTransaction.setDeleted(true);

            // ulozeni transakci
            transactionRepository.save(transactionInDatabase);
            transactionRepository.save(deleteTransaction);
        }


        // zruseni trzby
        saleRepository.delete(sale);

    }

    /**
     * Odstraní všechny tržby a odpovídající transakce.
     *
     * @return Počet odstraněných tržeb.
     */
    @Transactional
    public int deleteAllSales() {
        // Získání všech tržeb pro zrušení
        List<Sale> allSales = saleRepository.findAll();
        int deletedCount = 0;

        // kontrola součtu hodnoty tržeb
        int totalSaleAmount = allSales.stream().mapToInt(Sale::getAmount).sum();

        // Ziskání posledního záznamu z financial transaction
        Transaction lastTransaction = transactionRepository.findLastTransaction();

        // kontrola, zda součet hodnoty tržeb nepřesahuje balance after
        int balanceAfter = 0;
        if (lastTransaction != null) {
            balanceAfter = lastTransaction.getBalanceAfter();
        }

        if (totalSaleAmount > balanceAfter) {
            throw new IllegalStateException("Operace odstranění není možná, protože není dostatek peněz v pokladně.");
        }

        for (Sale sale : allSales) {

            deletedCount++;
            // Ziskani trzby pro zruseni
            int currentSaleId = sale.getIdSale();
            Transaction transactionInDatabase = transactionRepository.findBySaleId(currentSaleId);
            transactionInDatabase.setSale(null);
            transactionInDatabase.setDeleted(true);

            // Ziskání posledního záznamu z financial transaction
            Transaction lastTransactionLoop = transactionRepository.findLastTransaction();

            // nastaveni balance Before z posledniho zaznanu z financialtransaction ze sloupce balance after
            int balanceBefore = 0;
            if (lastTransactionLoop != null) {
                balanceBefore = lastTransactionLoop.getBalanceAfter();
            }

            // vypocet balance after
            int balanceAfterSale = balanceBefore + (-sale.getAmount());

            // Kontrola, zda balanceAfterSale není záporná
            if (balanceAfterSale < 0) {
                throw new IllegalStateException("Operace odstranění není možná, protože není dostatek peněz v pokladně.");
            }


            String newDescription = "(smazáno) " + transactionInDatabase.getDescription();
            transactionInDatabase.setDescription(newDescription);

            int idTransactionInDatabase = transactionInDatabase.getIdTransaction();

            Transaction deleteTransaction = new Transaction();
            deleteTransaction.setDescription("Zrušená transankce ID: " + idTransactionInDatabase + " Tržba - " + sale.getSoldGoods());

            deleteTransaction.setAmount(-sale.getAmount()); // Záporná hodnota pro zrušení
            deleteTransaction.setBalanceBefore(balanceBefore);
            deleteTransaction.setBalanceAfter(balanceAfterSale); // Odečtení hodnoty tržby
            deleteTransaction.setSale(null);
            deleteTransaction.setUser(userService.getCurrentUser());
            deleteTransaction.setDeleted(true);

            // ulozeni transakci
            transactionRepository.save(transactionInDatabase);
            transactionRepository.save(deleteTransaction);
        }

        // zruseni vsech trzeb
        saleRepository.deleteAll(allSales);
        return deletedCount;
    }

    /**
     * Odebere uživatelské ID ze všech tržeb.
     *
     * @param userId ID uživatele.
     */
    @Transactional
    public void removeUserIdFromSales(int userId) {
        List<Sale> sales = saleRepository.findByUser_IdUser(userId);
        for (Sale sale : sales) {
            sale.setUser(null); // Nastaví user_id na null
        }
        saleRepository.saveAll(sales);
    }


}


