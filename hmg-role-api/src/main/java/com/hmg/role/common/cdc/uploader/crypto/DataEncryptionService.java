package com.hmg.role.common.cdc.uploader.crypto;

import com.hmg.role.sdk.fetcher.crypto.CryptoConstants;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public final class DataEncryptionService {
    private static final int AES256_KEY_LENGTH = CryptoConstants.AES256_KEY_LENGTH;
    private static final int IV_LENGTH_BYTES = CryptoConstants.IV_LENGTH_BYTES;
    private static final int GCM_TAG_LENGTH_BITS = CryptoConstants.GCM_TAG_LENGTH_BITS;
    private static final String TRANSFORMATION = CryptoConstants.TRANSFORMATION;
    private static final String ALGORITHM = CryptoConstants.ALGORITHM;

    private final SecureRandom secureRandom;

    /**
     * Encrypt plaintext using AES-256-GCM.
     *
     * @param plaintext the data to encrypt (non-null, may be empty)
     * @param key 32-byte AES-256 key
     * @return byte[] concatenated as IV || CIPHERTEXT_WITH_TAG
     * @throws IllegalArgumentException if key length is not 32 bytes
     * @throws RuntimeException if encryption fails
     */
    public byte[] encrypt(byte[] plaintext, byte[] key) {
        require256BitKey(key);

        byte[] iv = new byte[IV_LENGTH_BYTES];
        secureRandom.nextBytes(iv);

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
            byte[] ciphertextWithTag = cipher.doFinal(plaintext == null ? new byte[0] : plaintext);

            // Return IV || ciphertext+tag
            byte[] result = new byte[iv.length + ciphertextWithTag.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(ciphertextWithTag, 0, result, iv.length, ciphertextWithTag.length);
            return result;
        } catch (Exception e) {
            log.error("unable to encrypt file, reason: {}", e.getMessage(), e);
            throw new RuntimeException("AES-GCM encryption failed", e);
        } finally {
            // optional: clear IV (not strictly necessary)
            // Arrays.fill(iv, (byte) 0);
        }
    }

    /**
     * Decrypt data produced by {@link #encrypt(byte[], byte[])}. Expects input as IV ||
     * CIPHERTEXT_WITH_TAG. Useful during key rotations.
     *
     * @param ivAndCiphertext concatenated IV and ciphertext+tag
     * @param key 32-byte AES-256 key
     * @return decrypted plaintext
     */
    public byte[] decrypt(byte[] ivAndCiphertext, byte[] key) {
        require256BitKey(key);

        if (ivAndCiphertext == null || ivAndCiphertext.length < IV_LENGTH_BYTES + 1) {
            throw new IllegalArgumentException(
                    "Invalid input: too short to contain IV and ciphertext");
        }

        byte[] iv = Arrays.copyOfRange(ivAndCiphertext, 0, IV_LENGTH_BYTES);
        byte[] ciphertextWithTag =
                Arrays.copyOfRange(ivAndCiphertext, IV_LENGTH_BYTES, ivAndCiphertext.length);

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            return cipher.doFinal(ciphertextWithTag);

        } catch (Exception e) {
            // For security, avoid revealing whether the failure is auth/tag or anything else
            throw new RuntimeException("AES-GCM decryption failed", e);
        }
    }

    public byte[] reencrypt(byte[] file, byte[] oldKey, byte[] newKey) {
        // may or may not be needed. Remove if it isn't
        byte[] plaintext = decrypt(file, oldKey);
        return encrypt(plaintext, newKey);
    }

    private static void require256BitKey(byte[] key) {
        if (key == null || key.length != AES256_KEY_LENGTH) {
            throw new IllegalArgumentException("AES-256 key must be exactly 32 bytes");
        }
    }
}
