package ma.mlia.banque.repositories;

import ma.mlia.banque.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository used by the JAX-RS resources.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
}
