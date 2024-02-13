package pavlik.pokladna.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pavlik.pokladna.entity.User;

import java.util.Optional;

/**
 * Rozhraní pro přístup k datům uživatelů v databázi.
 */
@Repository
public interface UserRepositoryInterface extends JpaRepository<User, Integer> {

    /**
     * Metoda pro nalezení uživatele podle uživatelského jména.
     *
     * @param username Uživatelské jméno uživatele.
     * @return Optional obsahující nalezeného uživatele nebo prázdný Optional, pokud uživatel nebyl nalezen.
     */
    Optional<User> findByUsername(String username);

}
