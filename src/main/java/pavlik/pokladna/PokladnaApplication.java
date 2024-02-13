package pavlik.pokladna;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hlavní třída aplikace Pokladna. Tato třída obsahuje metodu {@link #main(String[])},
 * která spouští Spring Boot aplikaci. Popis projektu je k dispozici v souboru README.md.
 *
 *  @author Zbyněk_Pavlík
 *  @version 1.0
 */
@SpringBootApplication(scanBasePackages = "pavlik.pokladna")
public class PokladnaApplication {

    /**
     * Metoda pro spuštění Spring Boot aplikace.
     *
     * @param args Argumenty příkazové řádky.
     */
    public static void main(String[] args) {
        SpringApplication.run(PokladnaApplication.class, args);
    }

}
