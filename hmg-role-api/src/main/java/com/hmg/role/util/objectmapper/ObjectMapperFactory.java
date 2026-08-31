package com.hmg.role.util.objectmapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectMapperFactory {
    private static ObjectMapper instance;

    /**
     * Get the ObjectMapper instance as a bean.
     *
     * @return the {@link ObjectMapper} instance
     */
    @Bean
    public ObjectMapper objectMapper() {
        return getObjectMapper();
    }

    /**
     * Get the ObjectMapper instance through a static method. Useful for static util classes.
     *
     * @return the {@link ObjectMapper} instance
     */
    public static synchronized ObjectMapper getObjectMapper() {
        // static class and method providers
        if (instance == null) {
            createInstance();
        }

        return instance;
    }

    /** Lazily create the default instance */
    private static synchronized void createInstance() {
        instance =
                (new ObjectMapper())
                        // ensures JavaTimeModule is active
                        .findAndRegisterModules();

        // write dates as ISO timestamp instead of epoch
        instance.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
