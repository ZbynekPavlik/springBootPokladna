package pavlik.pokladna.entity;

import jakarta.persistence.*;

/**
 * Entitní třída reprezentující prodej.
 */
@Entity
@Table(name = "sales", schema = "public")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sale")
    private Integer idSale;

    @Column(name = "amount")
    private Integer amount;

    @Column(name = "sold_goods")
    private String soldGoods;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Výchozí konstruktor pro entitu Sale.
     */
    public Sale() {

    }

    /**
     * Konstruktor pro vytvoření prodeje s určitými vlastnostmi.
     *
     * @param amount    Částka prodeje.
     * @param soldGoods Prodávané zboží.
     * @param user      Uživatel provádějící prodej.
     */
    public Sale(Integer amount, String soldGoods, User user) {
        this.amount = amount;
        this.soldGoods = soldGoods;
        this.user = user;
    }

    public Integer getIdSale() {
        return idSale;
    }

    public void setIdSale(Integer idSale) {
        this.idSale = idSale;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getSoldGoods() {
        return soldGoods;
    }

    public void setSoldGoods(String soldGoods) {
        this.soldGoods = soldGoods;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
