package pavlik.pokladna.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pavlik.pokladna.entity.Transaction;

import java.util.List;

/**
 * Rozhraní pro přístup k datům transakcí v databázi.
 */
@Repository
public interface TransactionRepositoryInterface extends JpaRepository<Transaction, Integer> {
    /**
     * Metoda pro nalezení poslední transakce.
     *
     * @return Poslední transakce.
     */
    @Query(value = "SELECT * FROM financialTransaction ORDER BY id_transaction DESC LIMIT 1", nativeQuery = true)
    Transaction findLastTransaction();

    /**
     * Metoda pro nalezení transakce podle ID prodeje.
     *
     * @param saleId ID prodeje.
     * @return Transakce spojená s daným prodejem.
     */
    @Query("SELECT t FROM Transaction t JOIN t.sale s WHERE s.idSale = :saleId")
    Transaction findBySaleId(@Param("saleId") Integer saleId);

    /**
     * Metoda pro nalezení nevymazaných transakcí.
     *
     * @return Seznam nevymazaných transakcí.
     */
    List<Transaction> findByDeletedFalse();

    /**
     * Metoda pro nalezení transakcí prováděných daným uživatelem.
     *
     * @param userId ID uživatele.
     * @return Seznam transakcí prováděných daným uživatelem.
     */
    List<Transaction> findByUser_IdUser(Integer userId);

    /**
     * Metoda pro aktualizaci transakcí, které mají nulové ID uživatele.
     */
    @Transactional
    @Modifying
    @Query("UPDATE Transaction t SET t.user = null WHERE t.user.idUser IS NOT NULL")
    void updateTransactionsWithNullUserId();

    /**
     * Metoda pro získání prvních 20 transakcí seřazených podle id transakce sestupně.
     *
     * @param pageable Objekt Pageable pro stránkování výsledků.
     * @return Seznam prvních 20 transakcí seřazených podle id transakce sestupně.
     */
    @Query("SELECT t FROM Transaction t ORDER BY t.idTransaction DESC")
    List<Transaction> findFirst20Transactions(Pageable pageable);
}
