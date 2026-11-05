# J2E-TP7 — JAX-RS / Jersey

Implémentation du **TP 7** du cours *Architecture Microservices : Conception, Déploiement et Orchestration* (MLIAEdu).

API REST de gestion de comptes bancaires exposée via **JAX-RS** avec **Jersey 3**
au-dessus de **Spring Boot 3**, capable de répondre en **JSON et en XML**.

> Le code, les commentaires et les noms enregistrés en base (table `accounts`, colonnes
> `balance` / `creation_date` / `type`) sont en **anglais**. Seule cette documentation est en français.

## Objectifs couverts

| Étape du TP | Où |
|---|---|
| 1. Projet Spring Initializr | `pom.xml` (web, data-jpa, h2, jersey, jaxb, lombok, devtools) |
| 2. Configuration H2 | `src/main/resources/application.properties` |
| 3. Entité JPA | `entities/Account.java` |
| 4. Énumération `TypeCompte` | `entities/AccountType.java` |
| 5. Repository JPA | `repositories/AccountRepository.java` |
| 6. Initialisation des données | `config/DataInitializer.java` (`CommandLineRunner`) |
| 7. Configuration Jersey | `config/JerseyConfig.java` (`ResourceConfig`) |
| 8. Services REST CRUD | `web/AccountRestJaxRsApi.java` |
| 9. Support JSON + XML | `@Produces` / `@Consumes` + dépendances JAXB |
| 10. `@XmlRootElement` | `entities/Account.java` |

## Structure

```
J2E-TP7-JAXRS-Jersey/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/ma/mlia/banque/
    │   │   ├── MsBanqueApplication.java
    │   │   ├── entities/
    │   │   │   ├── Account.java          # @Entity + @XmlRootElement
    │   │   │   └── AccountType.java      # CURRENT, SAVINGS
    │   │   ├── repositories/AccountRepository.java
    │   │   ├── web/
    │   │   │   ├── AccountRestJaxRsApi.java  # @Path("/banque"), @GET/@POST/@PUT/@DELETE
    │   │   │   └── Stats.java               # DTO @XmlRootElement
    │   │   └── config/
    │   │       ├── JerseyConfig.java        # ResourceConfig
    │   │       └── DataInitializer.java
    │   └── resources/application.properties # H2 + port 8082
    └── test/
        ├── java/ma/mlia/banque/AccountRestJaxRsApiTest.java
        └── resources/application-test.properties
```

## Modèle de données

```
accounts
--------
id             BIGINT (PK, auto-increment)
balance        DOUBLE
creation_date  DATE
type           VARCHAR(255)  -- CURRENT | SAVINGS
```

`@Enumerated(EnumType.STRING)` est utilisé pour stocker le libellé du type plutôt que son
ordinal : la base reste lisible et insensible à un réordonnancement de l'énumération.

## Prérequis

- JDK 17+
- Maven 3.8+
- Aucune base externe : **H2 en mémoire**

## Exécution

```bash
mvn spring-boot:run     # http://localhost:8082
mvn test
```

- Console H2 : <http://localhost:8082/h2-console>
  (JDBC URL `jdbc:h2:mem:banque`, user `sa`, mot de passe vide)

## Endpoints JAX-RS

| Méthode | URL | Statut | Description |
|---|---|---|---|
| GET | `/banque/comptes` | 200 | Tous les comptes (JSON ou XML) |
| GET | `/banque/comptes/{id}` | 200 / 404 | Un compte |
| POST | `/banque/comptes` | 201 | Créer un compte |
| PUT | `/banque/comptes/{id}` | 200 / 404 | Mettre à jour un compte |
| DELETE | `/banque/comptes/{id}` | 204 / 404 | Supprimer un compte |
| GET | `/banque/comptes/stats` | 200 | Nombre de comptes et solde total |

Chaque méthode porte `@Produces({APPLICATION_JSON, APPLICATION_XML})`, et les méthodes
d'écriture `@Consumes({...})` : le format dépend uniquement des en-têtes `Accept` / `Content-Type`.

### Exemples

```bash
# JSON
curl -X GET "http://localhost:8082/banque/comptes" -H "Accept: application/json"

# XML
curl -X GET "http://localhost:8082/banque/comptes" -H "Accept: application/xml"
# -> <accounts><account><id>1</id><balance>1200.5</balance>...</account></accounts>

# CREATE (JSON)
curl -X POST "http://localhost:8082/banque/comptes" \
  -H "Content-Type: application/json" \
  -d '{"balance":1500.0,"creationDate":"2024-11-01","type":"CURRENT"}'

# CREATE (XML)
curl -X POST "http://localhost:8082/banque/comptes" \
  -H "Content-Type: application/xml" \
  -d '<account><balance>1500.0</balance><creationDate>2024-11-01</creationDate><type>CURRENT</type></account>'

# UPDATE
curl -X PUT "http://localhost:8082/banque/comptes/1" \
  -H "Content-Type: application/json" \
  -d '{"balance":2000.0,"creationDate":"2024-11-01","type":"SAVINGS"}'

# DELETE
curl -X DELETE "http://localhost:8082/banque/comptes/1"
```

## Comment Jersey est branché sur Spring Boot

```java
@Configuration
public class JerseyConfig {
    @Bean
    public ResourceConfig resourceConfig() {
        ResourceConfig config = new ResourceConfig();
        config.register(AccountRestJaxRsApi.class);
        return config;
    }
}
```

`ResourceConfig` enregistre la classe ressource ; `spring-boot-starter-jersey` expose le
`ServletContainer` de Jersey et fait le pont avec le contexte Spring (la ressource est un
`@Component`, donc elle reçoit son repository par injection de constructeur).

Côté XML, trois éléments sont nécessaires :

1. `@XmlRootElement` sur les classes sérialisées (`Account`, `Stats`)
2. les dépendances `jakarta.xml.bind-api` + `jaxb-runtime` (JAXB a été retiré du JDK depuis Java 11)
3. le module `jersey-media-jaxb`

## Tests

`AccountRestJaxRsApiTest` démarre l'application sur un port aléatoire et appelle réellement
les endpoints HTTP :

| Test | Vérifie |
|---|---|
| `getAccountsShouldReturnJson` | corps JSON contenant `CURRENT` |
| `getAccountsShouldReturnXmlWhenRequested` | corps XML (`<accounts>` / `<account>`) |
| `postShouldCreateAnAccount` | 201 + identifiant généré |
| `getAccountShouldReturnNotFoundForAnUnknownId` | 404 |
| `putShouldUpdateAnExistingAccount` | solde mis à jour en base |
| `deleteShouldRemoveTheAccount` | table vidée |
| `statsShouldAggregateTheBalances` | solde total agrégé |

```
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
```
