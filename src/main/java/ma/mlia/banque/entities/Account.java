package ma.mlia.banque.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bank account persisted in the H2 database.
 * {@code @XmlRootElement} lets JAXB render the account as the root of an XML document.
 */
@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement
public class Account implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double balance;

    @Temporal(TemporalType.DATE)
    private Date creationDate;

    // Stored as a string so the database stays readable
    @Enumerated(EnumType.STRING)
    private AccountType type;
}
