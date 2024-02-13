package pavlik.pokladna.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pavlik.pokladna.entity.Sale;

import java.util.List;


/**
 * Rozhraní pro přístup k datům prodejů v databázi.
 */
@Repository
public interface SaleRepositoryInterface extends JpaRepository<Sale, Integer> {
    /**
     * Metoda pro nalezení prodejů prováděných daným uživatelem.
     *
     * @param userId ID uživatele.
     * @return Seznam prodejů prováděných daným uživatelem.
     */
    List<Sale> findByUser_IdUser(Integer userId);

    /**
     * Metoda pro aktualizaci prodejů, které mají nulové ID uživatele.
     */
    @Transactional
    @Modifying
    @Query("UPDATE Sale s SET s.user = null WHERE s.user.idUser IS NOT NULL")
    void updateSalesWithNullUserId();
}
