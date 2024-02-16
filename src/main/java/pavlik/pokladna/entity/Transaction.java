package pavlik.pokladna.entity;

import jakarta.persistence.*;

/**
 * Entitní třída reprezentující finanční transakci.
 */
@Entity
@Table(name = "financialtransaction", schema = "public")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transaction")
    private Integer idTransaction;

    @Column(name = "description")
    private String description;

    @Column(name = "amount")
    private Integer amount;

    @Column(name = "balance_before")
    private Integer balanceBefore;

    @Column(name = "balance_after")
    private Integer balanceAfter;

    @Column(name = "deleted") // Nový sloupec pro označení, zda je transakce smazána
    private boolean deleted; // Může být true nebo false

    @ManyToOne
    @JoinColumn(name = "sale_id", nullable = true)
    private Sale sale;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    /**
     * Výchozí konstruktor pro entitu Transaction.
     */
    public Transaction() {
        deleted = false;
    }

    /**
     * Konstruktor pro vytvoření finanční transakce s určitými vlastnostmi.
     *
     * @param description   Popis transakce.
     * @param amount        Částka transakce.
     * @param balanceBefore Zůstatek před transakcí.
     * @param balanceAfter  Zůstatek po transakci.
     * @param sale          Prodej spojený s transakcí.
     * @param user          Uživatel provádějící transakci.
     */
    public Transaction(String description, Integer amount, Integer balanceBefore, Integer balanceAfter, Sale sale, User user) {
        this.description = description;
        this.amount = amount;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.sale = sale;
        this.user = user;
        deleted = false;
    }


    public Integer getIdTransaction() {
        return idTransaction;
    }

    public void setIdTransaction(Integer idTransaction) {
        this.idTransaction = idTransaction;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getBalanceBefore() {
        return balanceBefore;
    }

    public void setBalanceBefore(Integer balanceBefore) {
        this.balanceBefore = balanceBefore;
    }

    public Integer getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(Integer balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public Sale getSale() {
        return sale;
    }

    public void setSale(Sale sale) {
        this.sale = sale;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
