package pavlik.pokladna.entity;


import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Entitní třída reprezentující oprávnění uživatele.
 */
@Entity
@Table(name = "authorities", schema = "public")
public class Authority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_authority")
    private Integer idAuthority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username", referencedColumnName = "username", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "authority")
    private String authority;

    // Getters and setters

    public Integer getIdAuthority() {
        return idAuthority;
    }

    public void setIdAuthority(Integer idAuthority) {
        this.idAuthority = idAuthority;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}
