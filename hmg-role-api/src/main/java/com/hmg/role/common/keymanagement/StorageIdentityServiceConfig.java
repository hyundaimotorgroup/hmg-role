package com.hmg.role.common.keymanagement;

import static com.hmg.role.common.keymanagement.KeyManagementUtils.parseIsoPeriod;

import com.hmg.role.util.container.Pair;
import java.time.Duration;
import java.time.Period;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageIdentityServiceConfig {
    @Bean
    public Pair<Period, Duration> secretExpiryDuration(
            @Value("${cdc.kms.client-secret-validity-period:P1M}")
                    // defaults to 1 month if not specified
                    String secretExpiryPeriodStr) {
        return parseIsoPeriod(secretExpiryPeriodStr);
    }
}
