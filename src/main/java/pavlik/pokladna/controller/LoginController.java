package pavlik.pokladna.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller pro zobrazení stránek pro přihlášení a odmítnutí přístupu.
 */
@Controller
@RequestMapping("authentication")
public class LoginController {

    /**
     * Metoda pro zobrazení stránky pro přihlášení.
     *
     * @return Název šablony pro přihlášení.
     */
    @GetMapping("/login")
    public String showLogin() {
        return "authentication/login";
    }

    /**
     * Metoda pro zobrazení stránky pro odmítnutí přístupu.
     *
     * @return Název šablony pro odmítnutí přístupu.
     */
    @GetMapping("/accesDenied")
    public String showAccesDenied() {
        return "authentication/accesDenied";
    }
}
