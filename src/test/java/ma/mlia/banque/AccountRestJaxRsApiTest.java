package ma.mlia.banque;

import java.util.Date;
import java.util.List;
import ma.mlia.banque.entities.Account;
import ma.mlia.banque.entities.AccountType;
import ma.mlia.banque.repositories.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end tests of the JAX-RS resource.
 * The application starts on a random port and the endpoints are called over
 * HTTP, checking both the JSON and the XML representations.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AccountRestJaxRsApiTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void cleanDatabase() {
        accountRepository.deleteAll();
    }

    private Account newAccount(double balance, AccountType type) {
        return new Account(null, balance, new Date(), type);
    }

    @Test
    void getAccountsShouldReturnJson() {
        accountRepository.save(newAccount(1500.0, AccountType.CURRENT));

        ResponseEntity<String> response = restTemplate.getForEntity("/banque/comptes", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("CURRENT"));
    }

    @Test
    void getAccountsShouldReturnXmlWhenRequested() {
        accountRepository.save(newAccount(2500.0, AccountType.SAVINGS));

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_XML));

        ResponseEntity<String> response = restTemplate.exchange(
                "/banque/comptes", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("<accounts>") || response.getBody().contains("<account>"),
                "The XML body must contain an account element, got: " + response.getBody());
    }

    @Test
    void postShouldCreateAnAccount() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String payload = "{\"balance\":800.0,\"creationDate\":\"2024-11-01\",\"type\":\"SAVINGS\"}";

        ResponseEntity<Account> response = restTemplate.postForEntity(
                "/banque/comptes", new HttpEntity<>(payload, headers), Account.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().getId());
        assertEquals(1, accountRepository.count());
    }

    @Test
    void getAccountShouldReturnNotFoundForAnUnknownId() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/banque/comptes/{id}", String.class, 9999);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void putShouldUpdateAnExistingAccount() {
        Account saved = accountRepository.save(newAccount(100.0, AccountType.CURRENT));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String payload = "{\"balance\":999.0,\"creationDate\":\"2024-11-01\",\"type\":\"SAVINGS\"}";

        restTemplate.exchange("/banque/comptes/{id}", HttpMethod.PUT,
                new HttpEntity<>(payload, headers), Account.class, saved.getId());

        assertEquals(999.0, accountRepository.findById(saved.getId()).orElseThrow().getBalance());
    }

    @Test
    void deleteShouldRemoveTheAccount() {
        Account saved = accountRepository.save(newAccount(50.0, AccountType.CURRENT));

        restTemplate.delete("/banque/comptes/{id}", saved.getId());

        assertEquals(0, accountRepository.count());
    }

    @Test
    void statsShouldAggregateTheBalances() {
        accountRepository.save(newAccount(100.0, AccountType.CURRENT));
        accountRepository.save(newAccount(200.0, AccountType.SAVINGS));

        ResponseEntity<String> response =
                restTemplate.getForEntity("/banque/comptes/stats", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("300"), "Total balance should be 300");
    }
}
