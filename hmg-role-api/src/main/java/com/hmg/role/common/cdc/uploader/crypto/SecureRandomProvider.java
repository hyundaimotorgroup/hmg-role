package com.hmg.role.common.cdc.uploader.crypto;

import java.security.DrbgParameters;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecureRandomProvider {
    /**
     * Get a secure random generator for cryptographic purposes. Use Deterministic Random Bit
     * Generator if possible as recommended by NIST per JCA implementation
     *
     * @return a secure random generator
     */
    @Bean
    public SecureRandom secureRandom() {
        try {
            // Request a strong DRBG with 256-bit strength; reseed as needed.
            // (RESEED_ONLY avoids prediction resistance overhead unless requested.)
            var params =
                    DrbgParameters.instantiation(
                            256,
                            DrbgParameters.Capability.RESEED_ONLY,
                            null // may be filled with information about the running machine but
                            // keep it emtpy for now
                            );
            return SecureRandom.getInstance("DRBG", params);
        } catch (NoSuchAlgorithmException e) {
            // Fallback: default SecureRandom (still cryptographically strong on modern JDKs)
            return new SecureRandom();
        }
    }
}
