package ma.mlia.banque.config;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import ma.mlia.banque.entities.Account;
import ma.mlia.banque.entities.AccountType;
import ma.mlia.banque.repositories.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Pre-fills a few accounts at startup so the API can be tested right away.
 */
@Configuration
@Profile("!test")
public class DataInitializer {

    @Bean
    CommandLineRunner start(AccountRepository accountRepository) {
        return args -> {
            if (accountRepository.count() > 0) {
                return;
            }

            accountRepository.save(new Account(null, 1200.50, today(), AccountType.SAVINGS));
            accountRepository.save(new Account(null, 4300.00, today(), AccountType.CURRENT));
            accountRepository.save(new Account(null, 8600.75, today(), AccountType.SAVINGS));

            accountRepository.findAll().forEach(System.out::println);
        };
    }

    private static Date today() {
        return Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
