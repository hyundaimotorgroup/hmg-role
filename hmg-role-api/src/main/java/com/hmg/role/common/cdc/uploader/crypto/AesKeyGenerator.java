package com.hmg.role.common.cdc.uploader.crypto;

import com.hmg.role.sdk.fetcher.crypto.CryptoConstants;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Secure key/IV generator for AES-256-GCM (Java 21). - Random AES-256 key generation using
 * DRBG-backed SecureRandom. - Password-based key derivation using PBKDF2-HMAC-SHA256. - 12-byte IV
 * (nonce) generation for GCM.
 */
@Service
@RequiredArgsConstructor
public final class AesKeyGenerator {

    // AES-256 key = 32 bytes
    public static final int AES256_KEY_LENGTH_BYTES = CryptoConstants.AES256_KEY_LENGTH;
    // GCM standard nonce length = 12 bytes
    public static final int GCM_IV_LENGTH_BYTES = CryptoConstants.IV_LENGTH_BYTES;

    // Auth tag length for GCM (128-bit = 16 bytes) — included here for convenience
    public static final int GCM_TAG_LENGTH_BITS = CryptoConstants.GCM_TAG_LENGTH_BITS;

    // Prefer DRBG on modern JDKs
    private final SecureRandom secureRandom;

    /**
     * Generate a random AES-256 key (32 bytes). Returns raw key bytes so you can store in a
     * KMS/keystore or wrap in SecretKeySpec.
     */
    public byte[] generateRawAes256Key() {
        byte[] key = new byte[AES256_KEY_LENGTH_BYTES];
        secureRandom.nextBytes(key);
        return key;
    }

    /**
     * Generate a random AES-256 key using JCA KeyGenerator (equivalent strength). If you prefer
     * SecretKey instead of raw bytes.
     */
    public SecretKey generateAes256SecretKey() {
        try {
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(256, secureRandom);
            return kg.generateKey();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Failed to generate AES-256 key", e);
        }
    }
}
