package pavlik.pokladna.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pavlik.pokladna.entity.Authority;
import pavlik.pokladna.entity.User;

import java.util.List;

/**
 * Rozhraní pro přístup k datům autorit uživatelů v databázi.
 */
@Repository
public interface AuthorityRepositoryInterface extends JpaRepository<Authority, Integer> {
    /**
     * Metoda pro nalezení autorit uživatele.
     *
     * @param existingUser Existující uživatel, jehož autority hledáme.
     * @return Seznam autorit daného uživatele.
     */
    List<Authority> findByUser(User existingUser);

    /**
     * Metoda pro nalezení autorit uživatele podle uživatelského jména.
     *
     * @param username Uživatelské jméno uživatele.
     * @return Seznam autorit daného uživatele.
     */
    List<Authority> findByUserUsername(String username);

    /**
     * Metoda pro smazání autorit uživatele podle uživatelského jména.
     *
     * @param username Uživatelské jméno uživatele, jehož autority chceme smazat.
     */
    void deleteByUserUsername(String username);

    /**
     * Metoda pro smazání autorit uživatele podle uživatelského jména.
     *
     * @param username Uživatelské jméno uživatele, jehož autority chceme smazat.
     */
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM Authority a WHERE a.user.username = :username")
    void deleteByUsername(@Param("username") String username);

}
