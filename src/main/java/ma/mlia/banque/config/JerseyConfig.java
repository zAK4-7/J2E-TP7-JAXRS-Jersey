package ma.mlia.banque.config;

import ma.mlia.banque.web.AccountRestJaxRsApi;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the JAX-RS resource so Jersey routes the HTTP requests to it.
 */
@Configuration
public class JerseyConfig {

    @Bean
    public ResourceConfig resourceConfig() {
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(AccountRestJaxRsApi.class);
        return resourceConfig;
    }
}
