package pavlik.pokladna.entity;

import jakarta.persistence.*;

import java.util.Set;

/**
 * Entitní třída reprezentující uživatele v systému.
 */
@Entity
@Table(name = "users", schema = "public")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Integer idUser;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "enabled")
    private short enabled;

    @Column(name = "role")
    private String role;

    @OneToMany(mappedBy = "user")
    private Set<Sale> sales;

    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<Transaction> transactions;

    /**
     * Výchozí konstruktor pro entitu User.
     */
    public User() {

    }

    /**
     * Konstruktor pro vytvoření uživatele s určitými vlastnostmi.
     *
     * @param username Uživatelské jméno.
     * @param password Heslo uživatele.
     * @param enabled  Příznak, zda je uživatel povolený.
     * @param role     Role uživatele v systému.
     */
    public User(String username, String password, short enabled, String role) {
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.role = role;
    }

    public Integer getIdUser() {
        return idUser;
    }

    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public short getEnabled() {
        return enabled;
    }

    public void setEnabled(short enabled) {
        this.enabled = enabled;
    }

    public Set<Sale> getSales() {
        return sales;
    }

    public void setSales(Set<Sale> sales) {
        this.sales = sales;
    }

    public Set<Transaction> getFinancialTransactions() {
        return transactions;
    }

    public void setFinancialTransactions(Set<Transaction> financialTransactions) {
        this.transactions = financialTransactions;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
