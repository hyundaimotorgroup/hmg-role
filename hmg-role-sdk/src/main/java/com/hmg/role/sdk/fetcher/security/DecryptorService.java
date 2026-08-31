package com.hmg.role.sdk.fetcher.security;

import com.hmg.role.sdk.fetcher.crypto.DataDecryptionService;
import com.hmg.role.sdk.fetcher.dto.ProjectEncryptionKeyDto;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DecryptorService {

    private final AccessKeyApiClient accessKeyApiClient;
    private final DataDecryptionService dataDecryptor;

    // Cache
    private final AtomicReference<byte[]> cachedKey = new AtomicReference<>();
    private final AtomicReference<ZonedDateTime> cachedExpiry = new AtomicReference<>();

    public synchronized byte[] decrypt(byte[] ivAndCiphertext) {
        ensureKeyFresh();
        byte[] key = cachedKey.get();
        return dataDecryptor.decrypt(ivAndCiphertext, key);
    }

    private void ensureKeyFresh() {
        ZonedDateTime keyExpiry = cachedExpiry.get();
        byte[] key = cachedKey.get();

        if (key == null || keyExpiry == null || isExpiring(keyExpiry)) {
            refreshKeyFromApi();
        }
    }

    private boolean isExpiring(ZonedDateTime expiryUtc) {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        return expiryUtc.isBefore(now);
    }

    private void refreshKeyFromApi() {
        try {
            ProjectEncryptionKeyDto dto = accessKeyApiClient.fetchDecryptionKey();
            if (dto == null || dto.getEncryptionKey() == null || dto.getExpiredAfter() == null) {
                throw new IllegalStateException("Invalid decryption key payload from API");
            }

            // Decode Base64 -> raw AES-256 key (must be 32 bytes)
            byte[] rawKey = Base64.getDecoder().decode(dto.getEncryptionKey());

            ZonedDateTime expiredAfter =
                    ZonedDateTime.parse(
                            dto.getExpiredAfter(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            // Update cache atomically
            cachedKey.set(rawKey);
            cachedExpiry.set(expiredAfter.withZoneSameInstant(ZoneOffset.UTC).minusHours(1));

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch/update decryption key", e);
        }
    }
}
