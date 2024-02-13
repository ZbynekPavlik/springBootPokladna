package pavlik.pokladna;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pavlik.pokladna.entity.Sale;
import pavlik.pokladna.entity.User;
import pavlik.pokladna.repository.SaleRepositoryInterface;
import pavlik.pokladna.repository.UserRepositoryInterface;
import pavlik.pokladna.service.SaleService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PokladnaApplicationTests {

	@Test
	void contextLoads() {
	}

	@Autowired
	private SaleService saleService;

	@Autowired
	private SaleRepositoryInterface saleRepository;

	@Autowired
	private UserRepositoryInterface userRepository;

	@Test
	public void testAddSaleWithTransaction() {

		Optional<User> optionalUser = userRepository.findById(1);
		User user = optionalUser.orElseThrow(() -> new RuntimeException("Uživatel nebyl nalezen"));
		// Vytvoření testovacího prodeje
		Sale testSale = new Sale(-1000, "prkno", user);

		// Volání metody z SaleService
		Sale savedSale = saleService.addSaleWithTransaction(testSale);

		// Zde můžete prověřit očekávané změny v databázi nebo vrácené hodnoty
		// Například: Sale by mělo být úspěšně přidáno, a transakce by měly být správně nastaveny.

		// Můžete také použít assert pro ověření očekávaných hodnot.
		assertNotNull(savedSale);
		assertEquals(-1000, savedSale.getAmount());

		// Další asserty a ověření podle potřeby.
	}

//	@Test
//	@Transactional
//	public void testDeleteSaleById() {
//
//		Optional<User> optionalUser = userRepository.findById(1);
//		User user = optionalUser.orElseThrow(() -> new RuntimeException("Uživatel nebyl nalezen"));
//		// Vytvoření testovací tržby
//		Sale testSale = new Sale();
//		testSale.setAmount(100);
//		testSale.setSoldGoods("Testovací zboží");
//		testSale.setUser(user);
//
//		Sale savedSale = saleRepository.save(testSale);
//
//		// Zavolání metody pro smazání tržby
//		saleService.deleteSaleById(savedSale.getIdSale());
//
//	}

}
